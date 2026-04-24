package calendar.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Represents a calendar event with timezone support.
 * Uses Builder pattern for object construction.
 */
public class Event {
  private static final int ALL_DAY_START_HOUR = 8;
  private static final int ALL_DAY_END_HOUR = 17;
  private String subject;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private String description;
  private String location;
  private boolean isPublic;
  private String seriesId;

  /**
   * Creates an event with required fields only.
   *
   * @param subject the event subject
   * @param start the event start time
   * @throws IllegalArgumentException if subject or start is invalid
   */
  public Event(String subject, ZonedDateTime start) {
    this(new Builder(subject, start));
  }

  /**
   * Private constructor used by Builder.
   *
   * @param builder the builder containing event data
   * @throws IllegalArgumentException if validation fails
   */
  private Event(Builder builder) {
    validateSubject(builder.subject);
    validateStart(builder.start);
    if (builder.end != null && builder.end.isBefore(builder.start)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.description = builder.description;
    this.location = builder.location;
    this.isPublic = builder.isPublic;
    this.seriesId = builder.seriesId;
  }

  /**
   * Validates subject is not null or empty.
   *
   * @param subject the subject to validate
   * @throws IllegalArgumentException if subject is null or empty
   */
  private void validateSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
  }

  /**
   * Validates start time is not null.
   *
   * @param start the start time to validate
   * @throws IllegalArgumentException if start is null
   */
  private void validateStart(ZonedDateTime start) {
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
  }

  /**
   * Returns true if this event is part of a recurring series.
   *
   * @return true if event has series identifier
   */
  public boolean isPartOfSeries() {
    return seriesId != null;
  }

  /**
   * Returns true if this is an all-day event.
   *
   * @return true if event spans 8am to 5pm
   */
  public boolean isAllDay() {
    return start.toLocalTime().equals(LocalTime.of(ALL_DAY_START_HOUR, 0))
        && end.toLocalTime().equals(LocalTime.of(ALL_DAY_END_HOUR, 0));
  }

  /**
   * Checks if this event overlaps with the given time range.
   *
   * @param rangeStart the range start time
   * @param rangeEnd the range end time
   * @return true if any overlap exists
   */
  public boolean overlaps(ZonedDateTime rangeStart, ZonedDateTime rangeEnd) {
    return start.toInstant().isBefore(rangeEnd.toInstant())
        && end.toInstant().isAfter(rangeStart.toInstant());
  }

  /**
   * Checks if the given time falls within this event.
   *
   * @param time the time to check
   * @return true if time is within event bounds
   */
  public boolean contains(ZonedDateTime time) {
    return !time.toInstant().isBefore(start.toInstant())
        && time.toInstant().isBefore(end.toInstant());
  }

  /**
   * Creates a copy of this event in a different timezone.
   *
   * @param targetZone the target timezone
   * @return new event in target timezone
   */
  public Event copyToTimezone(ZoneId targetZone) {
    return new Event.Builder(this.subject, this.start.withZoneSameInstant(targetZone))
        .end(this.end != null ? this.end.withZoneSameInstant(targetZone) : null)
        .description(this.description)
        .location(this.location)
        .isPublic(this.isPublic)
        .seriesId(this.seriesId)
        .build();
  }

  /**
   * Gets the duration of this event.
   *
   * @return duration between start and end
   */
  public Duration getDuration() {
    return Duration.between(start, end);
  }

  /**
   * Gets the event subject.
   *
   * @return the event subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the event subject.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is invalid
   */
  public void setSubject(String subject) {
    validateSubject(subject);
    this.subject = subject;
  }

  /**
   * Gets the event start time.
   *
   * @return the start time
   */
  public ZonedDateTime getStart() {
    return start;
  }

  /**
   * Sets the event start time.
   *
   * @param start the new start time
   * @throws IllegalArgumentException if start is null
   */
  public void setStart(ZonedDateTime start) {
    validateStart(start);
    this.start = start;
  }

  /**
   * Gets the event end time.
   *
   * @return the end time
   */
  public ZonedDateTime getEnd() {
    return end;
  }

  /**
   * Sets the event end time.
   *
   * @param end the new end time
   */
  public void setEnd(ZonedDateTime end) {
    this.end = end;
  }

  /**
   * Gets the event description.
   *
   * @return the description or null
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the event description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the event location.
   *
   * @return the location or null
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the event location.
   *
   * @param location the new location
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Gets whether event is public.
   *
   * @return true if public, false if private
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Sets whether event is public.
   *
   * @param isPublic true for public, false for private
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Gets the series identifier.
   *
   * @return the series ID or null if not part of series
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Sets the series identifier.
   *
   * @param seriesId the new series ID
   */
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  @Override
  public String toString() {
    return String.format("%s from %s to %s", subject, start, end);
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

  /**
   * Builder for creating Event objects with optional fields.
   */
  public static class Builder {
    private final String subject;
    private final ZonedDateTime start;
    private ZonedDateTime end;
    private String description;
    private String location;
    private boolean isPublic = true;
    private String seriesId;

    /**
     * Creates builder with required fields.
     *
     * @param subject the event subject
     * @param start the event start time
     */
    public Builder(String subject, ZonedDateTime start) {
      this.subject = subject;
      this.start = start;
    }

    /**
     * Sets the end time.
     *
     * @param end the end time
     * @return this builder
     */
    public Builder end(ZonedDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Sets the description.
     *
     * @param description the event description
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location the event location
     * @return this builder
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets whether the event is public.
     *
     * @param isPublic true for public, false for private
     * @return this builder
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Sets the series identifier.
     *
     * @param seriesId the series ID
     * @return this builder
     */
    public Builder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds the Event object.
     *
     * @return the constructed event
     * @throws IllegalArgumentException if validation fails
     */
    public Event build() {
      return new Event(this);
    }
  }
}