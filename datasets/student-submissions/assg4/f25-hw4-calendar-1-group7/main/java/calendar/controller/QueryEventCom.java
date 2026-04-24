package calendar.controller;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.view.MyCalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to query events by date or date range.
 */
public class QueryEventCom implements Command {
  private final LocalDate date;
  private final LocalDateTime startRange;
  private final LocalDateTime endRange;
  private final boolean isRangeQuery;

  /**
   * Constructor for single date query.
   *
   * @param date the date to query
   */
  public QueryEventCom(LocalDate date) {
    this.date = date;
    this.startRange = null;
    this.endRange = null;
    this.isRangeQuery = false;
  }

  /**
   * Constructor for date range query.
   *
   * @param startRange the start of the range
   * @param endRange the end of the range
   */
  public QueryEventCom(LocalDateTime startRange, LocalDateTime endRange) {
    this.date = null;
    this.startRange = startRange;
    this.endRange = endRange;
    this.isRangeQuery = true;
  }

  @Override
  public void execute(Calendar calendar, MyCalendarView view) {
    try {
      if (isRangeQuery) {
        queryRange(calendar, view);
      } else {
        queryDate(calendar, view);
      }
    } catch (Exception e) {
      view.displayError("Error querying events: " + e.getMessage());
    }
  }

  private void queryDate(Calendar calendar, MyCalendarView view) {
    List<Event> events = calendar.getEventsOnDate(date);

    if (events.isEmpty()) {
      view.displayMessage("No events on " + date);
      return;
    }

    view.displayMessage("Events on " + date + ":");
    view.displayEvents(events);
  }

  private void queryRange(Calendar calendar, MyCalendarView view) {
    List<Event> events = calendar.getEventsInRange(startRange, endRange);

    if (events.isEmpty()) {
      view.displayMessage("No events between " + startRange + " and " + endRange);
      return;
    }

    view.displayMessage("Events from " + startRange + " to " + endRange + ":");
    view.displayEvents(events);
  }

  @Override
  public boolean validate() {
    if (isRangeQuery) {
      return startRange != null && endRange != null && !endRange.isBefore(startRange);
    } else {
      return date != null;
    }
  }
}