package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Command for copying multiple events between calendars.
 */
public class CopyEventsCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a CopyEventsCommand.
   *
   * @param tokens the command tokens
   */
  public CopyEventsCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 8 || !"on".equals(tokens[2]) || !"--target".equals(tokens[4])
          || !"to".equals(tokens[6])) {
        return "Error: Invalid copy events command format. "
            + "Usage: copy events on <date> --target <calendar> to <date>";
      }

      LocalDate sourceDate = parseDate(tokens[3]);
      String targetCalendar = tokens[5];
      LocalDate targetDate = parseDate(tokens[7]);

      controller.copyEventsOnDate(sourceDate, targetCalendar, targetDate);
      return "Events on " + sourceDate + " copied successfully to calendar '"
          + targetCalendar + "' starting " + targetDate;
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