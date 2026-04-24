package calendar.controller;

import calendar.command.Command;
import calendar.model.CalendarManager;
import calendar.view.CalendarTextView;
import java.util.Optional;
import java.util.Scanner;

/**
 * The controller of the calendar application handles user input and updates the model and view.
 */
public class CalendarController {

  private final CalendarManager manager;
  private final CalendarTextView view;
  private final CommandParser commandParser;

  /**
   * Constructs a controller with the given model and view.
   */
  public CalendarController(CalendarManager manager, CalendarTextView view) {
    if (manager == null || view == null) {
      throw new IllegalArgumentException("manager and view cannot be null");
    }
    this.manager = manager;
    this.view = view;
    this.commandParser = new CommandParser();
  }

  /**
   * Runs the REPL against the provided scanner.
   *
   * @param scanner      System.in for interactive mode, file for headless mode
   * @param exitRequired  if true (headless), EOF without an "exit" is an error
   * @throws RuntimeException when exit is required and not found before EOF
   */
  public void run(Scanner scanner, boolean exitRequired) {
    boolean encounteredExit = false;

    view.printMessage("Welcome to the Calendar!");

    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.isEmpty()) {
        continue;
      }
      if (command.equalsIgnoreCase("exit")) {
        encounteredExit = true;
        break;
      }
      try {
        handleCommand(command);
      } catch (RuntimeException e) {
        view.printMessage("Error: " + e.getMessage());
      }
    }

    if (exitRequired && !encounteredExit) {
      String msg = "Command file must end with an exit command.";
      view.printMessage(msg);
      throw new RuntimeException(msg);
    }
  }

  /**
   * Parses and executes a single command string.
   *
   * @param commandString user input line
   * @throws RuntimeException when the command is syntactically invalid
   */
  public void handleCommand(String commandString) {
    Optional<Command> maybe = commandParser.parse(commandString);
    if (maybe.isEmpty()) {
      throw new InvalidCommandException("Invalid command: " + commandString);
    }
    maybe.get().execute(manager, view);
  }

  /**
   * Exception type used for reporting invalid commands.
   */
  private static class InvalidCommandException extends RuntimeException {
    InvalidCommandException(String message) {
      super(message);
    }
  }
}
