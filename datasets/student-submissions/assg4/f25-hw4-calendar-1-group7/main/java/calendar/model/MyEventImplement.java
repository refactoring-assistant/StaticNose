package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Implementation of the Event interface.
 * Represents a calendar event with required and optional properties.
 */
public class MyEventImplement implements Event {
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String location;
  private String description;
  private String status;
  private String seriesId;
  private boolean isAllDay;

  /**
   * Creates a new event with required properties.
   *
   * @param subject the event subject (required)
   * @param start the start date/time (required)
   * @param end the end date/time (required)
   */
  public MyEventImplement(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("End time cannot be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    this.subject = subject;
    this.start = start;
    this.end = end;
    this.status = "public";
    this.isAllDay = false;
  }

  /**
   * Creates an all-day event (8am to 5pm).
   *
   * @param subject the event subject
   * @param start the start date/time (time will be set to 8am)
   */
  public MyEventImplement(String subject, LocalDateTime start) {
    this(subject,
        start.withHour(8).withMinute(0).withSecond(0).withNano(0),
        start.withHour(17).withMinute(0).withSecond(0).withNano(0));
    this.isAllDay = true;
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
  public LocalDateTime getStart() {
    return start;
  }

  @Override
  public void setStart(LocalDateTime start) {
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    if (end != null && start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    this.start = start;
  }

  @Override
  public LocalDateTime getEnd() {
    return end;
  }

  @Override
  public void setEnd(LocalDateTime end) {
    if (end == null) {
      throw new IllegalArgumentException("End time cannot be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End time must be after start time");
    }
    this.end = end;
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
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
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
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  @Override
  public boolean isAllDay() {
    return isAllDay;
  }

  @Override
  public Event copy() {
    MyEventImplement copy = new MyEventImplement(this.subject, this.start, this.end);
    copy.setLocation(this.location);
    copy.setDescription(this.description);
    copy.setStatus(this.status);
    copy.setSeriesId(this.seriesId);
    copy.isAllDay = this.isAllDay;
    return copy;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    MyEventImplement event = (MyEventImplement) obj;
    return Objects.equals(subject, event.subject)
        && Objects.equals(start, event.start)
        && Objects.equals(end, event.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }

  @Override
  public String toString() {
    return String.format("Event{subject='%s', start=%s, end=%s, location='%s'}",
        subject, start, end, location);
  }
}