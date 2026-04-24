package calendar.model;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Builder for Event objects.
 * Provides a fluent and validated construction path.
 */
public class EventBuilder {

  private String subject;
  private Instant start;
  private Instant end;
  private String description;
  private String location;
  private boolean isPublic;
  private ZoneId zone;

  /**
   * Sets the subject.
   *
   * @param subject subject text
   * @return builder
   */
  public EventBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the start instant.
   *
   * @param start start instant
   * @return builder
   */
  public EventBuilder start(Instant start) {
    this.start = start;
    return this;
  }

  /**
   * Sets the end instant.
   *
   * @param end end instant
   * @return builder
   */
  public EventBuilder end(Instant end) {
    this.end = end;
    return this;
  }

  /**
   * Sets the description.
   *
   * @param description description text
   * @return builder
   */
  public EventBuilder description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the location.
   *
   * @param location location text
   * @return builder
   */
  public EventBuilder location(String location) {
    this.location = location;
    return this;
  }

  /**
   * Sets the visibility flag.
   *
   * @param isPublic true if public
   * @return builder
   */
  public EventBuilder isPublic(boolean isPublic) {
    this.isPublic = isPublic;
    return this;
  }

  /**
   * Sets the timezone.
   *
   * @param zone timezone
   * @return builder
   */
  public EventBuilder zone(ZoneId zone) {
    this.zone = zone;
    return this;
  }

  /**
   * Builds the Event.
   *
   * @return Event instance
   */
  public Event build() {
    return new Event(subject, start, end, description, location, isPublic, zone);
  }
}
