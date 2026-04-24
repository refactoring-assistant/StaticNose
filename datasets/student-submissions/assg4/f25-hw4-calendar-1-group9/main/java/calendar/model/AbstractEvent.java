package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Abstract base class providing common functionality for all event types.
 * Implements shared behavior to avoid code duplication (DRY principle).
 * Subclasses must implement validateEventConstraints for type-specific validation.
 */
public abstract class AbstractEvent implements InEvent {

  private static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
  private static final LocalTime ALL_DAY_END = LocalTime.of(17, 0);

  protected String subject;
  protected LocalDateTime startDateTime;
  protected LocalDateTime endDateTime;
  protected String description;
  protected String location;
  protected EventStatus status;
  protected boolean isAllDay;

  /**
   * Constructs an AbstractEvent with required and optional fields.
   *
   * @param subject       the event subject (required)
   * @param startDateTime the start date/time (required)
   * @param endDateTime   the end date/time (null for all-day events)
   * @param description   optional event description
   * @param location      optional event location
   * @param status        the privacy status (defaults to PUBLIC if null)
   */
  protected AbstractEvent(String subject, LocalDateTime startDateTime,
                          LocalDateTime endDateTime, String description,
                          String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.subject = subject.trim();
    this.startDateTime = startDateTime;
    this.description = description;
    this.location = location;
    this.status = (status == null) ? EventStatus.PUBLIC : status;

    if (endDateTime == null) {
      this.isAllDay = true;
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(), ALL_DAY_END);
      this.startDateTime = LocalDateTime.of(startDateTime.toLocalDate(), ALL_DAY_START);
    } else {
      this.isAllDay = false;
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("End date/time cannot be before start date/time");
      }
      this.endDateTime = endDateTime;
    }

    validateEventConstraints();
  }

  /**
   * Template method for subclass-specific validation.
   * Subclasses override this to enforce their own constraints.
   */
  protected abstract void validateEventConstraints();

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
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
      throw new IllegalArgumentException("Start cannot be after end");
    }
    this.startDateTime = startDateTime;
    validateEventConstraints();
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
      throw new IllegalArgumentException("End cannot be before start");
    }
    this.endDateTime = endDateTime;
    this.isAllDay = false;
    validateEventConstraints();
  }

  @Override
  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
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
    this.status = (status == null) ? EventStatus.PUBLIC : status;
  }

  @Override
  public boolean isAllDayEvent() {
    return isAllDay;
  }

  @Override
  public boolean conflictsWith(InEvent other) {
    if (other == null) {
      return false;
    }
    return !this.endDateTime.isBefore(other.getStartDateTime())
        && !this.startDateTime.isAfter(other.getEndDateTime());
  }

  @Override
  public boolean occursOn(LocalDate date) {
    if (date == null) {
      return false;
    }
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  @Override
  public boolean occursBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return false;
    }
    return !this.endDateTime.isBefore(start) && !this.startDateTime.isAfter(end);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InEvent)) {
      return false;
    }
    InEvent other = (InEvent) obj;
    return this.subject.equals(other.getSubject())
        && this.startDateTime.equals(other.getStartDateTime())
        && this.endDateTime.equals(other.getEndDateTime());
  }

  @Override
  public int hashCode() {
    return subject.hashCode() + startDateTime.hashCode() + endDateTime.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s from %s to %s", subject, startDateTime, endDateTime);
  }
}