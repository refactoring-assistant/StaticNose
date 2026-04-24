package calendar.controller;

import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnDateCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * Main controller for the calendar application.
 * Handles both interactive and headless modes with multi-calendar support.
 */
public class CalendarController {
  private final CalendarContext context;
  private final CalendarView view;
  private final CommandParser parser;

  /**
   * Constructs a CalendarController with calendar context and view.
   *
   * @param context the calendar context (system + current calendar)
   * @param view the calendar view
   */
  public CalendarController(CalendarContext context, CalendarView view) {
    this.context = context;
    this.view = view;
    this.parser = new CommandParser();
  }

  /**
   * Runs the application in interactive mode.
   * Reads commands from standard input until exit command.
   */
  public void runInteractive() {
    view.displayMessage("Calendar Application - Interactive Mode");
    view.displayMessage("Type 'exit' to quit.");

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(System.in))) {

      while (true) {
        System.out.print("> ");
        String line = reader.readLine();

        if (line == null) {
          break;
        }

        if (processCommand(line.trim())) {
          break; // Exit command was executed
        }
      }
    } catch (IOException e) {
      view.displayError("Error reading input: " + e.getMessage());
    }
  }

  /**
   * Runs the application in headless mode.
   * Reads commands from the specified file.
   *
   * @param fileName the file containing commands
   */
  public void runHeadless(String fileName) {
    view.displayMessage("Calendar Application - Headless Mode");

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      boolean foundExit = false;

      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue; // Skip empty lines and comments
        }

        view.displayMessage("Executing: " + trimmed);

        if (processCommand(trimmed)) {
          foundExit = true;
          break;
        }
      }

      if (!foundExit) {
        view.displayError("Command file must end with 'exit' command");
      }

    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
    }
  }

  /**
   * Processes a single command.
   *
   * @param commandLine the command to process
   * @return true if exit command, false otherwise
   */
  private boolean processCommand(String commandLine) {
    if (commandLine.isEmpty()) {
      return false;
    }

    try {
      Command command = parser.parse(commandLine);

      if (command instanceof ExitCommand) {
        view.displayMessage("Exiting calendar application.");
        return true;  // Signal to stop processing
      }

      // Check if it's a system-level command (uses executeOnSystem)
      if (isSystemCommand(command)) {
        executeSystemCommand(command);
      } else {
        // Regular event command - needs current calendar
        if (!context.hasCurrentCalendar()) {
          throw new IllegalStateException(
              "No calendar in use. Use 'use calendar --name <name>' first.");
        }
        command.execute(context.getCurrentCalendar(), view);
      }

      // Check if it's an exit command
      return command instanceof ExitCommand;

    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
      return false;
    } catch (IllegalStateException e) {
      view.displayError(e.getMessage());
      return false;
    } catch (Exception e) {
      view.displayError("Unexpected error: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Checks if command is a system-level command.
   */
  private boolean isSystemCommand(Command command) {
    return command instanceof CreateCalendarCommand
        || command instanceof EditCalendarCommand
        || command instanceof UseCalendarCommand
        || command instanceof CopyEventCommand
        || command instanceof CopyEventsOnDateCommand
        || command instanceof CopyEventsBetweenCommand // I added this when
        || command instanceof ExitCommand  // Exit doesn't need calendar
        || command instanceof ExportCommand;  // Export now needs timezone
  }

  /**
   * Executes a system-level command using reflection.
   */
  private void executeSystemCommand(Command command) {
    try {
      Method method = command.getClass().getMethod(
          "executeOnSystem",
          CalendarContext.class,
          CalendarView.class
      );
      method.invoke(command, context, view);
    } catch (Exception e) {
      if (e.getCause() != null) {
        throw new RuntimeException(e.getCause());
      }
      throw new RuntimeException(e);
    }
  }
}