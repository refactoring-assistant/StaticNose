package model;

import java.time.LocalDateTime;

/**
 * Represents a standalone single event which is not part of any recurring series.
 * This class extends AbstractEvent and implements the specific behavior for
 * non-recurring calendar events.
 * SingleEvent instances are created using the Builder pattern to ensure
 * proper validation and initialization of all required fields.
 *
 */
public class SingleEvent extends AbstractEvent {

  /**
   * Constructs a SingleEvent from the provided builder.
   * This constructor is private to enforce the use of the Builder pattern.
   *
   * @param builder the builder containing event properties
   */
  private SingleEvent(Builder builder) {
    super(builder.subject, builder.description, builder.startDateTime, builder.endDateTime,
        builder.isPublic, builder.location);

  }

  @Override
  public boolean isPartOfSeries() {
    return false;
  }

  @Override
  public String getSeriesId() {
    return null;
  }

  /**
   * Returns a new Builder instance for constructing SingleEvent objects.
   *
   * @return a new Builder instance
   */
  public static Builder getBuilder() {
    return new Builder();
  }

  /**
   * Builder class for constructing SingleEvent instances.
   * This builder ensures all required fields are set and validates
   * the event properties before construction.
   */

  public static class Builder {
    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isPublic = true;
    private String location = "";
    private String description = "";

    /**
     * Private constructor to prevent external instantiation.
     */
    private Builder() {

    }

    /**
     * Sets the subject of the event.
     *
     * @param subject the event subject (required)
     * @return this builder instance for method chaining
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date and time of the event.
     *
     * @param startDateTime the start date and time (required)
     * @return this builder instance for method chaining
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date and time of the event.
     *
     * @param endDateTime the end date and time (optional)
     * @return this builder instance for method chaining
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the visibility of the event.
     *
     * @param isPublic true for public events, false for private
     * @return this builder instance for method chaining
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param location the event location (optional)
     * @return this builder instance for method chaining
     */
    public Builder location(String location) {
      this.location = location == null ? "" : location;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the event description (optional)
     * @return this builder instance for method chaining
     */
    public Builder description(String description) {
      this.description = description == null ? "" : description;
      return this;
    }

    /**
     * Builds and returns a SingleEvent instance with the configured properties.
     *
     * @return a new SingleEvent instance
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public SingleEvent build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("subject cannot be null or empty");
      }

      if (startDateTime == null) {
        throw new IllegalArgumentException("Start date and time cannot be null");
      }

      if (endDateTime != null && !endDateTime.isAfter(startDateTime)) {
        throw new IllegalArgumentException("endDateTime must be after startDateTime");
      }

      return new SingleEvent(this);
    }
  }
}
