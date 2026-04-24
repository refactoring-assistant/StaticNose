package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import calendar.model.EventInterface;
import calendar.util.EventQueryHelper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to print all events on a given date.
 */
public class PrintEventsOnCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private static final ZoneId TIMEZONE = ZoneId.of("America/New_York");
  private final LocalDate date;

  /**
   * Creates a command for the given date.
   *
   * @param date The date to print events for.
   */
  public PrintEventsOnCommand(CalendarControllerInterface controller, LocalDate date) {
    this.controller = controller;
    this.date = date;
  }

  @Override
  public String execute() {
    try {
      ZonedDateTime start = date.atStartOfDay(TIMEZONE);
      ZonedDateTime end = date.atTime(23, 59, 59).atZone(TIMEZONE);

      List<EventInterface> events =
          controller.queryEvents(EventQueryHelper.overlapsWith(start, end));

      if (events.isEmpty()) {
        return "No events on " + date;
      }

      StringBuilder sb = new StringBuilder("Events on " + date + ":\n");
      for (EventInterface e : events) {
        sb.append("• ").append(e.getSubject())
            .append(" (").append(e.getStart().toLocalTime())
            .append(" - ").append(e.getEnd() != null ? e.getEnd().toLocalTime() : "5:00 PM")
            .append(")");
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
    return "Print events on a specific date";
  }
}