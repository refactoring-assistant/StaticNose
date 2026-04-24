package calendar.controller.commands;

import calendar.controller.commanddata.CreateCalendarCommandData;
import calendar.controller.handlers.CreateCalendarHandler;
import calendar.model.interfaces.CalendarContainer;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.List;

/**
 * Command class to create a new calendar with a unique name and timezone
 * as specified by the user. The expected timezone format is the
 * IANA Time Zone Database format. In this format the timezone is specified as "area/location".
 * It throws an Illegal argument exception if a calendar with given name is already existing.
 * This class handles parsing, while CreateCalendarHandler handles the logic.
 * Example usage of commands:
 * "create calendar --name calName --timezone area/location"
 */
public class CreateCalendarCommand implements Command {

  private final CreateCalendarHandler handler;

  /**
   * Constructor for creating a new calendar.
   * It takes in the Calendar Manager to which this new calendar needs to be added.
   *
   * @param calendarManager the calendar manager using which calendar creation is performed.
   */
  public CreateCalendarCommand(CalendarContainer calendarManager) {
    this.handler = new CreateCalendarHandler(calendarManager);
  }

  /**
   * Parses the command and delegates to the handler.
   *
   * @param parsedCommand list of user input values.
   * @return a string based on the operation performed.
   */
  @Override
  public String execute(List<String> parsedCommand) {
    CreateCalendarCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into a CreateCalendarCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return CreateCalendarCommandData containing parsed information
   */
  public CreateCalendarCommandData parse(List<String> parsedCommand) {
    if (parsedCommand.size() != 6 || !parsedCommand.get(2).equalsIgnoreCase("--name")
        || !parsedCommand.get(4).equalsIgnoreCase("--timezone")) {
      throw new IllegalArgumentException("Error: Invalid create calendar command. "
          + "Usage: create calendar --name <calName> --timezone area/location");
    }
    String name = parsedCommand.get(3);
    ZoneId timezone;

    try {
      timezone = ZoneId.of(parsedCommand.get(5));
    } catch (ZoneRulesException e) {
      throw new IllegalArgumentException("Error: Invalid timezone");
    }
    return new CreateCalendarCommandData(name, timezone);
  }
}
