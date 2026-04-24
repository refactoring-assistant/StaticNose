package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a single calendar event.
 */
public class CalendarEvent implements Comparable<CalendarEvent> {
  private final String id;
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String description;
  private String location;
  private EventStatus status;
  private String seriesId;
  private final boolean allDayPreferred;

  /**
   * Creates a new calendar event.
   *
   * @param subject     event subject
   * @param start       start date/time
   * @param end         end date/time
   * @param description optional description
   * @param location    optional location
   * @param status      status of event
   * @param seriesId    optional series identifier
   * @param allDay      whether the event was created as an all day event
   */
  public CalendarEvent(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      String description,
      String location,
      EventStatus status,
      String seriesId,
      boolean allDay) {
    this(UUID.randomUUID().toString(), subject, start, end, description, location, status, seriesId,
        allDay);
  }

  private CalendarEvent(
      String id,
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      String description,
      String location,
      EventStatus status,
      String seriesId,
      boolean allDay) {
    this.id = Objects.requireNonNull(id);
    setSubject(subject);
    setStart(start);
    setEnd(end);
    this.description = description;
    this.location = location;
    this.status = Objects.requireNonNull(status);
    this.seriesId = seriesId;
    this.allDayPreferred = allDay;
    validateDateOrder();
  }

  public String getId() {
    return id;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description).filter(s -> !s.isEmpty());
  }

  public Optional<String> getLocation() {
    return Optional.ofNullable(location).filter(s -> !s.isEmpty());
  }

  public EventStatus getStatus() {
    return status;
  }

  public Optional<String> getSeriesId() {
    return Optional.ofNullable(seriesId);
  }

  /**
   * Sets a new subject for this event like we would in the UI.
   *
   * @param subject new subject text
   * @throws IllegalArgumentException if the subject is empty
   */
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty.");
    }
    this.subject = subject.trim();
  }

  /**
   * Updates when the event begins.
   *
   * @param start start date and time
   * @throws NullPointerException if start is null
   * @throws IllegalArgumentException if the timing would be backwards
   */
  public void setStart(LocalDateTime start) {
    this.start = Objects.requireNonNull(start, "Start time cannot be null.");
    validateDateOrder();
  }

  /**
   * Adjusts when the event finishes up.
   *
   * @param end ending date and time
   * @throws NullPointerException if end is null
   * @throws IllegalArgumentException if the end would come before the start
   */
  public void setEnd(LocalDateTime end) {
    this.end = Objects.requireNonNull(end, "End time cannot be null.");
    validateDateOrder();
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setStatus(EventStatus status) {
    this.status = Objects.requireNonNull(status);
  }

  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  public boolean isAllDayPreferred() {
    return allDayPreferred;
  }

  /**
   * Returns the event duration.
   *
   * @return duration between start and end
   */
  public Duration duration() {
    return Duration.between(start, end);
  }

  /**
   * Determines if this event overlaps the supplied interval.
   *
   * @param start the inclusive start
   * @param end   the exclusive end
   * @return true if any overlap exists
   */
  public boolean overlaps(LocalDateTime start, LocalDateTime end) {
    return !this.start.isAfter(end) && !this.end.isBefore(start);
  }

  private void validateDateOrder() {
    if (start != null && end != null && !end.isAfter(start)) {
      throw new IllegalArgumentException("End time must be after start time.");
    }
  }

  /**
   * Determines whether this event is scheduled on a given date.
   *
   * @param date the date
   * @return true if the event overlaps any time on that date
   */
  public boolean occursOn(LocalDate date) {
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * Creates a defensive copy of the event instance.
   *
   * @return a deep copy
   */
  public CalendarEvent copy() {
    return new CalendarEvent(
        id,
        subject,
        start,
        end,
        description,
        location,
        status,
        seriesId,
        allDayPreferred);
  }

  @Override
  public int compareTo(CalendarEvent other) {
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

  @Override
  public String toString() {
    return "CalendarEvent{"
        + "subject='" + subject + '\''
        + ", start=" + start
        + ", end=" + end
        + ", description='" + description + '\''
        + ", location='" + location + '\''
        + ", status=" + status
        + ", seriesId='" + seriesId + '\''
        + ", allDayPreferred=" + allDayPreferred
        + '}';
  }
}
