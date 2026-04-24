package calendar.controller.commands;

import calendar.controller.commanddata.EditMultipleEventsCommandData;
import calendar.controller.handlers.EditMultipleEventsHandler;
import calendar.model.interfaces.CalendarEditable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the command used to edit an existing events in the calendar.
 * It is called when the user input starts with "edit events".
 * User gives the subject and start date.
 * This class handles parsing, while EditMultipleEventsHandler handles the logic.
 */
public class EditMultipleEventsCommand implements Command {

  private final EditMultipleEventsHandler handler;

  /**
   * We are passing the active calendar in the constructor.
   * It is the calendar on which the current operation is to be performed.
   *
   * @param calendarModel current active calendar.
   */
  public EditMultipleEventsCommand(CalendarEditable calendarModel) {
    this.handler = new EditMultipleEventsHandler(calendarModel);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return a string messages showing details of all updated events.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    EditMultipleEventsCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into an EditMultipleEventsCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return EditMultipleEventsCommandData containing parsed information
   */
  public EditMultipleEventsCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() < 8) {
      throw new IllegalArgumentException("Error: Invalid command format");
    }

    if (!"from".equalsIgnoreCase(parsedCommand.get(4))
        || !"with".equalsIgnoreCase(parsedCommand.get(6))) {
      throw new IllegalArgumentException("Error: Invalid command syntax.");
    }

    String propertyToUpdate = parsedCommand.get(2);
    String subject = parsedCommand.get(3);
    LocalDateTime startDateTime = LocalDateTime.parse(parsedCommand.get(5));
    String newPropertyValue = parsedCommand.get(7);

    return new EditMultipleEventsCommandData(propertyToUpdate, subject, startDateTime,
        newPropertyValue);
  }
}
