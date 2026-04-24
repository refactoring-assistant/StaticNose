package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for calendar events.
 * Contains common fields and methods shared by single events and event series.
 */
public abstract class AbstractEvent {

  protected String subject;
  protected LocalDateTime startDateTime;
  protected LocalDateTime endDateTime;
  protected String location;
  protected String description;
  protected String status;

  /**
   * Constructor for AbstractEvent.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   */
  protected AbstractEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    validateSubject(subject);
    validateDateTime(startDateTime, endDateTime);
    validateSingleDay(startDateTime, endDateTime);

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = "";
    this.location = null;
    this.status = "public";
  }

  /**
   * Validates the subject.
   */
  private void validateSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
  }

  /**
   * Validates the date and time.
   */
  private void validateDateTime(LocalDateTime start, LocalDateTime end) {
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
  }

  /**
   * Validates that events don't span multiple days.
   */
  private void validateSingleDay(LocalDateTime start, LocalDateTime end) {
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException("Events cannot span multiple days");
    }
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

  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is null or empty
   */

  public void setSubject(String subject) {
    validateSubject(subject);
    this.subject = subject;
  }

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the new start date and time
   * @throws IllegalArgumentException if startDateTime is null or after end time
   */

  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (this.endDateTime != null && startDateTime.isAfter(this.endDateTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    validateSingleDay(startDateTime, this.endDateTime);
    this.startDateTime = startDateTime;
  }

  /**
   * Sets the end date and time of the event.
   *
   * @param endDateTime the new end date and time
   * @throws IllegalArgumentException if endDateTime is null or before start time
   */

  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (this.startDateTime != null && this.startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    validateSingleDay(this.startDateTime, endDateTime);
    this.endDateTime = endDateTime;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the location where the event takes place
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Sets the description of the event.
   *
   * @param description the event description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the status of the event.
   *
   * @param status the event status ("public" or "private")
   * @throws IllegalArgumentException if status is not "public" or "private"
   */
  public void setStatus(String status) {
    if (status != null && !status.equalsIgnoreCase("public")
        &&
        !status.equalsIgnoreCase("private")) {
      throw new IllegalArgumentException("Status must be 'public' or 'private'");
    }
    this.status = status;
  }

  /**
   * Abstract method to check if this is an all-day event.
   *
   * @return true if all-day event, false otherwise
   */
  public abstract boolean isAllDay();

  /**
   * Calculates the duration of the event in minutes.
   *
   * @return duration in minutes
   */
  public long getDurationInMinutes() {
    return java.time.Duration.between(startDateTime, endDateTime).toMinutes();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractEvent that = (AbstractEvent) o;
    return Objects.equals(subject, that.subject)
        && Objects.equals(startDateTime, that.startDateTime)
        && Objects.equals(endDateTime, that.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{"
        + "subject='" + subject + '\''
        + ", startDateTime=" + startDateTime
        + ", endDateTime=" + endDateTime
        + ", location='" + location + '\''
        + '}';
  }
}