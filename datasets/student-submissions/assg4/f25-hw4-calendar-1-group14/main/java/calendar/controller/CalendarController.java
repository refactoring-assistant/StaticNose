package calendar.controller;

import calendar.controller.command.Icommand;
import calendar.controller.parser.CommandParser;
import calendar.controller.parser.IcommandParser;
import calendar.exceptions.InvalidCommandException;
import calendar.model.calendar.Icalendar;
import calendar.view.IcalendarView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Controller that coordinates between the calendar model and view.
 * Processes commands and displays results.
 */
public class CalendarController implements IcalendarController {

  private final Icalendar calendar;
  private final IcalendarView view;
  private final IcommandParser parser;

  /**
   * Constructor for CalendarController.
   *
   * @param calendar the calendar model
   * @param view the view for displaying output
   */
  public CalendarController(Icalendar calendar, IcalendarView view) {
    this.calendar = calendar;
    this.view = view;
    this.parser = new CommandParser();
  }

  /**
   * Execute a command string.
   *
   * @param commandString the command to execute
   * @throws Exception if the command cannot be parsed or executed
   */
  @Override
  public void executeCommand(String commandString) throws Exception {
    try {
      Icommand command = parser.parseCommand(commandString);

      if (command == null) {
        return;
      }

      command.execute(calendar, view);

    } catch (InvalidCommandException e) {
      view.displayError(e.getMessage());
      throw e;
    } catch (Exception e) {
      view.displayError("Command execution failed: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Run the calendar application in interactive mode.
   * Reads commands from standard input until exit command is received.
   */
  @Override
  public void runInteractive() {
    Scanner scanner = new Scanner(System.in);

    view.displayMessage("Calendar application started in interactive mode.");
    view.displayMessage("Type 'exit' to quit.");

    while (true) {
      try {
        System.out.print("> ");
        String commandString = scanner.nextLine().trim();

        if (commandString.equalsIgnoreCase("exit")) {
          view.displayMessage("Exiting calendar application.");
          break;
        }

        if (commandString.isEmpty()) {
          continue;
        }

        executeCommand(commandString);

      } catch (Exception e) {
        // Error already displayed by executeCommand
      }
    }

    scanner.close();
  }

  /**
   * Run the calendar application in headless mode.
   * Reads and executes commands from a file.
   *
   * @param commandFilePath the path to the command file
   * @throws Exception if the file cannot be read or does not end with exit command
   */
  @Override
  public void runHeadless(String commandFilePath) throws Exception {
    BufferedReader reader = null;
    boolean foundExit = false;

    try {
      reader = new BufferedReader(new FileReader(commandFilePath));
      String line;
      int lineNumber = 0;

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        line = line.trim();

        if (line.isEmpty()) {
          continue;
        }

        if (line.equalsIgnoreCase("exit")) {
          foundExit = true;
          view.displayMessage("Exiting calendar application.");
          break;
        }

        try {
          executeCommand(line);
        } catch (Exception e) {
          // Error already displayed
        }
      }

      if (!foundExit) {
        view.displayError("File must end with 'exit' command");
        throw new InvalidCommandException("File must end with 'exit' command");
      }

    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
      throw e;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // Ignore close error
        }
      }
    }
  }
}