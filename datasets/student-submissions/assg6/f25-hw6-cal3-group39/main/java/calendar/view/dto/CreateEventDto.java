package calendar.view.dto;

/**
 * View DTO representing a request to create a new event.
 * Contains raw user input from the EventDialog.
 */
public class CreateEventDto implements CreateEventDtoI {

  /** The name of the event. */
  private final String eventName;

  /** The start date of the event as a string. */
  private final String startDate;

  /** The start time of the event as a string. */
  private final String startTime;

  /** The end date of the event as a string. */
  private final String endDate;

  /** The end time of the event as a string. */
  private final String endTime;

  /** The location where the event takes place. */
  private final String location;

  /** A description of the event. */
  private final String description;

  /** The status of the event (e.g., busy/free). */
  private final String status;

  /** Whether the event is recurring. */
  private final boolean isRecurring;

  /** The days on which the event recurs (e.g., MTWRF). */
  private final String recurrenceDays;

  /** The end date of the recurrence. */
  private final String recurrenceEnd;

  /** Whether the event lasts all day. */
  private final boolean isAllDay;

  /**
   * Private constructor used by the {@link Builder}.
   *
   * @param builder the builder containing all field values
   */
  private CreateEventDto(Builder builder) {
    this.eventName = builder.eventName;
    this.startDate = builder.startDate;
    this.startTime = builder.startTime;
    this.endDate = builder.endDate;
    this.endTime = builder.endTime;
    this.location = builder.location;
    this.description = builder.description;
    this.status = builder.status;
    this.isRecurring = builder.isRecurring;
    this.recurrenceDays = builder.recurrenceDays;
    this.recurrenceEnd = builder.recurrenceEnd;
    this.isAllDay = builder.isAllDay;
  }

  /**
   * Gets the event name.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Gets the start date of the event.
   *
   * @return the start date
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Gets the end date of the event.
   *
   * @return the end date
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time
   */
  public String getEndTime() {
    return endTime;
  }

  /**
   * Gets the event location.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets the event description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the event status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Returns whether the event is recurring.
   *
   * @return true if recurring, false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Gets the recurrence days.
   *
   * @return the recurrence days
   */
  public String getRecurrenceDays() {
    return recurrenceDays;
  }

  /**
   * Gets the recurrence end date.
   *
   * @return the recurrence end date
   */
  public String getRecurrenceEnd() {
    return recurrenceEnd;
  }

  /**
   * Returns whether the event is an all-day event.
   *
   * @return true if all-day, false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Builder class for {@link CreateEventDto}.
   */
  public static class Builder {

    /** Builder field for event name. */
    private String eventName;

    /** Builder field for start date. */
    private String startDate;

    /** Builder field for start time. */
    private String startTime;

    /** Builder field for end date. */
    private String endDate;

    /** Builder field for end time. */
    private String endTime;

    /** Builder field for location. */
    private String location;

    /** Builder field for description. */
    private String description;

    /** Builder field for status. */
    private String status;

    /** Builder field indicating if recurring. */
    private boolean isRecurring;

    /** Builder field for recurrence days. */
    private String recurrenceDays;

    /** Builder field for recurrence end date. */
    private String recurrenceEnd;

    /** Builder field indicating all-day. */
    private boolean isAllDay;

    /**
     * Sets the event name.
     *
     * @param eventName the event name
     * @return this builder instance
     */
    public Builder eventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     * @return this builder instance
     */
    public Builder startDate(String startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the start time
     * @return this builder instance
     */
    public Builder startTime(String startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date
     * @return this builder instance
     */
    public Builder endDate(String endDate) {
      this.endDate = endDate;
      return this;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the end time
     * @return this builder instance
     */
    public Builder endTime(String endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location the location
     * @return this builder instance
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     * @return this builder instance
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the event status.
     *
     * @param status the status
     * @return this builder instance
     */
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Sets whether the event is recurring.
     *
     * @param isRecurring true if recurring
     * @return this builder instance
     */
    public Builder isRecurring(boolean isRecurring) {
      this.isRecurring = isRecurring;
      return this;
    }

    /**
     * Sets the recurrence days.
     *
     * @param recurrenceDays the recurrence days
     * @return this builder instance
     */
    public Builder recurrenceDays(String recurrenceDays) {
      this.recurrenceDays = recurrenceDays;
      return this;
    }

    /**
     * Sets the recurrence end date.
     *
     * @param recurrenceEnd the recurrence end date
     * @return this builder instance
     */
    public Builder recurrenceEnd(String recurrenceEnd) {
      this.recurrenceEnd = recurrenceEnd;
      return this;
    }

    /**
     * Sets whether the event lasts all day.
     *
     * @param isAllDay true if all-day
     * @return this builder instance
     */
    public Builder isAllDay(boolean isAllDay) {
      this.isAllDay = isAllDay;
      return this;
    }

    /**
     * Builds the {@link CreateEventDto} instance.
     *
     * @return a new CreateEventDto
     */
    public CreateEventDto build() {
      return new CreateEventDto(this);
    }
  }
}
