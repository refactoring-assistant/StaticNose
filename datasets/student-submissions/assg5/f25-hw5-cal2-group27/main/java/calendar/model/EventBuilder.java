package calendar.model;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * A builder for creating immutable Event objects.
 */
public class EventBuilder {

  private UUID seriesId;
  private String subject;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private UUID id;
  private String description;
  private String location;
  private EventStatus status;
  private RecurrenceRule recurrence;

  /**
   * Sets the unique identifier for the event.
   *
   * @param id The unique ID to reuse.
   * @return this builder instance for chaining.
   */
  public EventBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Sets the series ID for the event. If not set, a new ID will be generated.
   *
   * @param seriesId The ID of the series this event belongs to.
   * @return this builder instance for chaining.
   */
  public EventBuilder seriesId(UUID seriesId) {
    this.seriesId = seriesId;
    return this;
  }

  /**
   * Sets the subject of the event.
   *
   * @param subject The event subject (required).
   * @return this builder instance for chaining.
   */
  public EventBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the start time of the event.
   *
   * @param start The event start time (required).
   * @return this builder instance for chaining.
   */
  public EventBuilder start(ZonedDateTime start) {
    this.start = start;
    return this;
  }

  /**
   * Sets the end time of the event.
   *
   * @param end The event end time (required).
   * @return this builder instance for chaining.
   */
  public EventBuilder end(ZonedDateTime end) {
    this.end = end;
    return this;
  }

  /**
   * Sets the optional description of the event.
   *
   * @param description The event description.
   * @return this builder instance for chaining.
   */
  public EventBuilder description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the optional location of the event.
   *
   * @param location The event location.
   * @return this builder instance for chaining.
   */
  public EventBuilder location(String location) {
    this.location = location;
    return this;
  }

  /**
   * Sets the optional status of the event.
   *
   * @param status The event status.
   * @return this builder instance for chaining.
   */
  public EventBuilder status(EventStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Sets the optional recurrence rule for the event.
   *
   * @param recurrence The event recurrence rule.
   * @return this builder instance for chaining.
   */
  public EventBuilder recurrence(RecurrenceRule recurrence) {
    this.recurrence = recurrence;
    return this;
  }

  /**
   * Builds the immutable Event object from the current state of the builder.
   *
   * @return A new, immutable Event instance.
   * @throws IllegalArgumentException if required fields (subject, start, end) are missing or
   *     invalid.
   */
  public Event build() {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Subject cannot be null or empty.");
    }
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end times cannot be null.");
    }
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be on or before the end time.");
    }

    UUID finalId = (this.id == null) ? UUID.randomUUID() : this.id;
    UUID finalSeriesId = (this.seriesId == null) ? finalId : this.seriesId;
    EventStatus finalStatus = (this.status == null) ? EventStatus.PUBLIC : this.status;
    return new Event(finalId, finalSeriesId, subject, start, end, description, location,
            finalStatus, recurrence);
  }
}
