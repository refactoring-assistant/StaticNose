package calendar.model.calendar;

import calendar.exceptions.InvalidDateTimeException;
import java.time.ZoneId;

/**
 * Adapter that wraps a CalendarInterface to provide only management operations.
 * Prevents access to event manipulation methods while allowing calendar
 * property management.
 */
public class CalendarPropertiesAdapter implements CalendarPropertiesInterface {

  private final CalendarInterface calendar;

  /**
   * Creates an adapter wrapping the given calendar.
   *
   * @param calendar the calendar to wrap
   * @throws IllegalArgumentException if calendar is null
   */
  public CalendarPropertiesAdapter(CalendarInterface calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  @Override
  public String getCalendarName() {
    return calendar.getCalendarName();
  }

  @Override
  public void setName(String name) {
    calendar.setName(name);
  }

  @Override
  public ZoneId getTimezone() {
    return calendar.getCalendarTimeZone();
  }

  @Override
  public void setTimezone(ZoneId timezone) throws InvalidDateTimeException {
    calendar.setTimezone(timezone);
  }

  /**
   * Returns the underlying calendar with full access.
   * Assumed Package-private to restrict access to CalendarManager only.
   *
   * @return the wrapped calendar
   */
  public CalendarInterface getUnderlyingCalendar() {
    return this.calendar;
  }
}