package calendar.command;

import calendar.exception.CalendarException;
import calendar.model.InEvent;
import calendar.service.InEventService;
import calendar.view.InCalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Searches for events on a specific date or within a date range.
 */
public class QueryEventCommand implements InCommand {

  private final InEventService eventService;
  private final InCalendarView view;
  private final LocalDate date;
  private final LocalDateTime startRange;
  private final LocalDateTime endRange;

  /**
   * Creates a command to search for events by date or date range.
   *
   * @param eventService handles the calendar search
   * @param view         displays the results to the user
   * @param date         specific date to query (set to null if using range query)
   * @param startRange   beginning of date range (set to null if using single date query)
   * @param endRange     end of date range (null for single date query)
   */
  public QueryEventCommand(InEventService eventService, InCalendarView view,
                           LocalDate date, LocalDateTime startRange,
                           LocalDateTime endRange) {
    this.eventService = eventService;
    this.view = view;
    this.date = date;
    this.startRange = startRange;
    this.endRange = endRange;
  }

  @Override
  public void execute() throws CalendarException {
    List<InEvent> events;

    if (date != null) {
      events = eventService.queryEventsOnDate(date);
      view.displayMessage("Events on " + date + ":");
    } else {
      events = eventService.queryEventsBetween(startRange, endRange);
      view.displayMessage("Events from " + startRange + " to " + endRange + ":");
    }

    view.displayEvents(events);
  }

  @Override
  public String getDescription() {
    if (date != null) {
      return "Query events on: " + date;
    } else {
      return "Query events from " + startRange + " to " + endRange;
    }
  }
}
