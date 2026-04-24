package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a calendar event with subject, start/end times, and other properties.
 */
public class Event {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private String description;
  private String location;
  private EventStatus status;
  private final String seriesId;

  /**
   * All-day event constructor.
   *
   * @param subject event subject
   * @param date event date
   */
  public Event(String subject, LocalDate date) {
    this(subject,
        LocalDateTime.of(date, LocalTime.of(8, 0)),
        LocalDateTime.of(date, LocalTime.of(17, 0)),
        null, null, EventStatus.PUBLIC, null);
  }

  /**
   * Regular event constructor.
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
               String description, String location, EventStatus status, String seriesId) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDateTime != null && endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time");
    }

    this.subject = subject.trim();
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime != null
        ? endDateTime
        : LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.of(17, 0));
    this.description = description;
    this.location = location;
    this.status = status != null ? status : EventStatus.PUBLIC;
    this.seriesId = seriesId;
  }

  /**
   * Gets the subject.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start date/time.
   *
   * @return the start date/time
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the end date/time.
   *
   * @return the end date/time
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Gets the series ID.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the location.
   *
   * @param location the location
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Sets the status.
   *
   * @param status the status
   */
  public void setStatus(EventStatus status) {
    this.status = status;
  }

  /**
   * Checks if this is an all-day event.
   *
   * @return true if all-day event
   */
  public boolean isAllDayEvent() {
    return startDateTime.toLocalTime().equals(LocalTime.of(8, 0))
        && endDateTime.toLocalTime().equals(LocalTime.of(17, 0))
        && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Checks if this event overlaps with another.
   *
   * @param other the other event
   * @return true if overlapping
   */
  public boolean overlapsWith(Event other) {
    return this.startDateTime.isBefore(other.endDateTime)
        && other.startDateTime.isBefore(this.endDateTime);
  }

  /**
   * Checks if this event conflicts with another.
   *
   * @param other the other event
   * @return true if conflicting
   */
  public boolean conflictsWith(Event other) {
    return this.subject.equals(other.subject)
        && this.startDateTime.equals(other.startDateTime)
        && this.endDateTime.equals(other.endDateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(subject, event.subject)
        && Objects.equals(startDateTime, event.startDateTime)
        && Objects.equals(endDateTime, event.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return String.format("Event{subject='%s', start=%s, end=%s, location=%s}",
        subject, startDateTime, endDateTime, location);
  }
}