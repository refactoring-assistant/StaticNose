package calendar.controller.commands;

import calendar.controller.commanddata.EditCalendarCommandData;
import calendar.controller.handlers.EditCalendarHandler;
import calendar.model.interfaces.CalendarContainer;
import java.util.List;

/**
 * Edit Calendar command used to update the name and time zone of a active calendar.
 * This class handles parsing, while EditCalendarHandler handles the logic.
 */
public class EditCalendarCommand implements Command {

  private final EditCalendarHandler handler;

  /**
   * We are passing the calendar Manager to in which we have to update the details.
   *
   * @param calendarManager current calendar manager.
   */
  public EditCalendarCommand(CalendarContainer calendarManager) {
    this.handler = new EditCalendarHandler(calendarManager);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return a string based on the operation performed.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    EditCalendarCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into an EditCalendarCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return EditCalendarCommandData containing parsed information
   */
  public EditCalendarCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() != 7
        || !parsedCommand.get(2).equalsIgnoreCase("--name")
        || !parsedCommand.get(4).equalsIgnoreCase("--property")) {
      throw new IllegalArgumentException("Error: Invalid edit calendar command. "
          + "Usage: edit calendar --name <name-of-calendar> "
          + "--property <property-name> <new-property-value>");
    }
    String calendarName = parsedCommand.get(3);
    String propertyName = parsedCommand.get(5);
    String newPropertyValue = parsedCommand.get(6);
    return new EditCalendarCommandData(calendarName, propertyName, newPropertyValue);
  }
}
