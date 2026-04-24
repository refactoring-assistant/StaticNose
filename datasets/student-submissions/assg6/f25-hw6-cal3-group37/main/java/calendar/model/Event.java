package calendar.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Implementation of CalendarEvent representing a single calendar event.
 * Immutable except through setter methods.
 */
public class Event implements CalendarEvent {
  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private String status;
  private String seriesId;

  // Constants for all-day events
  private static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime ALL_DAY_END = LocalTime.of(17, 0);

  /**
   * Constructs a new Event with required fields.
   *
   * @param subject the event subject
   * @param startDateTime the start date and time
   * @param endDateTime the end date and time
   * @throws IllegalArgumentException if subject is null/empty or datetimes are invalid
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Start and end datetimes cannot be null");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.status = "public";
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    this.subject = subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start datetime cannot be null");
    }
    if (endDateTime != null && startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    this.startDateTime = startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  @Override
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime == null) {
      throw new IllegalArgumentException("End datetime cannot be null");
    }
    if (startDateTime != null && endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End time must be after start time");
    }
    this.endDateTime = endDateTime;
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
  public String getStatus() {
    return status;
  }

  @Override
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean isAllDayEvent() {
    return startDateTime.toLocalTime().equals(ALL_DAY_START)
        && endDateTime.toLocalTime().equals(ALL_DAY_END);
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  @Override
  public CalendarEvent copy() {
    Event copy = new Event(this.subject, this.startDateTime, this.endDateTime);
    copy.setDescription(this.description);
    copy.setLocation(this.location);
    copy.setStatus(this.status);
    copy.setSeriesId(this.seriesId);
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
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
    return String.format("Event{subject='%s', start=%s, end=%s}",
        subject, startDateTime, endDateTime);
  }
}