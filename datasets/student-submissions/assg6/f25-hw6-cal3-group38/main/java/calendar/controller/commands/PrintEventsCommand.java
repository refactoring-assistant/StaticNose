package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Command for printing events.
 */
public class PrintEventsCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a PrintEventsCommand.
   *
   * @param tokens the command tokens
   */
  public PrintEventsCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 3) {
        return "Error: Invalid print command";
      }

      if ("on".equals(tokens[2])) {
        // Print events on specific date
        LocalDate date = parseDate(tokens[3]);
        return controller.getEventsOnDate(date);
      } else if ("from".equals(tokens[2])) {
        // Print events in range
        LocalDateTime start = parseDateTime(tokens[3]);
        LocalDateTime end = parseDateTime(tokens[5]);
        return controller.getEventsInRange(start, end);
      } else {
        return "Error: Invalid print syntax";
      }
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  private LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }
  }

  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date-time format: " + dateTimeStr);
    }
  }
}