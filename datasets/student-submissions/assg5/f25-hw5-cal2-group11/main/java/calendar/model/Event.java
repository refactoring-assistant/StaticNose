package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single calendar event.
 * Extends AbstractEvent with additional fields specific to single events.
 */
public class Event extends AbstractEvent {

  private String seriesId;
  private final boolean isAllDay;

  /**
   * Constructor for all day event.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time
   */
  public Event(String subject, LocalDateTime startDateTime) {
    super(subject, startDateTime, calculateDefaultEndTime(startDateTime, true));
    this.isAllDay = true;
    this.seriesId = null;
  }

  /**
   * Convenience constructor for all-day event using LocalDate.
   *
   * @param subject the subject of the event
   * @param date    the date of the all-day event
   */
  public Event(String subject, LocalDate date) {
    this(subject, date.atStartOfDay());
  }

  /**
   * Constructor for an event.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    super(subject, startDateTime,
        endDateTime != null ? endDateTime :
            calculateDefaultEndTime(startDateTime, endDateTime == null));

    this.isAllDay = (endDateTime == null);
    this.seriesId = null;
  }

  /**
   * Private constructor for Builder pattern.
   * Modified to automatically treat events with no end time as all-day events.
   *
   * @param builder the builder containing event properties
   */
  private Event(Builder builder) {
    super(builder.subject,
        builder.startDateTime,
        calculateEndTimeForBuilder(builder));

    this.isAllDay = (builder.endDateTime == null) || builder.isAllDay;
    this.seriesId = builder.seriesId;

    if (builder.location != null) {
      this.location = builder.location;
    }
    if (builder.description != null) {
      this.description = builder.description;
    }
    if (builder.status != null && !builder.status.equals("public")) {
      this.status = builder.status;
    }
  }

  /**
   * Helper method to calculate end time for builder constructor.
   *
   * @param builder the builder containing event properties
   * @return the calculated end time for the event
   */
  private static LocalDateTime calculateEndTimeForBuilder(Builder builder) {
    boolean isAllDayEvent = (builder.endDateTime == null) || builder.isAllDay;
    return builder.endDateTime != null
        ?
        builder.endDateTime :
        calculateDefaultEndTime(builder.startDateTime, isAllDayEvent);
  }

  /**
   * Calculates default end time based on start time and whether it's all-day.
   * Modified to provide better all-day event handling.
   *
   * @param start  the start date and time
   * @param allDay whether this is an all-day event
   * @return the calculated end time
   */
  private static LocalDateTime calculateDefaultEndTime(LocalDateTime start, boolean allDay) {
    if (allDay) {
      return start.withHour(17).withMinute(0).withSecond(0).withNano(0);
    } else {
      return start.plusHours(1);
    }
  }

  /**
   * Gets the series ID of this event.
   *
   * @return the series ID, or null if not part of a series
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Sets the series ID of this event.
   *
   * @param seriesId the series ID to set
   */
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  @Override
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Builder class for Event construction.
   * Now automatically treats events without end times as all-day events.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String description;
    private String status = "public";
    private String seriesId;
    private boolean isAllDay = false;

    /**
     * Create a builder with subject and start time.
     * If no end time is set later, this will be treated as an all-day event.
     *
     * @param subject       the event subject
     * @param startDateTime the start date and time
     * @throws IllegalArgumentException if subject is null/empty or startDateTime is null
     */
    public Builder(String subject, LocalDateTime startDateTime) {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }
      if (startDateTime == null) {
        throw new IllegalArgumentException("Start date/time cannot be null");
      }
      this.subject = subject;
      this.startDateTime = startDateTime;
    }

    /**
     * Set the end time. If this is not called, the event will be all-day.
     *
     * @param endDateTime the end date and time
     * @return this builder for method chaining
     * @throws IllegalArgumentException if end time is before start time
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      if (endDateTime != null && this.startDateTime.isAfter(endDateTime)) {
        throw new IllegalArgumentException("Start time must be before end time");
      }
      this.endDateTime = endDateTime;
      this.isAllDay = false;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param location the event location
     * @return this builder for method chaining
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the event description
     * @return this builder for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the status of the event.
     *
     * @param status the event status ("public" or "private")
     * @return this builder for method chaining
     * @throws IllegalArgumentException if status is not "public" or "private"
     */
    public Builder status(String status) {
      if (status != null && !status.equalsIgnoreCase("public")
          && !status.equalsIgnoreCase("private")) {
        throw new IllegalArgumentException("Status must be 'public' or 'private'");
      }
      this.status = status;
      return this;
    }

    /**
     * Sets the series ID of the event.
     *
     * @param seriesId the series ID
     * @return this builder for method chaining
     */
    public Builder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Explicitly mark this as an all-day event.
     * This will override any end time that was set.
     *
     * @return this builder for method chaining
     */
    public Builder allDay() {
      this.isAllDay = true;
      this.endDateTime = null;
      return this;
    }

    /**
     * Mark this as a timed event (not all-day).
     * Only useful if you want to ensure it's not all-day but haven't set end time yet.
     *
     * @return this builder for method chaining
     */
    public Builder timedEvent() {
      this.isAllDay = false;
      return this;
    }

    /**
     * Build the event. If no end time was set and allDay() wasn't called,
     * this will automatically create an all-day event.
     *
     * @return the constructed Event
     */
    public Event build() {
      return new Event(this);
    }

    /**
     * Creates a builder from an existing event.
     *
     * @param existingEvent the event to copy properties from
     * @return a new builder with the existing event's properties
     */
    public static Builder from(Event existingEvent) {
      Builder builder = new Builder(existingEvent.subject, existingEvent.startDateTime)
          .location(existingEvent.location)
          .description(existingEvent.description)
          .status(existingEvent.status)
          .seriesId(existingEvent.seriesId);

      if (existingEvent.isAllDay) {
        builder.allDay();
      } else {
        builder.endDateTime(existingEvent.endDateTime);
      }

      return builder;
    }
  }

  /**
   * Creates a builder from this event (for modifications).
   *
   * @return a new builder with this event's properties
   */
  public Builder toBuilder() {
    return Builder.from(this);
  }

  /**
   * Creates a copy with new start time, maintaining duration.
   *
   * @param newStartDateTime the new start date and time
   * @return a new Event with the updated time
   */
  public Event copyWithNewTime(LocalDateTime newStartDateTime) {
    if (this.isAllDay) {
      Event copy = new Event(this.subject, newStartDateTime);
      copy.setLocation(this.location);
      copy.setDescription(this.description);
      copy.setStatus(this.status);
      copy.setSeriesId(this.seriesId);
      return copy;
    } else {
      long durationMinutes = getDurationInMinutes();
      LocalDateTime newEndDateTime = newStartDateTime.plusMinutes(durationMinutes);

      Event copy = new Event(this.subject, newStartDateTime, newEndDateTime);
      copy.setLocation(this.location);
      copy.setDescription(this.description);
      copy.setStatus(this.status);
      copy.setSeriesId(this.seriesId);

      return copy;
    }
  }

  @Override
  public String toString() {
    return "Event{"
        + "subject='" + subject + '\''
        + ", startDateTime=" + startDateTime
        + ", endDateTime=" + endDateTime
        + ", location='" + location + '\''
        + ", isAllDay=" + isAllDay
        + ", seriesId='" + seriesId + '\''
        + '}';
  }
}