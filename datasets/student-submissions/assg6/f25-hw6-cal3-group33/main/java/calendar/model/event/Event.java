package calendar.model.event;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Represents a calendar event with subject, start/end times, and optional properties.
 *
 * <p>REPRESENTATION CHOICE:
 * - Stores times as ZonedDateTime (America/New_York timezone) for consistency
 * - Subject, description, location are Strings for flexibility
 * - seriesId links events that are part of recurring series (null for standalone events)
 *
 * <p>WHY MUTABLE:
 * Events must be editable after creation (user can change subject, times, etc.).
 * However, certain setters are "Internal" (setSubjectInternal, setStartDateTimeInternal)
 * to enforce that only Calendar class can modify temporal/identity properties after
 * validation. This prevents breaking calendar invariants like duplicate detection.
 *
 * <p>CLASS INVARIANTS:
 * - subject is never null or empty
 * - startDateTime is always before endDateTime
 * - If seriesId != null, event must be single-day (start and end on same date)
 * - status is never null (defaults to PUBLIC)
 *
 * <p>CALENDAR-EVENT CONTRACT:
 * - Public setters (setDescription, setLocation, setStatus) can be called safely
 * - Internal setters (setSubjectInternal, etc.) bypass validation and must only be
 * called by Calendar class after it has validated the change won't break invariants
 * - setSeriesId() - only Calendar controls series membership
 */
public class Event implements EventInterface {

  // ==================== FIELDS ====================

  private String subject;
  private ZonedDateTime startDateTime;
  private ZonedDateTime endDateTime;
  private String description;
  private String location;
  private EventStatus status;
  private String seriesId;

  // ==================== CONSTRUCTORS ====================

  /**
   * Creates a new event with the specified subject, start time, and end time.
   *
   * @param subject       the event subject, must not be null or empty
   * @param startDateTime the start date and time, must not be null
   * @param endDateTime   the end date and time, must not be null and must be after start
   * @throws IllegalArgumentException if any parameter is invalid or start >= end
   */
  public Event(String subject, ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
    validateSubject(subject);
    validateTimes(startDateTime, endDateTime);

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = null;
    this.location = null;
    this.status = EventStatus.PUBLIC;
    this.seriesId = null;
  }

  // ==================== REQUIRED GETTERS ====================

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public ZonedDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public ZonedDateTime getEndDateTime() {
    return endDateTime;
  }

  // ==================== OPTIONAL GETTERS ====================

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public EventStatus getStatus() {
    return status;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  // ==================== PUBLIC SETTERS ====================

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public void setStatus(EventStatus status) {
    Objects.requireNonNull(status, "Status cannot be null");
    this.status = status;
  }

  // ==================== INTERNAL SETTERS (Calendar use only) ====================

  /**
   * Sets the subject. FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Bypasses duplicate checking - Calendar must validate before calling.
   *
   * @param subject the new subject, must not be null or empty
   * @throws IllegalArgumentException if subject is null or empty
   */
  @Override
  public void setSubjectInternal(String subject) {
    validateSubject(subject);
    this.subject = subject;
  }

  /**
   * Sets the start datetime. FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Does NOT validate temporal constraints - Calendar must validate before calling.
   *
   * @param startDateTime the new start datetime, must not be null
   * @throws NullPointerException if startDateTime is null
   */
  @Override
  public void setStartDateTimeInternal(ZonedDateTime startDateTime) {
    Objects.requireNonNull(startDateTime, "Start time cannot be null");
    this.startDateTime = startDateTime;
  }

  /**
   * Sets the end datetime. FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Does NOT validate temporal constraints - Calendar must validate before calling.
   *
   * @param endDateTime the new end datetime, must not be null
   * @throws NullPointerException if endDateTime is null
   */
  @Override
  public void setEndDateTimeInternal(ZonedDateTime endDateTime) {
    Objects.requireNonNull(endDateTime, "End time cannot be null");
    this.endDateTime = endDateTime;
  }

  // ==================== SERIES MANAGEMENT ====================

  /**
   * Removes this event from its series by setting seriesId to null.
   * FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Calendar is responsible for also updating eventsBySeriesId map.
   */
  @Override
  public void removeFromSeries() {
    this.seriesId = null;
  }

  /**
   * Sets the series ID for this event.
   * Only Calendar class should call this.
   *
   * @param seriesId the series UUID, or null to make standalone
   * @throws IllegalStateException if event is multi-day and being added to series
   */
  @Override
  public void setSeriesId(String seriesId) {
    if (seriesId != null && isMultiDay()) {
      throw new IllegalStateException(
          "Cannot add multi-day event to a series. Series events must be single-day.");
    }
    this.seriesId = seriesId;
  }

  // ==================== QUERY METHODS ====================

  /**
   * Checks if this event spans multiple days.
   *
   * @return true if start and end dates are different
   */
  @Override
  public boolean isMultiDay() {
    return !startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  // ==================== OBJECT METHODS ====================

  /**
   * Two events are equal if they have the same subject, start time, and end time.
   * Used for duplicate detection and set operations.
   *
   * <p>Note: seriesId is intentionally NOT included in equality comparison.
   * Two events with identical subject/times are considered duplicates
   * regardless of which series they belong to, per assignment requirements.
   *
   * @param o the object to compare
   * @return true if events have same subject, start, and end
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return subject.equals(event.subject)
        && startDateTime.equals(event.startDateTime)
        && endDateTime.equals(event.endDateTime);
  }

  /**
   * Hash code based on subject, start, and end times.
   * Consistent with equals() method.
   *
   * @return hash code for this event
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  // ==================== VALIDATION HELPERS ====================

  /**
   * Validates that the subject is not null or empty.
   */
  private void validateSubject(String subject) {
    Objects.requireNonNull(subject, "Subject cannot be null");
    if (subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }
  }

  /**
   * Validates that start time is before end time.
   */
  private void validateTimes(ZonedDateTime start, ZonedDateTime end) {
    Objects.requireNonNull(start, "Start time cannot be null");
    Objects.requireNonNull(end, "End time cannot be null");

    if (start.isAfter(end)) {
      throw new IllegalArgumentException(
          "Start time must be before end time. Start: " + start + ", End: " + end);
    }

    if (start.equals(end)) {
      throw new IllegalArgumentException(
          "Start time and end time cannot be the same");
    }
  }
}