package calendar.command;

import calendar.exception.CalendarException;
import calendar.model.InEvent;
import calendar.service.InEventService;
import calendar.view.InCalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command for querying events by date or date range.
 */
public class QueryEventCommand implements InCommand {

  private final InEventService eventService;
  private final InCalendarView view;
  private final LocalDate date;
  private final LocalDateTime startRange;
  private final LocalDateTime endRange;

  /**
   * Constructs a QueryEventCommand.
   *
   * @param eventService the event service
   * @param view         the view
   * @param date         specific date to query (null for range query)
   * @param startRange   start of date range (null for single date query)
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
