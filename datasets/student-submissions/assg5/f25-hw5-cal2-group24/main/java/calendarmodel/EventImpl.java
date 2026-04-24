package calendarmodel;

import calendarmodel.enums.Location;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Concrete implementation of the Event interface.
 * This class is package-private and should not be used
 * directly outside the model package.
 *
 * <p>Use the Event.newBuilder() static factory methods.</p>
 */
final class EventImpl implements Event {

  private final String subject;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final String description;
  private final Location location;
  private final String status;
  private final String seriesId;

  /**
   * Private constructor. Can only be called by the Builder.
   *
   * @param builder The builder instance to construct from.
   */
  private EventImpl(Builder builder) {
    this.subject = builder.subject;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = builder.seriesId;
  }

  /**
   * Concrete implementation of the Event.Builder interface.
   * This class is package-private.
   */
  static class Builder implements Event.Builder {
    private String subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String description = null;
    private Location location = null;
    private String status = null;
    private String seriesId = null;

    /**
     * Builder constructor for required fields.
     *
     * @param subject   The event subject.
     * @param startTime The event start time.
     * @param endTime   The event end time.
     */
    Builder(String subject, LocalDateTime startTime, LocalDateTime endTime) {
      this.subject = subject;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    /**
     * "Copy constructor" for the builder.
     *
     * @param event The event to copy.
     */
    Builder(Event event) {
      this.subject = event.getSubject();
      this.startTime = event.getStartTime();
      this.endTime = event.getEndTime();
      this.description = event.getDescription();
      this.location = event.getLocation();
      this.status = event.getStatus();
      this.seriesId = event.getSeriesId();
    }

    @Override
    public Event.Builder withSubject(String subject) {
      this.subject = subject;
      return this;
    }

    @Override
    public Event.Builder withStartTime(LocalDateTime startTime) {
      this.startTime = startTime;
      return this;
    }

    @Override
    public Event.Builder withEndTime(LocalDateTime endTime) {
      this.endTime = endTime;
      return this;
    }

    @Override
    public Event.Builder withDescription(String description) {
      this.description = description;
      return this;
    }



    @Override
    public Event.Builder withLocation(Location location) {
      this.location = location;
      return this;
    }

    @Override
    public Event.Builder withStatus(String status) {
      this.status = status;
      return this;
    }

    @Override
    public Event.Builder withSeriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds and returns the immutable Event object.
     *
     * <p>All validation of event properties (e.g., non-null subject,
     * start/end times, and end not being before start) happens here.</p>
     *
     * @return The fully constructed, immutable Event instance.
     * @throws IllegalArgumentException if any validation fails.
     */
    @Override
    public Event build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty.");
      }
      if (startTime == null) {
        throw new IllegalArgumentException("Start time cannot be null.");
      }
      if (endTime == null) {
        throw new IllegalArgumentException("End time cannot be null.");
      }
      if (endTime.isBefore(startTime)) {
        throw new IllegalArgumentException("End time cannot be before start time.");
      }
      return new EventImpl(this);
    }
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartTime() {
    return startTime;
  }

  @Override
  public LocalDateTime getEndTime() {
    return endTime;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public boolean isPartOfSeries() {
    return seriesId != null;
  }

  /**
   * Defines the natural sorting order for Events.
   *
   * <p>Events are sorted primarily by their start time. If start times
   * are equal, they are sorted by end time. If end times are also
   * equal, they are sorted alphabetically by subject.</p>
   *
   * @param other The other Event to compare against.
   * @return A negative integer, zero, or a positive integer as this event
   *         is less than, equal to, or greater than the specified event.
   */
  @Override
  public int compareTo(Event other) {
    int startCompare = this.getStartTime().compareTo(other.getStartTime());
    if (startCompare != 0) {
      return startCompare;
    }
    int endCompare = this.getEndTime().compareTo(other.getEndTime());
    if (endCompare != 0) {
      return endCompare;
    }
    return this.getSubject().compareTo(other.getSubject());
  }

  /**
   * Defines equality for Event objects.
   *
   * <p>Two events are considered equal if they have the same
   * subject, start time, and end time. All other fields are
   * ignored for the purpose of equality checks (and hashing).</p>
   *
   * @param o The object to compare with.
   * @return true if the objects are equal based on key fields, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(getSubject(), event.getSubject())
        && Objects.equals(getStartTime(), event.getStartTime())
        && Objects.equals(getEndTime(), event.getEndTime());
  }

  /**
   * Generates a hash code for the Event.
   *
   * <p>The hash code is based only on the "key" fields:
   * subject, start time, and end time, consistent with the
   * {@code equals} method.</p>
   *
   * @return The hash code for this event.
   */
  @Override
  public int hashCode() {
    return Objects.hash(getSubject(), getStartTime(), getEndTime());
  }

  /**
   * Returns a concise string representation of the event.
   *
   * @return A string summary of the event.
   */
  @Override
  public String toString() {
    return "Event{"
        + "subject='" + subject + '\''
        + ", startTime=" + startTime
        + ", endTime=" + endTime
        + ", location=" + location
        + '}';
  }
}
