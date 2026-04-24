package calendar.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Builder for creating events with optional properties.
 * Follows the Builder pattern to avoid telescoping constructors.
 */
public class EventBuilder {
  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private EventStatus status = EventStatus.PUBLIC;

  /**
   * Sets the event subject.
   *
   * @param subject the event subject
   * @return this builder for method chaining
   */
  public EventBuilder setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the event start date and time.
   *
   * @param startDateTime the start date/time
   * @return this builder for method chaining
   */
  public EventBuilder setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  /**
   * Sets the event end date and time.
   *
   * @param endDateTime the end date/time (null for all-day events)
   * @return this builder for method chaining
   */
  public EventBuilder setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  /**
   * Sets the event description.
   *
   * @param description the event description
   * @return this builder for method chaining
   */
  public EventBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the event location.
   *
   * @param location the event location
   * @return this builder for method chaining
   */
  public EventBuilder setLocation(String location) {
    this.location = location;
    return this;
  }

  /**
   * Sets the event privacy status.
   *
   * @param status the privacy status (PUBLIC or PRIVATE)
   * @return this builder for method chaining
   */
  public EventBuilder setStatus(EventStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Populates builder fields from a map of optional properties.
   * Handles null or invalid values gracefully by using defaults.
   *
   * @param props map containing optional properties (description, location, status)
   * @return this builder for method chaining
   */
  public EventBuilder fromOptionalProps(Map<String, String> props) {
    if (props == null) {
      return this;
    }

    String descriptionValue = props.get("description");
    if (descriptionValue != null && !descriptionValue.trim().isEmpty()) {
      this.description = descriptionValue;
    }

    String locationValue = props.get("location");
    if (locationValue != null && !locationValue.trim().isEmpty()) {
      this.location = locationValue;
    }

    String statusValue = props.get("status");
    if (statusValue != null && !statusValue.trim().isEmpty()) {
      try {
        this.status = EventStatus.valueOf(statusValue.toUpperCase());
      } catch (IllegalArgumentException e) {
        this.status = EventStatus.PUBLIC;
      }
    }

    return this;
  }

  /**
   * Builds a single (non-recurring) event with the configured properties.
   *
   * @return the constructed SingleEvent
   * @throws IllegalStateException if required fields (subject, start) are not set
   */
  public SingleEvent buildSingleEvent() {
    if (subject == null || startDateTime == null) {
      throw new IllegalStateException(
          "Subject and start date/time are required");
    }
    return new SingleEvent(
        subject, startDateTime, endDateTime, description, location, status);
  }

  /**
   * Creates a new EventBuilder instance.
   * Static factory method for cleaner syntax.
   *
   * @return a new EventBuilder
   */
  public static EventBuilder create() {
    return new EventBuilder();
  }
}