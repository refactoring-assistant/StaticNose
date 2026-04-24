package calendar.controller;

import calendar.model.CalendarModel;
import calendar.model.CalendarModelImpl;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Implementation of CalendarController interface.
 */
public class CalendarControllerImpl implements CalendarController {

  private final CalendarView view;
  private final CommandParser parser;

  /**
   * Constructor for CalendarControllerImpl.
   *
   * @param model the calendar model
   * @param view  the calendar view
   */
  public CalendarControllerImpl(CalendarModel model, CalendarView view) {
    this.view = view;
    this.parser = new CommandParser((CalendarModelImpl) model);
  }

  /**
   * Runs the controller with the provided input source.
   *
   * @param input the input source (Reader)
   */
  @Override
  public void run(Reader input) {
    BufferedReader reader = null;

    try {
      reader = new BufferedReader(input);

      String line;
      int lineNumber = 0;
      boolean exitFound = false;

      boolean isInteractive = (input instanceof InputStreamReader) || (input
          instanceof StringReader);

      if (isInteractive) {
        view.displayPrompt();
      }

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
          if (isInteractive) {
            view.displayPrompt();
          }
          continue;
        }

        if (trimmed.equalsIgnoreCase("exit")) {
          exitFound = true;
          if (!isInteractive) {
            view.displayMessage("Commands executed successfully.");
          }
          break;
        }

        try {
          parser.parseAndExecute(trimmed);
          if (isInteractive) {
            view.displaySuccess("Command executed successfully");
          }
        } catch (Exception e) {
          if (isInteractive) {
            view.displayError(e.getMessage());
          } else {
            view.displayError("Error on line " + lineNumber + ": " + e.getMessage());
          }
        }

        if (isInteractive) {
          view.displayPrompt();
        }
      }

      if (!exitFound && lineNumber > 0 && !isInteractive) {
        view.displayError("File ended without 'exit' command");
      }

    } catch (IOException e) {
      view.displayError("Error reading input: " + e.getMessage());
    }
  }

  /**
   * Legacy method for headless mode - delegates to run().
   */
  public void runHeadlessMode(String filename) {
    try {
      FileReader fileReader = new FileReader(filename);
      run(fileReader);
    } catch (java.io.FileNotFoundException e) {
      view.displayError("Error reading file: " + e.getMessage());
    }
  }

  /**
   * Legacy method for interactive mode - delegates to run().
   */
  public void runInteractiveMode() {
    run(new InputStreamReader(System.in));
  }
}