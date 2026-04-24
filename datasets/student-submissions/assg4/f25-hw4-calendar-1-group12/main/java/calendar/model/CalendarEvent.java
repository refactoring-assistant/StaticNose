package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single calendar event with required and optional properties.
 * Events are immutable and created using the Builder pattern.
 */
public class CalendarEvent implements Event {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final Status status;
  private final String seriesId;
  private static final int ALL_DAY_START_HOUR = 8;
  private static final int ALL_DAY_START_MINUTE = 0;
  private static final int ALL_DAY_END_HOUR = 17;
  private static final int ALL_DAY_END_MINUTE = 0;

  /**
   * Private constructor accessible via Builder.
   *
   * @param builder the builder object used to construct the event
   */
  private CalendarEvent(EventBuilder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = builder.seriesId;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
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
  public Status getStatus() {
    return status;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public Boolean isAllDay() {
    return startDateTime.getHour() == ALL_DAY_START_HOUR
        && startDateTime.getMinute() == ALL_DAY_START_MINUTE
        && endDateTime.getHour() == ALL_DAY_END_HOUR
        && endDateTime.getMinute() == ALL_DAY_END_MINUTE
        && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Determines whether two events are equal based on their subject, startDateTime, and endDateTime.
   *
   * @param other the object to compare with this event
   * @return true if the events have the same subject, start, and end time; false otherwise
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CalendarEvent)) {
      return false;
    }
    CalendarEvent otherEvent = (CalendarEvent) other;
    return Objects.equals(subject, otherEvent.getSubject())
        && Objects.equals(startDateTime, otherEvent.getStartDateTime())
        && Objects.equals(endDateTime, otherEvent.getEndDateTime());
  }

  /**
   * Computes the hash code for the event based on its subject, startDateTime, and endDateTime.
   *
   * @return a hash code for the event
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  /**
   * Provides a string representation of the event, including subject, start, and end time.
   *
   * @return a string representing the event
   */
  @Override
  public String toString() {
    return String.format("Event{subject='%s', start=%s, end=%s}",
        subject, startDateTime, endDateTime);
  }

  /**
   * Builder class for creating Event instances.
   */
  public static class EventBuilder extends AbstractEventBuilder<EventBuilder> {
    private String seriesId = null;

    /**
     * Sets the series ID for this event.
     * If the event is part of a recurring series, a series ID can be assigned here.
     *
     * @param seriesId the series ID if this event is part of a recurring series, or null if not
     * @return this builder instance for method chaining
     */
    public EventBuilder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds and validates the Event object.
     *
     * @return a new CalendarEvent instance created using the builder
     * @throws IllegalArgumentException if the event does not pass validation (e.g., invalid data)
     */
    public CalendarEvent build() {
      applyAllDayCheck();
      validate();
      return new CalendarEvent(this);
    }
  }
}
