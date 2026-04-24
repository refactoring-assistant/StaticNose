package calendar.controller.commands;

import calendar.controller.commanddata.CopyMultipleEventsCommandData;
import calendar.controller.handlers.CopyMultipleEventsHandler;
import calendar.model.interfaces.CalendarContainer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command class responsible for copying multiple events from a
 * source calendar (the active calendar) to a target calendar.
 * This class handles parsing, while CopyMultipleEventsHandler handles the logic.
 */
public class CopyMultipleEventsCalendarCommand implements Command {

  private final CopyMultipleEventsHandler handler;

  /**
   * Constructor for copying multiple events from a source calendar to target calendar.
   * It takes in the Calendar Manager to get information about source and target calendars.
   *
   * @param calendarManager the calendar manager using which this copy events is performed
   */
  public CopyMultipleEventsCalendarCommand(CalendarContainer calendarManager) {
    this.handler = new CopyMultipleEventsHandler(calendarManager);
  }

  @Override
  public String execute(List<String> parsedCommand) {
    CopyMultipleEventsCommandData data = parse(parsedCommand);
    return handler.handle(data);
  }

  /**
   * Parses the command input into a CopyMultipleEventsCommandData object.
   *
   * @param parsedCommand the parsed command tokens
   * @return CopyMultipleEventsCommandData containing parsed information
   */
  public CopyMultipleEventsCommandData parse(List<String> parsedCommand) {
    if ((parsedCommand.size() != 8 && parsedCommand.size() != 10)
        || !checkValidCommand(parsedCommand)) {
      throw new IllegalArgumentException(
          "Error: Invalid copy events command."
              + System.lineSeparator()
              + "Usage: copy events on <dateString> --target <calendarName> to <dateString> "
              + System.lineSeparator()
              + "or copy events between <dateString> and <dateString> "
              + "--target <calendarName> to <dateString>");
    }

    if (parsedCommand.size() == 8) {
      return parseOnCase(parsedCommand);
    } else {
      return parseBetweenCase(parsedCommand);
    }
  }

  private CopyMultipleEventsCommandData parseOnCase(List<String> parsedCommand) {
    LocalDate sourceStart = parseDate(parsedCommand.get(3));
    LocalDate targetStart = parseDate(parsedCommand.get(7));
    String targetCal = parsedCommand.get(5);
    return new CopyMultipleEventsCommandData(sourceStart, targetStart, targetCal);
  }

  private CopyMultipleEventsCommandData parseBetweenCase(List<String> parsedCommand) {
    LocalDate sourceStart = parseDate(parsedCommand.get(3));
    LocalDate sourceEnd = parseDate(parsedCommand.get(5));
    LocalDate targetStart = parseDate(parsedCommand.get(9));
    String targetCal = parsedCommand.get(7);
    return new CopyMultipleEventsCommandData(sourceStart, sourceEnd, targetStart, targetCal);
  }

  private LocalDate parseDate(String dateString) {
    return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  private boolean checkValidCommand(List<String> parsedCommand) {
    if (parsedCommand.size() == 8) {
      return parsedCommand.get(2).equalsIgnoreCase("on")
          && parsedCommand.get(4).equalsIgnoreCase("--target")
          && parsedCommand.get(6).equalsIgnoreCase("to");
    } else if (parsedCommand.size() == 10) {
      return parsedCommand.get(2).equalsIgnoreCase("between")
          && parsedCommand.get(4).equalsIgnoreCase("and")
          && parsedCommand.get(6).equalsIgnoreCase("--target")
          && parsedCommand.get(8).equalsIgnoreCase("to");
    }
    return false;
  }
}
