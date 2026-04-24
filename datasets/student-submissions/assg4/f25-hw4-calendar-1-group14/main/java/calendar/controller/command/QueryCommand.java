package calendar.controller.command;

import calendar.model.calendar.Icalendar;
import calendar.model.event.Ievent;
import calendar.view.IcalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to query events from the calendar.
 * Supports querying events on a single date or within a date and time range.
 */
public class QueryCommand implements Icommand {

  private final LocalDate queryDate;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean isRangeQuery;

  /**
   * Constructor for single date query.
   *
   * @param date the date to query events for
   */
  public QueryCommand(LocalDate date) {
    this.queryDate = date;
    this.startDateTime = null;
    this.endDateTime = null;
    this.isRangeQuery = false;
  }

  /**
   * Constructor for date range query.
   *
   * @param start the start date and time of the range
   * @param end the end date and time of the range
   */
  public QueryCommand(LocalDateTime start, LocalDateTime end) {
    this.queryDate = null;
    this.startDateTime = start;
    this.endDateTime = end;
    this.isRangeQuery = true;
  }

  /**
   * Executes the query command.
   *
   * @param calendar the calendar to query events from
   * @param view the view to display the events to
   * @throws Exception if the query cannot be executed
   */
  @Override
  public void execute(Icalendar calendar, IcalendarView view) throws Exception {
    List<Ievent> events;

    if (isRangeQuery) {
      events = calendar.getEventsInRange(startDateTime, endDateTime);
      view.displayEvents(events);
    } else {
      events = calendar.getEventsOnDate(queryDate);
      view.displayEventsOnDate(events);
    }
  }
}