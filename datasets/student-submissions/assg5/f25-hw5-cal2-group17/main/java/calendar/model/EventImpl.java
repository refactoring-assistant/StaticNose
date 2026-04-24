package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a single calendar event with required and optional properties.
 * Required: subject, start date/time
 * Optional: end date/time, description, location, status
 *
 * <p>Invariant: If end date/time is null, this is an all-day event (8am-5pm)
 * Invariant: startDateTime must not be null
 *
 * <p>Implementation note: This is a concrete implementation of Event interface.
 */
public class EventImpl implements Event {
  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;  // null for all-day events
  private String description;
  private String location;
  private boolean isPrivate;  // true = private, false = public
  private String seriesId;  // null if not part of a series


  /**
   * Constructor for a timed event with specific start and end times.
   *
   * @param subject       Subject of the Event as String
   * @param startDateTime Start date and time as Local Date Time
   * @param endDateTime   End date and time as Local Date Time
   */
  public EventImpl(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDateTime != null && endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time");
    }

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isPrivate = false;  // default to public
  }

  /**
   * Constructor for an all-day event (8am to 5pm).
   *
   * @param subject Subject of the Event as String
   * @param date    Date as Local Date
   */
  public EventImpl(String subject, LocalDate date) {
    this(subject,
        LocalDateTime.of(date, LocalTime.of(8, 0)),
        LocalDateTime.of(date, LocalTime.of(17, 0)));
  }

  // Getters
  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public boolean isPrivate() {
    return isPrivate;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public boolean isAllDay() {
    return endDateTime != null
        && startDateTime.toLocalTime().equals(LocalTime.of(8, 0))
        && endDateTime.toLocalTime().equals(LocalTime.of(17, 0))
        && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  // Setters
  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    this.subject = subject;
  }

  @Override
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (this.endDateTime != null && startDateTime.isAfter(this.endDateTime)) {
      throw new IllegalArgumentException("Start date/time cannot be after end date/time");
    }
    this.startDateTime = startDateTime;
  }

  @Override
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime != null && endDateTime.isBefore(this.startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time");
    }
    this.endDateTime = endDateTime;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public void setStatus(String status) {
    if (status != null) {
      this.isPrivate = status.equalsIgnoreCase("private");
    }
  }

  @Override
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Checks if this event conflicts with another event at a specific time.
   */
  @Override
  public boolean isScheduledAt(LocalDateTime dateTime) {
    if (endDateTime == null) {
      return startDateTime.equals(dateTime);
    }
    return !dateTime.isBefore(startDateTime) && dateTime.isBefore(endDateTime);
  }

  /**
   * Checks if this event occurs on a specific date.
   */
  @Override
  public boolean occursOnDate(LocalDate date) {
    if (endDateTime == null) {
      return startDateTime.toLocalDate().equals(date);
    }
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * Checks if this event overlaps with a date range.
   */
  @Override
  public boolean overlapsWithRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    LocalDateTime eventEnd = endDateTime != null ? endDateTime : startDateTime;
    return !eventEnd.isBefore(rangeStart) && !startDateTime.isAfter(rangeEnd);
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
    StringBuilder sb = new StringBuilder();
    sb.append(subject);
    sb.append(" starting on ").append(startDateTime.toLocalDate());
    sb.append(" at ").append(startDateTime.toLocalTime());
    if (endDateTime != null) {
      sb.append(", ending on ").append(endDateTime.toLocalDate());
      sb.append(" at ").append(endDateTime.toLocalTime());
    }
    if (location != null && !location.isEmpty()) {
      sb.append(" at ").append(location);
    }
    return sb.toString();
  }
}