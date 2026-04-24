package calendar.controller;

import calendar.controller.command.Command;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Controller for headless/batch mode.
 * Reads all commands from file and processes them sequentially.
 *
 * <p>Uses the Command Pattern: parses command strings into Command objects,
 * then executes them. This separates parsing from execution and makes the
 * controller a simple orchestrator.
 *
 * <p>Headless mode requires that the command file end with an 'exit' command.
 * If the file ends without an exit command, an error is displayed.
 */
public class HeadlessController implements CalendarController {
  private final Reader input;
  private final CalendarView view;
  private final CalendarManager calendarManager;
  private final CommandParser parser;

  /**
   * Constructs a HeadlessController with the specified input source, view, and calendar manager.
   * Initializes the command parser for processing calendar commands in batch mode.
   *
   * @param input the Reader to read commands from (typically from file)
   * @param view the CalendarView for displaying output and messages
   * @param calendarManager the CalendarManager for managing multiple calendars
   */
  public HeadlessController(Reader input, CalendarView view,
                            CalendarManager calendarManager) {
    this.input = input;
    this.view = view;
    this.calendarManager = calendarManager;
    this.parser = new CommandParser();
  }

  @Override
  public void run() {
    int lineNumber = 0;
    boolean exitCommandFound = false;

    try (BufferedReader reader = new BufferedReader(input)) {
      String command;
      while ((command = reader.readLine()) != null) {
        lineNumber++;
        command = command.trim();

        if (command.isEmpty()) {
          continue;
        }

        if (command.equalsIgnoreCase("exit")) {
          exitCommandFound = true;
          view.displayMessage("Executing: " + command);
          view.displayGoodbye();
          break;
        }

        view.displayMessage("Executing: " + command);

        try {
          Command cmd = parser.parse(command);
          cmd.execute(calendarManager, view);
        } catch (Exception e) {
          view.displayError("Line " + lineNumber + ": " + e.getMessage());
        }
      }

      if (!exitCommandFound) {
        view.displayNoExitCommand();
      } else {
        view.displayMessage("All commands processed.");
      }

    } catch (IOException e) {
      view.displayFileReadError(e.getMessage());
      throw new RuntimeException("Failed to read command file", e);
    } finally {
      view.close();
    }
  }
}