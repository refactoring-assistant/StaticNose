package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a calendar event.
 */
public class Event {

  /**
   * The subject of the event.
   */
  String subject;
  /**
   * The start date and time of the event.
   */
  LocalDateTime startDateTime;
  /**
   * The end date and time of the event.
   */
  LocalDateTime endDateTime;
  String location;
  String description;
  String status;
  private String seriesId;
  private final boolean isAllDay;

  /**
   * Constructor for all day event.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start date and time of the event.
   */
  public Event(String subject, LocalDateTime startDateTime) {
    this(subject, startDateTime, null);
  }

  /**
   * Constructor for an event.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start date and time of the event.
   * @param endDateTime the end date and time of the event.
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.subject = subject;

    if (endDateTime == null) {
      this.startDateTime = startDateTime.withHour(8).withMinute(0).withSecond(0).withNano(0);
      this.endDateTime = startDateTime.withHour(17).withMinute(0).withSecond(0).withNano(0);
      this.isAllDay = true;
    } else {
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
      this.isAllDay = false;

      if (this.startDateTime.isAfter(this.endDateTime)) {
        throw new IllegalArgumentException("Start time must be before end time");
      }
    }

    this.description = "";
    this.location = null;
    this.status = "public";
    this.seriesId = null;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public String getSeriesId() {
    return seriesId;
  }

  public boolean isAllDay() {
    return isAllDay;
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
        &&
        Objects.equals(startDateTime, event.startDateTime)
        &&
        Objects.equals(endDateTime, event.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return "Event{"
        +
        "subject='" + subject + '\''
        +
        ", startDateTime=" + startDateTime
        +
        ", endDateTime=" + endDateTime
        +
        '}';
  }
}