package calendar.model;

import calendar.model.utils.EventStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single, non-recurring calendar event.
 */
public final class EventSingle extends AbstractEvent {
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Private constructor, accessible only by the internal Builder.
   */
  private EventSingle(String subject, LocalDateTime start, LocalDateTime end,
                      String description, String location, EventStatus status) {
    super(subject, description, location, status);
    this.start = start;
    this.end = end;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  /**
   * Verifies if an event is overlapping with another event.
   *
   * @param rangeStart start of other event.
   * @param rangeEnd   end of other event.
   * @return true/false if its overlaps or doesn't.
   */
  public boolean overlaps(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return this.start.isBefore(rangeEnd) && this.end.isAfter(rangeStart);
  }

  /**
   * Ensures a given event occurs after the start time and before
   * the end time.
   *
   * @param dateTime The time to check the occurrence of the event.
   * @return A boolean if the event at that occurs in between the start and end time.
   */
  public boolean occursAt(LocalDateTime dateTime) {
    return !dateTime.isBefore(this.start) && dateTime.isBefore(this.end);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventSingle that = (EventSingle) o;
    return subject.equals(that.subject)
        && start.equals(that.start)
        && end.equals(that.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }

  @Override
  public String toString() {
    return "Event{"
        + "subject='" + subject + '\''
        + ", start=" + start
        + ", end=" + end
        + '}';
  }


  /**
   * The public-facing class for constructing an EventSingle.
   */
  public static class Builder extends AbstractEvent.AbstractBuilder<Builder> {
    private LocalDateTime start;
    private LocalDateTime end;

    /**
     * Creates a new Builder for a new EventSingle.
     *
     * @param subject The event's subject (mandatory).
     * @param start   The event's start time (mandatory).
     */
    public Builder(String subject, LocalDateTime start) {
      super();
      this.withSubject(subject);
      this.start = start;
    }

    /**
     * Creates a new Builder based on an existing EventSingle.
     *
     * @param existingEvent The event to copy from.
     */
    public Builder(EventSingle existingEvent) {
      super();
      this.withSubject(existingEvent.subject);
      this.withDescription(existingEvent.description);
      this.withLocation(existingEvent.location);
      this.withStatus(existingEvent.status);

      this.start = existingEvent.start;
      this.end = existingEvent.end;
    }

    /**
     * Updates the start time of the event.
     *
     * @param start the start date and time
     * @return this builder for chaining
     */
    public Builder withStart(LocalDateTime start) {
      this.start = start;
      return this;
    }

    /**
     * Updates the end time of the event.
     *
     * @param end the end date and time
     * @return this builder for chaining
     */
    public Builder withEnd(LocalDateTime end) {
      this.end = end;
      return this;
    }

    /**
     * Validates the data and constructs the final, immutable EventSingle object.
     *
     * @return A new EventSingle instance.
     * @throws IllegalArgumentException if mandatory fields are null
     *                                  or if the end time is before the start time.
     */
    public EventSingle build() {
      if (start == null) {
        throw new IllegalArgumentException("Start time cannot be null.");
      }
      if (end == null) {
        throw new IllegalArgumentException("End time cannot be null.");
      }
      if (end.isBefore(start)) {
        throw new IllegalArgumentException("Event end time cannot be before its start time.");
      }

      return new EventSingle(subject, start, end, description, location, status);
    }
  }
}