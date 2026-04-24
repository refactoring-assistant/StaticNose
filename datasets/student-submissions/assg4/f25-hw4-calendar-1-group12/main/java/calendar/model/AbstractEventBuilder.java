package calendar.model;

import java.time.LocalDateTime;

/**
 * Abstract builder class for creating calendar events.
 * Provides common builder methods for setting event properties.
 *
 * @param <T> the type of the builder subclass for method chaining
 */
public class AbstractEventBuilder<T extends AbstractEventBuilder<T>> {
  protected String subject;
  protected LocalDateTime startDateTime;
  protected LocalDateTime endDateTime;
  protected String description = "";
  protected String location = "";
  protected Status status = Status.PUBLIC;

  /**
   * Sets the subject of the event.
   *
   * @param subject the event subject
   * @return this builder instance for method chaining
   */
  public T subject(String subject) {
    this.subject = subject;
    return (T) this;
  }

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the start date and time
   * @return this builder instance for method chaining
   */
  public T startDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
    return (T) this;
  }

  /**
   * Sets the end date and time of the event.
   *
   * @param endDateTime the end date and time
   * @return this builder instance for method chaining
   */
  public T endDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
    return (T) this;
  }

  /**
   * Sets the description of the event.
   *
   * @param description the event description
   * @return this builder instance for method chaining
   */
  public T description(String description) {
    this.description = description;
    return (T) this;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the event location
   * @return this builder instance for method chaining
   */
  public T location(String location) {
    this.location = location;
    return (T) this;
  }

  /**
   * Sets the status of the event.
   *
   * @param status the event status
   * @return this builder instance for method chaining
   */
  public T status(Status status) {
    this.status = status;
    return (T) this;
  }

  /**
   * Applies default all-day event times if end time is not specified.
   * Sets start time to 8:00 AM and end time to 5:00 PM.
   */
  protected void applyAllDayCheck() {
    if (endDateTime == null && startDateTime != null) {
      startDateTime = startDateTime.withHour(8).withMinute(0);
      endDateTime = startDateTime.withHour(17).withMinute(0);
    }
  }

  /**
   * Validates that required fields are set and constraints are met.
   *
   * @throws IllegalArgumentException if validation fails
   */
  protected void validate() {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("StartDateTime cannot be null");
    }
    if (endDateTime != null && !endDateTime.isAfter(startDateTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
  }
}