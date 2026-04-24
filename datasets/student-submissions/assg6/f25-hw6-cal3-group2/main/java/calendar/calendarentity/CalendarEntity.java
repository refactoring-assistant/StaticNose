package calendar.calendarentity;

import calendar.model.Calendar;
import calendar.model.CalendarInterface;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Represents a calendar entity with a unique name, timezone, and calendar.
 */
public class CalendarEntity implements CalendarEntityInterface {
  private String calendarName;
  private ZoneId zoneId;
  private CalendarInterface calendar;

  /**
   * Private constructor used by the Builder.
   *
   * @param builder the builder instance
   */
  private CalendarEntity(CalendarEntityBuilder builder) {
    if (builder.calendarName == null || builder.calendarName.trim().isEmpty()) {
      throw new IllegalStateException("Calendar name must be set.");
    }
    if (builder.zoneId == null) {
      throw new IllegalStateException("Timezone must be set.");
    }
    this.calendarName = builder.calendarName;
    this.zoneId = builder.zoneId;

    if (builder.calendar != null) {
      this.calendar = builder.calendar;
    } else {
      this.calendar = new Calendar();
    }
  }

  /**
   * Static factory method to create a new CalendarEntityBuilder.
   *
   * @return a new CalendarEntityBuilder instance
   */
  public static CalendarEntityBuilder builder() {
    return new CalendarEntityBuilder();
  }

  /**
   * Inner builder class for constructing a CalendarEntity object.
   */
  public static class CalendarEntityBuilder {
    private String calendarName;
    private ZoneId zoneId;
    private Calendar calendar;

    /**
     * Constructs a new CalendarEntityBuilder to use chained set methods.
     */
    public CalendarEntityBuilder() {
      this.calendarName = null;
      this.zoneId = null;
      this.calendar = null;
    }

    /**
     * Sets the name of the calendar entity.
     *
     * @param calendarName the calendar name
     * @return the builder instance
     * @throws IllegalArgumentException if calendar name is null or empty
     */
    public CalendarEntityBuilder calendarName(String calendarName) {
      if (calendarName == null || calendarName.trim().isEmpty()) {
        throw new IllegalArgumentException("Calendar name cannot be null or empty");
      }
      this.calendarName = calendarName.trim();
      return this;
    }

    /**
     * Sets the timezone of the calendar entity.
     *
     * @param zoneId the timezone
     * @return the builder instance
     * @throws IllegalArgumentException if timezone is null
     */
    public CalendarEntityBuilder timezone(ZoneId zoneId) {
      if (zoneId == null) {
        throw new IllegalArgumentException("Timezone cannot be null");
      }
      this.zoneId = zoneId;
      return this;
    }

    /**
     * Sets the Calendar object.
     *
     * @param calendar the Calendar object to use
     * @return the builder instance
     * @throws IllegalArgumentException if calendar is null
     */
    public CalendarEntityBuilder calendar(Calendar calendar) {
      if (calendar == null) {
        throw new IllegalArgumentException("Calendar cannot be null");
      }
      this.calendar = calendar;
      return this;
    }

    /**
     * Creates and returns the CalendarEntity object.
     *
     * @return the CalendarEntity
     */
    public CalendarEntity build() {
      return new CalendarEntity(this);
    }
  }

  @Override
  public String getCalendarName() {
    return calendarName;
  }

  @Override
  public ZoneId getTimeZone() {
    return zoneId;
  }

  @Override
  public CalendarInterface getCalendar() {
    return calendar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CalendarEntity that = (CalendarEntity) o;
    return Objects.equals(calendarName, that.calendarName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(calendarName);
  }

  @Override
  public String toString() {
    return "CalendarEntity{"
        + "name='" + calendarName + '\''
        + ", timezone=" + zoneId
        + '}';
  }
}