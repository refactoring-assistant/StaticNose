package calendar.view.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * View DTO representing a request to edit an event property.
 * Contains all information needed to update a specific property of an event.
 */
public class EditEventDto {

  /** The subject (name) of the event to edit. */
  private final String subject;

  /** The start date of the event. */
  private final LocalDate startDate;

  /** The end date of the event. */
  private final LocalDate endDate;

  /** The start time of the event. */
  private final LocalTime startTime;

  /** The end time of the event. */
  private final LocalTime endTime;

  /** The name of the property to edit (e.g., "location", "status"). */
  private final String propertyToEdit;

  /** The new value for the property. */
  private final String newValue;

  /** The scope of the edit: single event, future events, or entire series. */
  private final Scope scope;

  /**
   * Private constructor used by {@link Builder}.
   *
   * @param builder the builder containing all field values
   */
  private EditEventDto(Builder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.propertyToEdit = builder.propertyToEdit;
    this.newValue = builder.newValue;
    this.scope = builder.scope;
  }

  /**
   * Gets the event subject.
   *
   * @return the subject of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start date of the event.
   *
   * @return the start date
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * Gets the end date of the event.
   *
   * @return the end date
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * Gets the property to edit.
   *
   * @return the property name
   */
  public String getPropertyToEdit() {
    return propertyToEdit;
  }

  /**
   * Gets the new value for the property.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValue;
  }

  /**
   * Gets the scope of the edit.
   *
   * @return the scope
   */
  public Scope getScope() {
    return scope;
  }

  /**
   * Enum representing the scope of the edit operation.
   */
  public enum Scope {
    /** Edit applies only to the single selected event. */
    SINGLE_EVENT,

    /** Edit applies to this event and all future events in the series. */
    FUTURE_EVENTS,

    /** Edit applies to the entire series of events. */
    ENTIRE_SERIES
  }

  /**
   * Builder class for {@link EditEventDto}.
   */
  public static class Builder {

    /** Builder field for event subject. */
    private String subject;

    /** Builder field for start date. */
    private LocalDate startDate;

    /** Builder field for end date. */
    private LocalDate endDate;

    /** Builder field for start time. */
    private LocalTime startTime;

    /** Builder field for end time. */
    private LocalTime endTime;

    /** Builder field for the property to edit. */
    private String propertyToEdit;

    /** Builder field for the new value. */
    private String newValue;

    /** Builder field for the edit scope. */
    private Scope scope;

    /**
     * Sets the event subject.
     *
     * @param subject the subject of the event
     * @return this builder instance
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     * @return this builder instance
     */
    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date
     * @return this builder instance
     */
    public Builder endDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the start time
     * @return this builder instance
     */
    public Builder startTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the end time
     * @return this builder instance
     */
    public Builder endTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the property to edit.
     *
     * @param propertyToEdit the property name
     * @return this builder instance
     */
    public Builder propertyToEdit(String propertyToEdit) {
      this.propertyToEdit = propertyToEdit;
      return this;
    }

    /**
     * Sets the new value for the property.
     *
     * @param newValue the new value
     * @return this builder instance
     */
    public Builder newValue(String newValue) {
      this.newValue = newValue;
      return this;
    }

    /**
     * Sets the scope of the edit.
     *
     * @param scope the edit scope
     * @return this builder instance
     */
    public Builder scope(Scope scope) {
      this.scope = scope;
      return this;
    }

    /**
     * Builds the {@link EditEventDto} instance.
     *
     * @return a new EditEventDto
     */
    public EditEventDto build() {
      return new EditEventDto(this);
    }
  }
}
