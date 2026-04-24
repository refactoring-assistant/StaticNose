package calendar.view;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents all parameters required to create an event.
 */
public class CreateEventParams {

  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final String status;
  private final boolean isRepeating;
  private final String repeatDays;
  private final String type;
  private final LocalDate repeatEndDate;
  private final int repeats;

  /**
   * Constructs an immutable CreateEventParams instance using values from the Builder.
   *
   * @param builder the builder containing initialized event values
   */
  private CreateEventParams(Builder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.isRepeating = builder.isRepeating;
    this.repeatDays = builder.repeatDays;
    this.type = builder.type;
    this.repeatEndDate = builder.repeatEndDate;
    this.repeats = builder.repeats;
  }

  /**
   * Returns the subject/title of the event.
   *
   * @return the subject of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the start date and time of the event.
   *
   * @return the start LocalDateTime
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return the end LocalDateTime
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns the event description text.
   *
   * @return the description of the event
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the event's location.
   *
   * @return the location of the event
   */
  public String getLocation() {
    return location;
  }

  /**
   * Returns the status text of the event.
   *
   * @return the status value
   */
  public String getStatus() {
    return status;
  }

  /**
   * Indicates whether the event repeats.
   *
   * @return true if repeating, false otherwise
   */
  public boolean isRepeating() {
    return isRepeating;
  }

  /**
   * Returns the textual representation of repeat days (e.g., "MO,TU,WE").
   *
   * @return the repeat days string
   */
  public String getRepeatDays() {
    return repeatDays;
  }

  /**
   * Returns the type/category of the event.
   *
   * @return the event type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the date on which repeating ends.
   *
   * @return the repeat end date, or null if not repeating
   */
  public LocalDate getRepeatEndDate() {
    return repeatEndDate;
  }

  /**
   * Returns the number of repetitions for this event.
   *
   * @return the total number of repeats
   */
  public int getRepeats() {
    return repeats;
  }

  /**
   * Builder class for constructing CreateEventParams instances.
   */
  public static class Builder {

    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description = "No description given";
    private String location = "UNKNOWN";
    private String status = "UNKNOWN";
    private boolean isRepeating = false;
    private String repeatDays = "";
    private String type = "";
    private LocalDate repeatEndDate = null;
    private int repeats = 0;

    /**
     * Sets the subject/title for the event.
     *
     * @param subject the subject text
     * @return this Builder for chaining
     */
    public Builder setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date and time.
     *
     * @param startDateTime the LocalDateTime when the event begins
     * @return this Builder for chaining
     */
    public Builder setStartDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date and time.
     *
     * @param endDateTime the LocalDateTime when the event ends
     * @return this Builder for chaining
     */
    public Builder setEndDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets a descriptive text for the event.
     *
     * @param description the description string
     * @return this Builder for chaining
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the event location.
     *
     * @param location the location string
     * @return this Builder for chaining
     */
    public Builder setLocation(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event's status field.
     *
     * @param status the status text
     * @return this Builder for chaining
     */
    public Builder setStatus(String status) {
      this.status = status;
      return this;
    }

    /**
     * Sets whether the event repeats.
     *
     * @param repeating true if the event repeats
     * @return this Builder for chaining
     */
    public Builder setRepeating(boolean repeating) {
      this.isRepeating = repeating;
      return this;
    }

    /**
     * Sets which days of the week the event repeats on.
     *
     * @param repeatDays a string representing repeat days
     * @return this Builder for chaining
     */
    public Builder setRepeatDays(String repeatDays) {
      this.repeatDays = repeatDays;
      return this;
    }

    /**
     * Sets the category or type of the event.
     *
     * @param type the type string
     * @return this Builder for chaining
     */
    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    /**
     * Sets the final date at which repeating should stop.
     *
     * @param repeatEndDate the repeat end date
     * @return this Builder for chaining
     */
    public Builder setRepeatEndDate(LocalDate repeatEndDate) {
      this.repeatEndDate = repeatEndDate;
      return this;
    }

    /**
     * Sets the number of times the event repeats.
     *
     * @param repeats total repeat count
     * @return this Builder for chaining
     */
    public Builder setRepeats(int repeats) {
      this.repeats = repeats;
      return this;
    }

    /**
     * Builds the final immutable CreateEventParams instance.
     *
     * @return a new CreateEventParams object
     * @throws IllegalStateException if required fields are missing
     */
    public CreateEventParams build() {
      if (subject == null || startDateTime == null || endDateTime == null) {
        throw new IllegalStateException("Subject, start time, and end time must not be null.");
      }
      return new CreateEventParams(this);
    }
  }
}
