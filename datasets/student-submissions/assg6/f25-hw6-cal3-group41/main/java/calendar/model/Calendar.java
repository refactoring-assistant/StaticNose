package calendar.model;

import java.time.ZoneId;

/**
 * Calendar with a name and timezone.
 */
public class Calendar {
  private String name;
  private ZoneId timezone;
  private final CalendarModelImpl model;

  /**
   * Creates a calendar.
   *
   * @param name the calendar name
   * @param timezone the timezone
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.name = name.trim();
    this.timezone = timezone;
    this.model = new CalendarModelImpl();
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    this.name = name.trim();
  }

  /**
   * Gets the timezone.
   *
   * @return the timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the timezone.
   *
   * @param timezone the timezone
   */
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.timezone = timezone;
  }

  /**
   * Gets the model.
   *
   * @return the model
   */
  public CalendarModel getModel() {
    return model;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Calendar calendar = (Calendar) obj;
    return name.equalsIgnoreCase(calendar.name);
  }

  @Override
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }
}

