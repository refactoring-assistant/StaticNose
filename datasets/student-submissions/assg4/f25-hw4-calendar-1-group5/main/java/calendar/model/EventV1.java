package calendar.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents an event stored by the first version of the calendar.
 */
public class EventV1 implements Event {
  private final UUID seriesId;
  private final String subject;
  private final String description;
  private final LocalDateTime startsAt;
  private final LocalDateTime endsAt;
  private final Location location;
  private final Status status;
  private final int hashCode;

  /**
   * Initializes an event with the given parameters.
   *
   * @param subject String event subject.
   * @param description String event description.
   * @param startsAt LocalDateTime of the event start timestamp.
   * @param endsAt LocalDateTime of the event end timestamp.
   * @param location location of event.
   * @param status status of event.
   * @param seriesId ID of the series this event belongs to.
   */
  private EventV1(String subject, String description, LocalDateTime startsAt, LocalDateTime endsAt,
                  Location location, Status status, UUID seriesId) {
    this.seriesId = Objects.requireNonNull(seriesId);
    this.subject = Objects.requireNonNull(subject);
    this.description = description;
    this.startsAt = Objects.requireNonNull(startsAt);
    this.endsAt = Objects.requireNonNull(endsAt);
    this.location = location;
    this.status = status;
    this.hashCode = computeHashCode();

    if (endsAt.isBefore(startsAt)) {
      throw new IllegalArgumentException("end date must be before start date");
    }
  }

  @Override
  public UUID seriesId() {
    return this.seriesId;
  }

  @Override
  public String subject() {
    return this.subject;
  }

  @Override
  public String description() {
    return this.description;
  }

  @Override
  public LocalDateTime startsAt() {
    return this.startsAt;
  }

  @Override
  public LocalDateTime endsAt() {
    return this.endsAt;
  }

  @Override
  public Location location() {
    return this.location;
  }

  @Override
  public Status status() {
    return this.status;
  }

  @Override
  public boolean matches(String subject, LocalDateTime startsAt, LocalDateTime endsAt) {
    return this.subject.equals(subject) && this.startsAt.equals(startsAt)
        && this.endsAt.equals(endsAt);
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    if (this == o) {
      return true;
    }

    if (!(o instanceof Event)) {
      return false;
    }

    return ((Event) o).matches(this.subject, this.startsAt, this.endsAt);
  }

  private int computeHashCode() {
    return Objects.hash(this.startsAt.toLocalDate().atStartOfDay(ZoneId.systemDefault()));
  }

  /**
   * This class helps build a customized version of the event.
   */
  public static class Builder {
    private UUID seriesId;
    private String subject;
    private String description;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Location location;
    private Status status;

    /**
     * Initializes the builder with all null values.
     */
    public Builder() {
      this.seriesId = null;
      this.subject = null;
      this.description = null;
      this.startsAt = null;
      this.endsAt = null;
      this.location = null;
      this.status = null;
    }

    /**
     * Initializes the builder with starting values that of another event.
     *
     * @param event Target event to copy the starting values from.
     */
    public Builder(Event event) {
      this.subject = event.subject();
      this.description = event.description();
      this.startsAt = event.startsAt();
      this.endsAt = event.endsAt();
      this.location = event.location();
      this.status = event.status();
    }

    /**
     * Set the series of the event to be built.
     *
     * @param seriesId UUID of the series the event could belong to.
     * @return Self.
     */
    public Builder seriesId(UUID seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Set the subject of the event to be built.
     *
     * @param subject Subject with String type.
     * @return Self.
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Set the description of the event to be built.
     *
     * @param description Description with String type.
     * @return Self.
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Set the start timestamp of the event to be built.
     *
     * @param startsAt Start timestamp with LocalDateTime type.
     * @return Self.
     */
    public Builder startsAt(LocalDateTime startsAt) {
      this.startsAt = startsAt;
      return this;
    }

    /**
     * Set the end timestamp of the event to be built.
     *
     * @param endsAt End timestamp with LocalDateTime type.
     * @return Self.
     */
    public Builder endsAt(LocalDateTime endsAt) {
      this.endsAt = endsAt;
      return this;
    }

    /**
     * Set the location of the event to be built.
     *
     * @param location location with Location type.
     * @return Self.
     */
    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    /**
     * Set the status of the event to be built.
     *
     * @param status status with ShowStatus type.
     * @return Self.
     */
    public Builder status(Status status) {
      this.status = status;
      return this;
    }


    /**
     * A method to construct an EventV1 object using the configured values.
     *
     * @return The EventV1 object.
     */
    public Event build() {
      return new EventV1(this.subject, this.description, this.startsAt, this.endsAt, this.location,
          this.status, this.seriesId == null ? UUID.randomUUID() : this.seriesId);
    }
  }
}
