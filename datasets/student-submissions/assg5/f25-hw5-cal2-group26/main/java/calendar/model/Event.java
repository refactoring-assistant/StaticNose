package calendar.model;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of EventInterface. Stores start/end as ZonedDateTime, description,
 * location, status, and all-day flag. Handles all-day time adjustments internally.
 * Uses UUID for identity to support mutable fields without breaking HashMap contracts.
 */
public class Event implements EventInterface {

  private static final int ALL_DAY_START_HOUR = 8;
  private static final int ALL_DAY_END_HOUR = 17;

  private final String id;
  private String subject;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private String description;
  private String location;
  private EventStatus status;
  private boolean allDay;

  /**
   * Constructs a new Event with the specified details.
   *
   * @param subject the event's title or subject (cannot be null or empty).
   * @param start the start date and time (cannot be null).
   * @param end the end date and time (can be null for no end time).
   * @param description a brief description of the event (can be null).
   * @param location where the event takes place (can be null).
   * @param status the visibility status of the event (defaults to PUBLIC if null).
   * @param allDay true if this is an all-day event.
   *
   * @throws IllegalArgumentException if subject is null/empty or start is null.
   */
  public Event(String subject, ZonedDateTime start, ZonedDateTime end,
               String description, String location, EventStatus status,
               boolean allDay) {

    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject required");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start required");
    }

    this.id = UUID.randomUUID().toString();
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.status = (status != null) ? status : EventStatus.PUBLIC;
    this.allDay = allDay;

    if (allDay) {
      adjustAllDayTimes();
    }
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject required");
    }
    this.subject = subject;
  }

  @Override
  public ZonedDateTime getStart() {
    return start;
  }

  @Override
  public void setStart(ZonedDateTime start) {
    if (start == null) {
      throw new IllegalArgumentException("Start required");
    }
    this.start = start;
    if (allDay) {
      adjustAllDayTimes();
    }
  }

  @Override
  public ZonedDateTime getEnd() {
    return end;
  }

  @Override
  public void setEnd(ZonedDateTime end) {
    this.end = end;
    if (allDay) {
      adjustAllDayTimes();
    }
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public EventStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(EventStatus status) {
    this.status = status;
  }

  @Override
  public boolean isAllDay() {
    return allDay;
  }

  @Override
  public void setAllDay(boolean allDay) {
    this.allDay = allDay;
    if (allDay) {
      adjustAllDayTimes();
    }
  }

  private void adjustAllDayTimes() {
    this.start = start.toLocalDate().atTime(ALL_DAY_START_HOUR, 0).atZone(start.getZone());
    this.end = start.toLocalDate().atTime(ALL_DAY_END_HOUR, 0).atZone(start.getZone());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event e = (Event) o;
    return Objects.equals(id, e.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return subject + " (" + start + " - " + end + ")";
  }
}