package calendar.controller.commands;

import calendar.controller.commanddata.EditEventCommandData;
import calendar.controller.handlers.EditEventHandler;
import calendar.model.interfaces.CalendarEditable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the command used to edit an existing event in the calendar.
 * It is called when the user input starts with "edit event".
 * This class handles parsing, while EditEventHandler handles the logic.
 */
public class EditEventCommand implements Command {

  private final EditEventHandler handler;

  /**
   * We are passing the active calendar in the constructor.
   * It is the calendar on which the current operation is to be performed.
   *
   * @param calendarModel current active calendar.
   */
  public EditEventCommand(CalendarEditable calendarModel) {
    this.handler = new EditEventHandler(calendarModel);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return a string of edited events.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    EditEventCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into an EditEventCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return EditEventCommandData containing parsed information
   */
  public EditEventCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() < 10) {
      throw new IllegalArgumentException("Error: Invalid command format");
    }

    if (!"from".equalsIgnoreCase(parsedCommand.get(4))
        || !"to".equalsIgnoreCase(parsedCommand.get(6))
        || !"with".equalsIgnoreCase(parsedCommand.get(8))) {
      throw new IllegalArgumentException("Error: Invalid command syntax");
    }
    String propertyToUpdate = parsedCommand.get(2);
    String subject = parsedCommand.get(3);
    LocalDateTime startDateTime = LocalDateTime.parse(parsedCommand.get(5));
    LocalDateTime endDateTime = LocalDateTime.parse(parsedCommand.get(7));
    String newPropertyValue = parsedCommand.get(9);

    return new EditEventCommandData(propertyToUpdate, subject, startDateTime,
        endDateTime, newPropertyValue);
  }
}
