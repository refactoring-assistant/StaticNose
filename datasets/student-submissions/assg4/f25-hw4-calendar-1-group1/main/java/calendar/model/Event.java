package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Implementation of an event in a calendar.
 * This class is immutable to prevent unintended modifications.
 * Representation choice: Using LocalDateTime for precise time tracking,
 * and String fields for flexibility in text-based properties.
 * Class invariant: subject and start must never be null.
 */
public class Event implements Ievent {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final String status;
  private final String seriesId;

  /**
   * Constructs a new Event.
   *
   * @param subject     the subject (required, non-null)
   * @param start       the start time (required, non-null)
   * @param end         the end time (null for all-day events)
   * @param description the description (optional)
   * @param location    the location (optional)
   * @param status      the status (optional)
   * @param seriesId    the series ID if part of a series (optional)
   * @throws IllegalArgumentException if subject is null or empty, or start is null
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end,
               String description, String location, String status, String seriesId) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }

    this.subject = subject.trim();
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.status = status;
    this.seriesId = seriesId;
  }

  /**
   * Convenience constructor for events without a series ID.
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end,
               String description, String location, String status) {
    this(subject, start, end, description, location, status, null);
  }

  /**
   * Convenience constructor for basic events.
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end) {
    this(subject, start, end, null, null, null, null);
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStart() {
    return start;
  }

  @Override
  public LocalDateTime getEnd() {
    return end;
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
  public String getStatus() {
    return status;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public boolean isPartOfSeries() {
    return seriesId != null;
  }

  @Override
  public Ievent copyWith(String subject, LocalDateTime start, LocalDateTime end,
                         String description, String location, String status) {
    return new Event(
        subject != null ? subject : this.subject,
        start != null ? start : this.start,
        end != null ? end : this.end,
        description != null ? description : this.description,
        location != null ? location : this.location,
        status != null ? status : this.status,
        this.seriesId
    );
  }

  /**
   * Creates a copy with a new series ID.
   *
   * @param newSeriesId the new series ID
   * @return a new event with updated series ID
   */
  public Event withSeriesId(String newSeriesId) {
    return new Event(subject, start, end, description, location, status, newSeriesId);
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