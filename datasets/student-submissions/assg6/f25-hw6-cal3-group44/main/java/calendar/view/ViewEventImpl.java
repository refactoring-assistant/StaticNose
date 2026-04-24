package calendar.view;

import java.time.LocalDateTime;

/**
 * Data transfer object for event information to be displayed in the view.
 * Uses a builder to avoid large constructors.
 */
public class ViewEventImpl implements ViewEvent {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final String status;
  private final boolean allDay;
  private final boolean isSeries;

  /**
   * Construtor for the View Event.
   *
   * @param builder contains all the details.
   */
  private ViewEventImpl(Builder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.allDay = builder.allDay;
    this.isSeries = builder.isSeries;
  }

  /**
   * Returns the event subject.
   */
  @Override
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the start date and time.
   */
  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time.
   */
  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns the event description.
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Returns the event location.
   */
  @Override
  public String getLocation() {
    return location;
  }

  /**
   * Returns the event status.
   */
  @Override
  public String getStatus() {
    return status;
  }

  /**
   * Indicates whether the event is all day.
   */
  @Override
  public boolean isAllDay() {
    return allDay;
  }

  /**
   * Indicates whether the event is part of a series.
   */
  @Override
  public boolean isSeries() {
    return isSeries;
  }

  /**
   * Static builder class used to build a view event.
   */
  public static class Builder {
    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description = "";
    private String location = "";
    private String status = "";
    private boolean allDay = false;
    private boolean isSeries = false;

    /**
     * Sets the subject.
     */
    public Builder setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date and time.
     */
    public Builder setStartDateTime(LocalDateTime start) {
      this.startDateTime = start;
      return this;
    }

    /**
     * Sets the end date and time.
     */
    public Builder setEndDateTime(LocalDateTime end) {
      this.endDateTime = end;
      return this;
    }

    /**
     * Sets the description.
     */
    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location.
     */
    public Builder setLocation(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status.
     */
    public Builder setStatus(String status) {
      this.status = status;
      return this;
    }

    /**
     * Sets whether the event is all day.
     */
    public Builder setAllDay(boolean allDay) {
      this.allDay = allDay;
      return this;
    }

    /**
     * Sets whether the event is part of a series.
     */
    public Builder setIsSeries(boolean isSeries) {
      this.isSeries = isSeries;
      return this;
    }

    /**
     * Builds and returns the ViewEvent.
     */
    public ViewEvent build() {
      return new ViewEventImpl(this);
    }
  }
}
