package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Implementation of a calendar event.
 */
public class EventImpl implements Event {
  private String subject;
  private String description;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String location;
  private EventStatus status;
  private String seriesId;
  private boolean isAllDay;

  /**
   * Creates a new event with required fields.
   *
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @param endDateTime   the end date/time
   * @throws IllegalArgumentException if any parameter is invalid
   */
  public EventImpl(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime)
      throws IllegalArgumentException {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time");
    }
    this.subject = subject.trim();
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.status = EventStatus.PUBLIC;
    this.isAllDay = false;
  }

  /**
   * Creates a new all-day event.
   *
   * @param subject the event subject
   * @param date    the date of the event
   * @throws IllegalArgumentException if any parameter is invalid
   */
  public EventImpl(String subject, LocalDateTime date) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    this.subject = subject.trim();
    this.startDateTime = date.withHour(8).withMinute(0).withSecond(0).withNano(0);
    this.endDateTime = date.withHour(17).withMinute(0).withSecond(0).withNano(0);
    this.status = EventStatus.PUBLIC;
    this.isAllDay = true;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    this.subject = subject.trim();
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDateTime != null && startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start date/time cannot be after end date/time");
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
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time");
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
  public EventStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(EventStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    this.status = status;
  }

  @Override
  public boolean isAllDay() {
    return isAllDay;
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
  public boolean conflictsWith(Event other) {
    if (other == null) {
      return false;
    }

    return this.subject.equals(other.getSubject())
        && this.startDateTime.equals(other.getStartDateTime())
        && this.endDateTime.equals(other.getEndDateTime());
  }

  @Override
  public boolean occursAt(LocalDateTime dateTime) {
    if (dateTime == null) {
      return false;
    }

    return !dateTime.isBefore(this.startDateTime) && dateTime.isBefore(this.endDateTime);
  }

  @Override
  public boolean occursInRange(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return false;
    }
    return this.startDateTime.isBefore(end) && start.isBefore(this.endDateTime);
  }

  @Override
  public Event copy() {
    EventImpl copy = new EventImpl(this.subject, this.startDateTime, this.endDateTime);
    copy.setDescription(this.description);
    copy.setLocation(this.location);
    copy.setStatus(this.status);
    copy.setSeriesId(this.seriesId);
    copy.isAllDay = this.isAllDay;
    return copy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventImpl event = (EventImpl) o;
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
    return String.format("Event{subject='%s', start=%s, end=%s, location='%s', allDay=%s}", subject,
        startDateTime, endDateTime, location, isAllDay);
  }
}