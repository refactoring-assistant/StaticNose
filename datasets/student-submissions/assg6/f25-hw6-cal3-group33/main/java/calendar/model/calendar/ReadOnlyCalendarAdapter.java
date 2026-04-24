package calendar.model.calendar;

import calendar.exceptions.InvalidDateTimeException;
import calendar.model.event.EventInterface;
import java.time.ZoneId;
import java.util.List;

/**
 * Adapter that provides read-only access to a calendar.
 * Wraps a CalendarInterface and exposes only query operations,
 * preventing any modifications to the underlying calendar.
 *
 * <p>This adapter cannot be downcast to CalendarInterface, ensuring
 * true read-only access even at runtime.
 */
public class ReadOnlyCalendarAdapter implements ReadOnlyCalendar {

  private final CalendarInterface calendar;

  /**
   * Creates a read-only adapter for the given calendar.
   *
   * @param calendar the calendar to wrap with read-only access
   * @throws IllegalArgumentException if calendar is null
   */
  public ReadOnlyCalendarAdapter(CalendarInterface calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  @Override
  public List<EventInterface> getAllEvents() throws InvalidDateTimeException {
    return calendar.getAllEvents();
  }

  @Override
  public List<EventInterface> getEvents(String date) throws InvalidDateTimeException {
    return calendar.getEvents(date);
  }

  @Override
  public List<EventInterface> getEvents(String startdateTime, String enddateTime)
      throws InvalidDateTimeException {
    return calendar.getEvents(startdateTime, enddateTime);
  }

  @Override
  public boolean busyStatus(String dateTimes) throws InvalidDateTimeException {
    return calendar.busyStatus(dateTimes);
  }

  @Override
  public String getCalendarName() {
    return calendar.getCalendarName();
  }

  @Override
  public ZoneId getCalendarTimeZone() {
    return calendar.getCalendarTimeZone();
  }
}