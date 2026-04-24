package calendar.controller.commands;

import calendar.controller.commanddata.CopySingleEventCommandData;
import calendar.controller.handlers.CopySingleEventHandler;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.CalendarContainer;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command to copy a specific event with the given name and
 * start date/time from the current calendar to the target calendar
 * to start at the specified date/time. The "to" date/time is assumed to
 * be specified in the timezone of the target calendar.
 * This class handles parsing, while CopySingleEventHandler handles the logic.
 * Example usage of commands:
 * "copy event eventName on dateStringTtimeString --target calendarName to dateStringTtimeString".
 */
public class CopySingleEventCalendarCommand implements Command {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private final CopySingleEventHandler handler;

  /**
   * Constructor for copying single event from a source calendar to target calendar.
   * It takes in the Calendar Manager to get information about source and target calendars.
   *
   * @param calendarManager the calendar manager using which this copy events is performed
   */
  public CopySingleEventCalendarCommand(CalendarContainer calendarManager) {
    this.handler = new CopySingleEventHandler(calendarManager);
  }

  /**
   * Static helper method to format output string.
   *
   * @param copiedEvents the list of copied events
   * @return the formatted output string
   */
  public static String getOutputString(List<EventReadOnly> copiedEvents) {
    if (copiedEvents.isEmpty()) {
      throw new IllegalArgumentException("Error: No events copied.");
    }
    StringBuilder result = new StringBuilder();
    result.append("Copied Events:").append(System.lineSeparator());
    for (EventReadOnly event : copiedEvents) {
      if (event.getEventType().equals(TypeOfEvent.SERIES)
          && !event.getStartDateTime().toLocalDate()
          .equals(event.getEndDateTime().toLocalDate())) {
        result.append("WARNING: Series event in target calendar is spreading across multiple days")
            .append(System.lineSeparator());
      }
      result.append(event).append(System.lineSeparator());
    }
    return result.toString();
  }

  @Override
  public String execute(List<String> parsedCommand) {
    CopySingleEventCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into a CopySingleEventCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return CopySingleEventCommandData containing parsed information
   */
  public CopySingleEventCommandData parse(List<String> parsedCommand) {
    validateCommand(parsedCommand);

    String eventName = parsedCommand.get(2);
    LocalDateTime sourceStart = parseDate(parsedCommand.get(4));
    String targetCalendarName = parsedCommand.get(6);
    LocalDateTime targetStart = parseDate(parsedCommand.get(8));

    return new CopySingleEventCommandData(eventName, sourceStart, targetCalendarName, targetStart);
  }

  private void validateCommand(List<String> cmd) {
    if (cmd.size() != 9
        || !cmd.get(3).equals("on")
        || !cmd.get(5).equals("--target")
        || !cmd.get(7).equals("to")) {
      throw new IllegalArgumentException("Error: Wrong command format. Usage: "
          + "copy event <eventName> on <dateTime> --target <calendarName> to <dateTime>");
    }
  }

  private LocalDateTime parseDate(String dateStr) {
    try {
      return LocalDateTime.parse(dateStr, FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error: Invalid date/time format: " + dateStr);
    }
  }
}
