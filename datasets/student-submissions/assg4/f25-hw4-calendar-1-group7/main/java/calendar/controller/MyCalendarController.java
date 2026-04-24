package calendar.controller;

import calendar.model.Calendar;
import calendar.view.MyCalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for the calendar application.
 * Handles both interactive and headless modes.
 */
public class MyCalendarController {
  private final Calendar calendar;
  private final MyCalendarView view;
  private final MyCommandInterpreter parser;

  /**
   * Creates a new CalendarController.
   *
   * @param calendar the calendar model
   * @param view the view for output
   */
  public MyCalendarController(Calendar calendar, MyCalendarView view) {
    this.calendar = calendar;
    this.view = view;
    this.parser = new MyCommandInterpreter();
  }

  /**
   * Runs the application in interactive mode.
   * User types commands and sees results immediately.
   */
  public void runInteractive() {
    view.displayMessage("Calendar Application - Interactive Mode");
    view.displayMessage("Type 'exit' to quit\n");

    boolean running = true;
    while (running) {
      try {
        view.displayMessage("> ");
        String input = view.readCommand();

        if (input == null || input.trim().isEmpty()) {
          continue;
        }

        Command command = parser.parseCommand(input);

        if (!command.validate()) {
          view.displayError("Invalid command parameters");
          continue;
        }

        command.execute(calendar, view);

        if (command instanceof ExitingTheCommand) {
          running = false;
        }

      } catch (IllegalArgumentException e) {
        view.displayError("Error: " + e.getMessage());
      } catch (Exception e) {
        view.displayError("Unexpected error: " + e.getMessage());
      }
    }
  }

  /**
   * Runs the application in headless mode.
   * Reads and executes commands from a file.
   *
   * @param filename the file containing commands
   */
  public void runHeadless(String filename) {
    view.displayMessage("Calendar Application - Headless Mode");
    view.displayMessage("Reading commands from: " + filename + "\n");

    Path path = Paths.get(filename);

    if (!Files.exists(path)) {
      view.displayError("Error: File not found: " + filename);
      return;
    }

    List<String> commands = readCommandsFromFile(path);

    if (commands.isEmpty()) {
      view.displayError("Error: No commands found in file");
      return;
    }

    String lastCommand = commands.get(commands.size() - 1).trim();
    if (!lastCommand.equalsIgnoreCase("exit")) {
      view.displayError("Error: Command file must end with 'exit' command");
      return;
    }

    for (String input : commands) {
      if (input.trim().isEmpty()) {
        continue;
      }

      view.displayMessage("> " + input);

      try {
        Command command = parser.parseCommand(input);

        if (!command.validate()) {
          view.displayError("Invalid command parameters");
          continue;
        }

        command.execute(calendar, view);

      } catch (IllegalArgumentException e) {
        view.displayError("Error: " + e.getMessage());
      } catch (Exception e) {
        view.displayError("Unexpected error: " + e.getMessage());
      }

      view.displayMessage("");
    }

    view.displayMessage("Headless execution completed");
  }

  /**
   * Reads all commands from a file.
   *
   * @param path the file path
   * @return list of command strings
   */
  private List<String> readCommandsFromFile(Path path) {
    List<String> commands = new ArrayList<>();

    try (BufferedReader reader = Files.newBufferedReader(path)) {
      String line;
      while ((line = reader.readLine()) != null) {
        commands.add(line);
      }
    } catch (IOException e) {
      view.displayError("Error reading file: " + e.getMessage());
    }

    return commands;
  }
}