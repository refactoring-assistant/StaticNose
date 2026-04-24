package calendar.controller.filter;

import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a filter for the calendar that can be used
 * to get a list of events matching certain conditions.
 */
public class CalendarFilter {
  private final CalendarEditable calendar;


  /**
   * Creates a CalendarFilter object with a calendar.
   *
   * @param calendar the model that contains the event filter.
   */
  public CalendarFilter(CalendarEditable calendar) {
    this.calendar = calendar;
  }


  /**
   * Filters the events in the calendar based on the given condition.
   *
   * @param predicate the condition used to filter each event
   * @return a list of events that meet the given condition
   */
  public List<EventReadOnly> filter(Predicate<EventReadOnly> predicate) {
    List<EventReadOnly> result = new ArrayList<>();
    calendar.forEachEvent(event -> {
      if (predicate.test(event)) {
        result.add(event);
      }
    });
    return result;
  }
}
