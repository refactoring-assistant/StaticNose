package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import calendar.model.EventInterface;
import calendar.util.EventQueryHelper;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to print all events between two given date-times.
 */
public class PrintEventsBetweenCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final ZonedDateTime start;
  private final ZonedDateTime end;

  /**
   * Creates a command for a specific time range.
   *
   * @param start Start of the time range.
   * @param end End of the time range.
   */
  public PrintEventsBetweenCommand(CalendarControllerInterface controller, ZonedDateTime start,
                                   ZonedDateTime end) {
    this.controller = controller;
    this.start = start;
    this.end = end;
  }

  @Override
  public String execute() {
    try {
      List<EventInterface> events =
          controller.queryEvents(EventQueryHelper.overlapsWith(start, end));

      if (events.isEmpty()) {
        return "No events in specified range";
      }

      StringBuilder sb = new StringBuilder("Events from " + start + " to " + end + ":\n");
      for (EventInterface e : events) {
        sb.append("• ").append(e.getSubject())
            .append(" starting on ").append(e.getStart().toLocalDate())
            .append(" at ").append(e.getStart().toLocalTime())
            .append(", ending on ")
            .append(e.getEnd() != null ? e.getEnd().toLocalDate() : e.getStart().toLocalDate())
            .append(" at ").append(e.getEnd() != null ? e.getEnd().toLocalTime() : "5:00 PM");
        if (e.getLocation() != null && !e.getLocation().isEmpty()) {
          sb.append(" at ").append(e.getLocation());
        }
        sb.append("\n");
      }
      return sb.toString().trim();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Print events in a date range";
  }
}