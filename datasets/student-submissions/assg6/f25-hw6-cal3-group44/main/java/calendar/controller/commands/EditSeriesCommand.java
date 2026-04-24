package calendar.controller.commands;

import calendar.controller.commanddata.EditSeriesCommandData;
import calendar.controller.handlers.EditSeriesHandler;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the command used to edit an existing events in the calendar.
 * It is called when the user input starts with "edit series".
 * User gives the subject and start date.
 * This class handles parsing, while EditSeriesHandler handles the logic.
 */
public class EditSeriesCommand implements Command {

  private final EditSeriesHandler handler;

  /**
   * We are passing the active calendar in the constructor.
   * It is the calendar on which the current operation is to be performed.
   *
   * @param calendarModel current active calendar.
   */
  public EditSeriesCommand(CalendarEditable calendarModel) {
    this.handler = new EditSeriesHandler(calendarModel);
  }

  /**
   * Static helper method to format the return value.
   *
   * @param output     the string builder to append to
   * @param currEvents the list of events to format
   */
  public static void getReturnValue(StringBuilder output, List<EventReadOnly> currEvents) {
    int i = 0;
    for (EventReadOnly e : currEvents) {
      if (i == 0) {
        output.append("Event updated:").append(System.lineSeparator());
        i++;
      }
      output.append(e.toString()).append(System.lineSeparator());
    }
    if (i == 0) {
      output.append("No updated Events:").append(System.lineSeparator());
    }
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return a string messages showing details of all updated events.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    EditSeriesCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into an EditSeriesCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return EditSeriesCommandData containing parsed information
   */
  public EditSeriesCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() < 8) {
      throw new IllegalArgumentException("Error: Invalid command format");
    }

    if (!"from".equalsIgnoreCase(parsedCommand.get(4))
        || !"with".equalsIgnoreCase(parsedCommand.get(6))) {
      throw new IllegalArgumentException("Error: Invalid command syntax");
    }

    String propertyToUpdate = parsedCommand.get(2);
    String subject = parsedCommand.get(3);
    LocalDateTime startDateTime = LocalDateTime.parse(parsedCommand.get(5));
    String newPropertyValue = parsedCommand.get(7);

    return new EditSeriesCommandData(propertyToUpdate, subject, startDateTime, newPropertyValue);
  }
}
