package calendar.controller;

import calendar.controller.Command;
import calendar.controller.commands.ExitCommand;
import calendar.model.CalendarSystemModel;
import calendar.view.CalendarView;
import java.io.IOException;

/**
 * This is the main controller handling the command parsing, model and view.
 */
public class CalendarController {

  private final CalendarSystemModel model;
  private final CalendarView view;
  private final CommandParser parser;

  /**
   * Creating a controller bound to view and model.
   *
   * @param model system model
   * @param view  view to read input or render output
   */
  public CalendarController(CalendarSystemModel model, CalendarView view) {
    this.model = model;
    this.view = view;
    this.parser = new CommandParser();
  }

  /**
   * Loop to run commands until exit command is received.
   *
   * @throws IOException failure to read or write to view.
   */
  public void run() throws IOException {
    view.renderMessage("Calendar ready.");
    String line;
    while ((line = view.readInput()) != null) {
      String trimmed = line.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      try {
        Command cmd = parser.parse(trimmed);
        if (cmd instanceof ExitCommand) {
          view.renderMessage("Goodbye.");
          break;
        }
        String result = cmd.execute(model);
        if (result != null && !result.isBlank()) {
          view.renderMessage(result);
        }
      } catch (IllegalArgumentException e) {
        view.renderMessage("Error: " + e.getMessage());
      }
    }
  }
}