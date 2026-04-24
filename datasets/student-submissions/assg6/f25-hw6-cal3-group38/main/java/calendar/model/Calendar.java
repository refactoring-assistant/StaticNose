package calendar.model;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Represents a calendar with a unique name and timezone.
 */
public class Calendar {
  private final String name;
  private ZoneId timezone;
  private final CalendarModel model;

  /**
   * Constructs a new Calendar.
   *
   * @param name the unique calendar name
   * @param timezone the timezone for this calendar
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    this.name = name.trim();
    this.timezone = timezone;
    this.model = new CalendarModel();
  }

  /**
   * Gets the calendar name.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the calendar timezone.
   *
   * @return the timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the calendar timezone.
   *
   * @param timezone the new timezone
   */
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.timezone = timezone;
  }

  /**
   * Gets the calendar model.
   *
   * @return the calendar model
   */
  public CalendarModel getModel() {
    return model;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Calendar calendar = (Calendar) o;
    return Objects.equals(name, calendar.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return String.format("Calendar{name='%s', timezone=%s}", name, timezone);
  }
}