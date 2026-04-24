package calendar.controller;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Implementation of CalendarController that handles command execution
 * in both interactive and headless modes.
 */
public class Calendar implements CalendarController {
  private final CalendarModel model;
  private final CalendarView view;
  private final BufferedReader input;
  private final CommandParser parser;

  /**
   * Creates a Calendar controller with the specified model, view, and input source.
   *
   * @param model the calendar model to operate on
   * @param view the view to display output
   * @param input the input source for reading commands
   */
  public Calendar(CalendarModel model, CalendarView view, Reader input) {
    this.model = model;
    this.view = view;
    this.parser = new CommandParser();
    this.input = new BufferedReader(input);
  }

  @Override
  public void runInteractive() throws IOException {
    view.displayMessage("Calendar started. Enter commands (type 'exit' to quit): ");

    String line;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      if (line.equalsIgnoreCase("exit")) {
        view.displayMessage("Calendar has been terminated.");
        break;
      }
      try {
        Command command = parser.parseCommand(line);
        command.execute(model, view);
      } catch (Exception e) {
        view.displayError(e.getMessage());
      }
    }
  }

  @Override
  public void runHeadless(String commandsFile) throws IOException {
    List<String> commands = Files.readAllLines(Paths.get(commandsFile));
    boolean hasExit = false;
    for (String line : commands) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      if (line.equalsIgnoreCase("exit")) {
        hasExit = true;
        break;
      }
      try {
        Command command = parser.parseCommand(line);
        command.execute(model, view);
      } catch (Exception e) {
        view.displayError(e.getMessage());
      }
    }
    if (!hasExit) {
      view.displayError("Error: Command file must end with 'exit'");
    }
  }
}
