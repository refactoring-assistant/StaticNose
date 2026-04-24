package calendar.model;

import java.time.Instant;
import java.util.Objects;

/**
 * An implementation of the {@link Event} interface.
 */
public class CalendarEvent implements Event {
  /**
   * The subject or title of the event.
   */
  private String subject;
  /**
   * The exact start date and time of the event, in UTC.
   */
  private Instant start;
  /**
   * The exact end date and time of the event, in UTC.
   */
  private Instant end;
  /**
   * An optional description for the event.
   */
  private String description;
  /**
   * An optional location for the event.
   */
  private String location;
  /**
   * The privacy status of the event (true = private, false = public).
   */
  private boolean isPrivate;
  /**
   * A unique identifier for a series of recurring events.
   * Null if the event is not part of a series.
   */
  private String seriesId;

  /**
   * Constructs a new Event.
   *
   * @param subject     The subject of the event (cannot be null or blank).
   * @param start       The start date and time (cannot be null).
   * @param end         The end date and time (cannot be before start).
   * @param description An optional description.
   * @param location    An optional location.
   * @param isPrivate   The privacy status.
   * @param seriesId    The ID of the series this event belongs to (can be null).
   * @throws IllegalArgumentException if subject is invalid or end is before start.
   */
  public CalendarEvent(String subject, Instant start, Instant end,
               String description, String location, boolean isPrivate, String seriesId) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Subject cannot be null or empty.");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start date and time cannot be null.");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("Event end time cannot be before start time.");
    }
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.isPrivate = isPrivate;
    this.seriesId = seriesId;
  }

  /**
   * Gets the event's subject.
   *
   * @return The subject line.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the event's start date and time in UTC.
   *
   * @return The start {@link Instant}.
   */
  public Instant getStart() {
    return start;
  }

  /**
   * Gets the event's end date and time in UTC.
   *
   * @return The end {@link Instant}.
   */
  public Instant getEnd() {
    return end;
  }

  /**
   * Gets the event's description.
   *
   * @return The description string.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the event's location.
   *
   * @return The location string.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks the event's privacy status.
   *
   * @return true if the event is private, false otherwise.
   */
  public boolean isPrivate() {
    return isPrivate;
  }

  /**
   * Gets the event's series ID.
   *
   * @return The series ID, or null if not part of a series.
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Checks if this event is part of a series.
   *
   * @return true if the seriesId is not null, false otherwise.
   */
  public boolean isSeries() {
    return seriesId != null;
  }


  /**
   * Sets the event's subject.
   *
   * @param subject The new subject.
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Sets the event's start date and time in UTC.
   *
   * @param start The new start time.
   */
  public void setStart(Instant start) {
    this.start = start;
  }

  /**
   * Sets the event's end date and time in UTC.
   *
   * @param end The new end time.
   */
  public void setEnd(Instant end) {
    this.end = end;
  }

  /**
   * Sets the event's description.
   *
   * @param description The new description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the event's location.
   *
   * @param location The new location.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Sets the event's privacy status.
   *
   * @param isPrivate true to set as private, false for public.
   */
  public void setPrivate(boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  /**
   * Sets the event's series ID.
   *
   * @param seriesId The new series ID.
   */
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Creates a deep copy of this event.
   *
   * @return A new {@link Event} instance with the same data.
   */
  public Event copy() {
    return new CalendarEvent(this.subject, this.start, this.end,
        this.description, this.location, this.isPrivate, this.seriesId);
  }

  /**
   * Checks for equality based on subject, start, and end time.
   * This is used for conflict detection.
   *
   * @param o The object to compare with.
   * @return true if the subject, start, and end are identical, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CalendarEvent event = (CalendarEvent) o;
    return Objects.equals(subject, event.subject)
        && Objects.equals(start, event.start)
        && Objects.equals(end, event.end);
  }

  /**
   * Generates a hash code based on subject, start, and end time.
   *
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }

  /**
   * Returns a string representation of the event (in UTC).
   *
   * @return A formatted string (e.g., "Event{'Subject' from 2025-10-30T14:00:00Z to
   *          2025-10-30T15:00:00Z}").
   */
  @Override
  public String toString() {
    return String.format("Event{'%s' from %s to %s}", subject, start, end);
  }
}
