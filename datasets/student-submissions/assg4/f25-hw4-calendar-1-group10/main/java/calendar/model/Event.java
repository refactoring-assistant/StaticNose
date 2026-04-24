package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a single event.
 * Implements Comparable to allow natural chronological ordering.
 */
public class Event implements Comparable<Event> {

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final String status;
  private final String seriesId;

  /**
   * Creates a new event.
   *
   * @param subject     the subject of the event
   * @param start       the start time of the event
   * @param end         the end time of the event
   * @param description the description of the event
   * @param location    the location of the event
   * @param status      the status of the event
   * @param seriesId    the ID of the series this event belongs to
   */
  public Event(String subject,
               LocalDateTime start,
               LocalDateTime end,
               String description,
               String location,
               String status,
               String seriesId) {

    if (subject == null || start == null) {
      throw new IllegalArgumentException("Subject and start time are required.");
    }

    if (end == null) {
      start = start.withHour(8).withMinute(0);
      end = start.withHour(17).withMinute(0);
    }

    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time cannot be before start time.");
    }

    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.status = status;
    this.seriesId = seriesId;
  }

  /**
   * Creates an all-day event.
   *
   * @param subject     the subject of the event
   * @param date        the date of the event
   * @param description the description of the event
   * @param location    the location of the event
   * @param status      the status of the event
   * @param seriesId    the ID of the series this event belongs to
   * @return the new event
   */
  public static Event allDay(String subject, LocalDate date,
                             String description, String location,
                             String status, String seriesId) {
    LocalDateTime start = date.atTime(LocalTime.of(8, 0));
    LocalDateTime end = date.atTime(LocalTime.of(17, 0));
    return new Event(subject, start, end, description, location, status, seriesId);
  }

  /**
   * Gets the subject of the event.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start times
   */
  public LocalDateTime getStart() {
    return start;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end times
   */
  public LocalDateTime getEnd() {
    return end;
  }

  /**
   * Gets the description of the event.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the status of the event.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Gets the ID of the series this event belongs to.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Checks if the event occurs on a given date.
   *
   * @param date the date to check
   * @return whether the event occurs on the given date
   */
  public boolean occursOn(LocalDate date) {
    return !date.isBefore(start.toLocalDate()) && !date.isAfter(end.toLocalDate());
  }

  /**
   * Checks if the event overlaps with a given time range.
   *
   * @param rangeStart the start of the time range
   * @param rangeEnd   the end of the time range
   * @return whether the event overlaps with the given time range
   */
  public boolean overlaps(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return !end.isBefore(rangeStart) && !start.isAfter(rangeEnd);
  }

  /**
   * Checks if the event contains a given time.
   *
   * @param time the time to check
   * @return whether the event contains the given time
   */
  public boolean contains(LocalDateTime time) {
    return !time.isBefore(start) && !time.isAfter(end);
  }

  /**
   * Edits a property of the event.
   *
   * @param property the property to edit
   * @param newValue the new value of the property
   * @return the new event
   * @throws IllegalArgumentException if the property is not recognized
   */
  public Event editProperty(String property, String newValue) {
    String p = property.toLowerCase();
    if ("subject".equals(p)) {
      return new Event(newValue, start, end, description, location, status, seriesId);
    } else if ("start".equals(p)) {
      return new Event(subject, LocalDateTime.parse(newValue), end, description, location, status,
          seriesId);
    } else if ("end".equals(p)) {
      return new Event(subject, start, LocalDateTime.parse(newValue), description, location, status,
          seriesId);
    } else if ("description".equals(p)) {
      return new Event(subject, start, end, newValue, location, status, seriesId);
    } else if ("location".equals(p)) {
      return new Event(subject, start, end, description, newValue, status, seriesId);
    } else if ("status".equals(p)) {
      return new Event(subject, start, end, description, location, newValue, seriesId);
    }
    throw new IllegalArgumentException("Unknown property: " + property);
  }

  /**
   * Compares two events chronologically.
   *
   * @param other the other event to compare to
   * @return -1 if this event occurs before the other, 0 if they are equal, 1 if this event
   *         occurs after the other
   */
  @Override
  public int compareTo(Event other) {
    int cmp = this.start.compareTo(other.start);
    if (cmp != 0) {
      return cmp;
    }
    cmp = this.end.compareTo(other.end);
    if (cmp != 0) {
      return cmp;
    }
    return this.subject.compareToIgnoreCase(other.subject);
  }

  /**
   * Checks if two events are equal.
   *
   * @param obj the object to compare to
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Event)) {
      return false;
    }
    Event other = (Event) obj;
    return subject.equalsIgnoreCase(other.subject)
        && start.equals(other.start)
        && end.equals(other.end);
  }

  /**
   * Calculates the hash code for the event.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject.toLowerCase(), start, end);
  }

  /**
   * Returns a string representation of the event.
   *
   * @return the string representation
   */
  @Override
  public String toString() {
    return subject + " from " + start + " to " + end
        + (location != null ? " at " + location : "");
  }
}
