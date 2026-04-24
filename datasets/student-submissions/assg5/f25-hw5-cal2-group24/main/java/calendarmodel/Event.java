package calendarmodel;

import calendarmodel.enums.Location;
import java.time.LocalDateTime;

/**
 * Public interface for an immutable Event.
 *
 * <p>Use the static factory methods (e.g., Event.newBuilder(...)) to
 * create new Builder instances.</p>
 */
public interface Event extends Comparable<Event> {

  /**
   * Creates a new Builder for a new Event.
   *
   * @param subject   The event subject.
   * @param startTime The event start time.
   * @param endTime   The event end time.
   * @return A new Event.Builder instance.
   */
  static Builder newBuilder(String subject, LocalDateTime startTime, LocalDateTime endTime) {
    return new EventImpl.Builder(subject, startTime, endTime);
  }

  /**
   * Creates a new Builder pre-populated with an existing event's data.
   *
   * <p>This is useful for creating a modified copy of an event
   * (e.g., for editing operations).</p>
   *
   * @param event The event to copy.
   * @return A new Event.Builder instance initialized with the event's data.
   */
  static Builder newBuilder(Event event) {
    return new EventImpl.Builder(event);
  }

  /**
   * Gets the subject (title) of the event.
   *
   * @return The event subject.
   */
  String getSubject();

  /**
   * Gets the date and time the event starts.
   *
   * @return The event start time.
   */
  LocalDateTime getStartTime();

  /**
   * Gets the date and time the event ends.
   *
   * @return The event end time.
   */
  LocalDateTime getEndTime();

  /**
   * Gets the description or notes for the event.
   *
   * @return The event description, or null if not set.
   */
  String getDescription();

  /**
   * Gets the location of the event (PHYSICAL or ONLINE).
   *
   * @return The Location enum, or null if not set.
   */
  Location getLocation();

  /**
   * Gets the status of the event (e.g., "Private", "Busy").
   *
   * @return The event status, or null if not set.
   */
  String getStatus();

  /**
   * Gets the unique identifier for the series this event belongs to.
   *
   * @return The series ID string, or null if this event is not part of a series.
   */
  String getSeriesId();

  /**
   * Checks if this event is part of a recurring series.
   *
   * @return true if the event has a series ID, false otherwise.
   */
  boolean isPartOfSeries();


  /**
   * Public interface for the Event builder, using a fluent API.
   */
  interface Builder {
    /**
     * Sets the subject for the event.
     *
     * @param subject The event subject.
     * @return This builder instance for chaining.
     */
    Builder withSubject(String subject);

    /**
     * Sets the start time for the event.
     *
     * @param startTime The event start time.
     * @return This builder instance for chaining.
     */
    Builder withStartTime(LocalDateTime startTime);

    /**
     * Sets the end time for the event.
     *
     * @param endTime The event end time.
     * @return This builder instance for chaining.
     */
    Builder withEndTime(LocalDateTime endTime);

    /**
     * Sets the description for the event.
     *
     * @param description The event description.
     * @return This builder instance for chaining.
     */
    Builder withDescription(String description);

    /**
     * Sets the location of the event.
     *
     * @param location The Location enum (PHYSICAL or ONLINE).
     * @return This builder instance for chaining.
     */
    Builder withLocation(Location location);

    /**
     * Sets the status for the event.
     *
     * @param status The event status.
     * @return This builder instance for chaining.
     */
    Builder withStatus(String status);

    /**
     * Sets the series ID for the event.
     *
     * @param seriesId The unique series identifier.
     * @return This builder instance for chaining.
     */
    Builder withSeriesId(String seriesId);

    /**
     * Builds the immutable Event object.
     *
     * @return The final, validated Event.
     * @throws IllegalArgumentException if validation fails (e.g., null subject).
     */
    Event build();
  }
}
