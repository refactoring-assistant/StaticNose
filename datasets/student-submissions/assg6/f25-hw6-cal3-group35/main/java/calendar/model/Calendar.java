package calendar.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single calendar with a unique name and timezone.
 * Each calendar can contain multiple events.
 */
public class Calendar {

  private String name;
  private ZoneId timezone;
  private final CalendarService service;

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name unique name for the calendar
   * @param timezone timezone for this calendar (IANA format)
   * @throws IllegalArgumentException if name is blank or timezone is null
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be blank");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.name = name;
    this.timezone = timezone;
    this.service = new CalendarService();
  }

  /**
   * Gets the calendar name.
   *
   * @return calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets a new name for the calendar.
   *
   * @param newName new calendar name
   * @throws IllegalArgumentException if name is blank
   */
  public void setName(String newName) {
    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be blank");
    }
    this.name = newName;
  }

  /**
   * Gets the calendar timezone.
   *
   * @return timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets a new timezone for the calendar.
   *
   * @param newTimezone new timezone
   * @throws IllegalArgumentException if timezone is null
   */
  public void setTimezone(ZoneId newTimezone) {
    if (newTimezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    ZoneId oldTimezone = this.timezone;
    this.timezone = newTimezone;

    List<Event> allEvents = new ArrayList<>(service.getAllEvents());

    for (Event event : allEvents) {
      LocalDateTime oldStart = event.getStart();
      LocalDateTime oldEnd = event.getEnd();

      ZonedDateTime oldStartZoned = oldStart.atZone(oldTimezone);
      ZonedDateTime oldEndZoned = oldEnd.atZone(oldTimezone);

      ZonedDateTime newStartZoned = oldStartZoned.withZoneSameInstant(newTimezone);
      ZonedDateTime newEndZoned = oldEndZoned.withZoneSameInstant(newTimezone);

      LocalDateTime newStart = newStartZoned.toLocalDateTime();
      LocalDateTime newEnd = newEndZoned.toLocalDateTime();

      service.updateEventTime(event.getSubject(), oldStart, newStart, newEnd);
    }
  }

  /**
   * Gets the service that manages events for this calendar.
   *
   * @return calendar service
   */
  public CalendarService getService() {
    return service;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Calendar)) {
      return false;
    }
    Calendar calendar = (Calendar) o;
    return name.equals(calendar.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "Calendar{name='" + name + "', timezone=" + timezone + "}";
  }


}