package calendar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Controller for the calendar application.
 */
public class CalendarController {
  private final CalendarModel model;
  private final CalendarView view;
  private final CommandParser parser;

  /**
   * Controller for the calendar application.
   *
   * @param model the calendar model.
   * @param view the view part.
   */
  public CalendarController(CalendarModel model, CalendarView view) {
    this.model = model;
    this.view = view;
    this.parser = new CommandParser((CalendarModelImpl) model);
  }

  /**
   * Running the calendar in interactive mode.
   */
  public void runInteractiveMode() {

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      String input;

      while (true) {
        view.displayPrompt();
        input = reader.readLine();

        if (input == null) {
          break;
        }

        String trimmed = input.trim();

        if (trimmed.isEmpty()) {
          continue;
        }

        if (trimmed.equalsIgnoreCase("exit")) {
          break;
        }

        try {
          parser.parseAndExecute(trimmed);
          view.displaySuccess("Command executed successfully");
        } catch (Exception e) {
          view.displayError(e.getMessage());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Running the calendar in headless mode.
   *
   * @param filename the filename of the file with commands.
   */
  public void runHeadlessMode(String filename) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      int lineNumber = 0;
      boolean exitFound = false;

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
          continue;
        }

        if (trimmed.equalsIgnoreCase("exit")) {
          exitFound = true;
          view.displayMessage("Commands executed successfully.");
          break;
        }

        try {
          parser.parseAndExecute(trimmed);
        } catch (Exception e) {
          view.displayError("Error on line " + lineNumber + ": " + e.getMessage());
        }
      }

      if (!exitFound) {
        view.displayError("Error: File ended without 'exit' command.");
      }

    } catch (IOException e) {
      view.displayError("Error reading file: " + e.getMessage());
    }
  }
}

