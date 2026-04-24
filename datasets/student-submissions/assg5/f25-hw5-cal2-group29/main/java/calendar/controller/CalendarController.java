package calendar.controller;

import calendar.controller.ExitCommand.ExitApplicationException;
import calendar.model.CalendarApplication;
import calendar.view.CalendarView;

/**
 * Holds the top-level CalendarApplication, which manages all calendars.
 */
public class CalendarController {

  private final CalendarApplication model;
  private final CalendarView view;
  private final CommandParser parser;

  /**
   * Constructs the main controller.
   *
   * @param model  The top-level application model.
   * @param view   The view (e.g., Interface for command line).
   * @param parser The parser to translate strings to commands.
   */
  public CalendarController(CalendarApplication model, CalendarView view, CommandParser parser) {
    if (model == null || view == null || parser == null) {
      throw new IllegalArgumentException("Model, View, and Parser cannot be null.");
    }
    this.model = model;
    this.view = view;
    this.parser = parser;
  }

  /**
   * Processes a single line of user input.
   *
   * @param line A single command string from the user (e.g., "exit").
   * @throws ExitApplicationException if the command was 'exit', to signal the
   *                                  main application loop to terminate.
   */
  public void processCommand(String line) throws ExitApplicationException {
    if (line == null || line.trim().isEmpty()) {
      return;
    }

    try {
      Command command = parser.parse(line);
      command.execute(model, view);
    } catch (ExitApplicationException e) {
      throw e;
    } catch (IllegalArgumentException | IllegalStateException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("An unexpected error occurred: " + e.getMessage());
      e.printStackTrace();
    }
  }
}