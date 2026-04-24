package calendar.model;

import calendar.model.repository.EventRepository;
import calendar.model.repository.InMemoryEventRepository;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Represents a calendar with a unique name, timezone,
 * and its own dedicated event repository.
 */
public class Calendar implements CalendarInterface {
  private String name;
  private ZoneId timezone;

  private final EventRepository eventRepository;

  /**
   * Constructs a new Calendar.
   *
   * @param name     The unique name of the calendar (cannot be null or blank).
   * @param timezone The timezone for this calendar (cannot be null).
   * @throws IllegalArgumentException if name is invalid or timezone is null.
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.name = name;
    this.timezone = timezone;
    this.eventRepository = new InMemoryEventRepository();
  }

  /**
   * Gets the calendar's name.
   *
   * @return The calendar name.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the calendar's name.
   *
   * @param name The new name.
   * @throws IllegalArgumentException if name is null or blank.
   */
  @Override
  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    this.name = name;
  }

  /**
   * Gets the calendar's timezone.
   *
   * @return The timezone.
   */
  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the calendar's timezone.
   *
   * @param timezone The new timezone.
   * @throws IllegalArgumentException if timezone is null.
   */
  @Override
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.timezone = timezone;
  }

  /**
   * Gets this calendar's dedicated event repository.
   *
   * @return The EventRepository for this calendar.
   */
  @Override
  public EventRepository getEventRepository() {
    return this.eventRepository;
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
    return String.format("Calendar{'%s', timezone=%s}", name, timezone.getId());
  }
}