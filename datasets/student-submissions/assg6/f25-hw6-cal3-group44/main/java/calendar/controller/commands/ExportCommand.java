package calendar.controller.commands;

import calendar.controller.commanddata.ExportCommandData;
import calendar.controller.handlers.ExportEventHandler;
import calendar.model.interfaces.CalendarEditable;
import java.util.List;

/**
 * Executes the "export cal" command from the user input.
 * Represents the command used to export the calendar events into csv file.
 * This class handles parsing, while ExportEventHandler handles the logic.
 */
public class ExportCommand implements Command {

  private final ExportEventHandler handler;

  /**
   * We are passing the active calendar in the constructor.
   * It is the calendar on which the current operation is to be performed.
   *
   * @param calendarModel current active calendar.
   */
  public ExportCommand(CalendarEditable calendarModel) {
    this.handler = new ExportEventHandler(calendarModel);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return the result string
   */
  @Override
  public String execute(List<String> parsedCommand) {
    ExportCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into an ExportCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return ExportCommandData containing parsed information
   */
  public ExportCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() != 3) {
      throw new IllegalArgumentException("Error: Invalid export command");
    }

    String fileName = parsedCommand.get(2);
    return new ExportCommandData(fileName);
  }
}
