package calendar.controller;

import calendar.controller.command.Command;
import calendar.exceptions.InvalidCommandException;
import calendar.exceptions.NoCalendarInUseException;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Controller for interactive mode.
 * Reads commands from console and processes them in real-time.
 *
 * <p>Uses the Command Pattern: parses command strings into Command objects,
 * then executes them. This separates parsing from execution and makes the
 * controller a simple orchestrator.
 */
public class InteractiveController implements CalendarController {
  private final Reader input;
  private final CalendarView view;
  private final CalendarManager calendarManager;
  private final CommandParser parser;

  /**
   * Constructs an InteractiveController with the specified input source, view,
   * and calendar manager.
   * Initializes the command parser for processing calendar commands in interactive mode.
   *
   * @param input the BufferedReader to read commands from (typically from console input)
   * @param view the CalendarView for displaying output and messages
   * @param calendarManager the CalendarManager for managing multiple calendars
   */
  public InteractiveController(Reader input, CalendarView view,
                               CalendarManager calendarManager) {
    this.input = input;
    this.view = view;
    this.calendarManager = calendarManager;
    this.parser = new CommandParser();
  }

  @Override
  public void run() {
    view.displayWelcome();
    view.displayCommandOptions();
    view.displayPrompt();

    try (BufferedReader reader = new BufferedReader(input)) {
      String command;
      while ((command = reader.readLine()) != null) {

        command = command.trim();

        if (command.isEmpty()) {
          view.displayPrompt();
          continue;
        }

        if (command.equalsIgnoreCase("exit")) {
          view.displayGoodbye();
          break;
        }

        try {
          Command cmd = parser.parse(command);
          cmd.execute(calendarManager, view);
        } catch (Exception e) {
          view.displayError(e.getMessage());
        }

        view.displayPrompt();
      }
    } catch (IOException e) {
      view.displayFileReadError(e.getMessage());
      throw new RuntimeException("Failed to read user input", e);
    } finally {
      view.close();
    }
  }
}