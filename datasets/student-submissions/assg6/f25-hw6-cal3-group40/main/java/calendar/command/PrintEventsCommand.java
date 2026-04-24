package calendar.command;

import calendar.model.Event;
import calendar.service.CalendarService;
import calendar.view.EventViewData;
import calendar.view.textbased.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command to print events on a specific date or within a range.
 */
public class PrintEventsCommand implements CalendarCommand {
  private final Map<String, String> params;
  private final boolean isDateQuery;

  /**
   * Constructs a PrintEventsCommand.
   *
   * @param params      The keyword arguments (on, from, to).
   * @param isDateQuery true for "print events on", false for "print events
   *                    from...to".
   */
  public PrintEventsCommand(Map<String, String> params, boolean isDateQuery) {
    this.params = params;
    this.isDateQuery = isDateQuery;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) throws IllegalArgumentException {
    try {
      if (isDateQuery) {
        executeDateQuery(service, view);
      } else {
        executeRangeQuery(service, view);
      }
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format.", e);
    }
  }

  private void executeDateQuery(CalendarService service, CalendarView view) {
    String date = params.get("on");
    if (date == null) {
      throw new IllegalArgumentException("Invalid command. Format: print events on <date>");
    }
    ZoneId activeZone = service.getCurrentCalendarTimezone();
    if (activeZone == null) {
      throw new IllegalStateException("No calendar is in use. Please 'use calendar' first.");
    }
    List<Event> events = service.getEventsOn(LocalDate.parse(date));

    List<EventViewData> eventsForView = new ArrayList<>();
    for (Event e : events) {
      LocalDateTime startLdt = LocalDateTime.ofInstant(e.getStart(), activeZone);
      LocalDateTime endLdt = LocalDateTime.ofInstant(e.getEnd(), activeZone);

      eventsForView.add(new EventViewData(
          e.getSubject(),
          e.getStart(),
          e.getEnd(),
          e.getDescription(),
          e.getLocation(),
          e.isPrivate(),
          e.getSeriesId(),
          e.isSeries()));
    }

    view.showEvents(eventsForView);
  }

  private void executeRangeQuery(CalendarService service, CalendarView view) {
    String fromStr = params.get("from");
    String toStr = params.get("to");

    if (fromStr == null || toStr == null) {
      throw new IllegalArgumentException("Invalid command. Format: print events from <start> "
          + "to <end>");
    }

    LocalDateTime from = LocalDateTime.parse(fromStr);
    LocalDateTime to = LocalDateTime.parse(toStr);

    ZoneId activeZone = service.getCurrentCalendarTimezone();
    if (activeZone == null) {
      throw new IllegalStateException("No calendar is in use. Please 'use calendar' first.");
    }

    List<Event> events = service.getEventsBetween(from, to);
    List<EventViewData> eventsForView = new ArrayList<>();
    for (Event e : events) {
      LocalDateTime startLdt = LocalDateTime.ofInstant(e.getStart(), activeZone);
      LocalDateTime endLdt = LocalDateTime.ofInstant(e.getEnd(), activeZone);

      eventsForView.add(new EventViewData(
          e.getSubject(),
          e.getStart(),
          e.getEnd(),
          e.getDescription(),
          e.getLocation(),
          e.isPrivate(),
          e.getSeriesId(),
          e.isSeries()));
    }
    view.showEventSchedule(eventsForView);
  }
}