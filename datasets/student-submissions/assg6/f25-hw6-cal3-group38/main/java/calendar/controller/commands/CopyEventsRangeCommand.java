package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Command for copying events in a date range between calendars.
 */
public class CopyEventsRangeCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a CopyEventsRangeCommand.
   *
   * @param tokens the command tokens
   */
  public CopyEventsRangeCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 10 || !"between".equals(tokens[2]) || !"and".equals(tokens[4])
          || !"--target".equals(tokens[6]) || !"to".equals(tokens[8])) {
        return "Error: Invalid copy events command format. "
            + "Usage: copy events between <startDate> "
            + "and <endDate> --target <calendar> to <startDate>";
      }

      LocalDate startDate = parseDate(tokens[3]);
      LocalDate endDate = parseDate(tokens[5]);
      String targetCalendar = tokens[7];
      LocalDate targetStartDate = parseDate(tokens[9]);

      controller.copyEventsInRange(startDate, endDate, targetCalendar, targetStartDate);
      return "Events between " + startDate + " and " + endDate
          + " copied successfully to calendar '" + targetCalendar + "' starting " + targetStartDate;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * parseDate.
   *
   * @param dateStr dateStr
   * @return return
   */
  public LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }
  }
}