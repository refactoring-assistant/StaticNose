package calendar.model;

/**
 * Builder class for creating Event objects with a fluent API.
 * This pattern helps avoid constructors with too many parameters (code smell).
 *
 * <p>Example usage:
 * <pre>
 * Event event = new EventBuilder()
 *     .subject("Team Meeting")
 *     .startDate(new Date(2025, 5, 15))
 *     .startTime(new Time(10, 0))
 *     .endDate(new Date(2025, 5, 15))
 *     .endTime(new Time(11, 0))
 *     .description("Quarterly review")
 *     .location(Location.ONLINE)
 *     .status(Status.PUBLIC)
 *     .build();
 * </pre>
 */
public class EventBuilder {
  private String subject;
  private Date startDate;
  private Time startTime;
  private Date endDate;
  private Time endTime;
  private String description;
  private Location location;
  private Status status;

  /**
   * Sets the subject (required).
   *
   * @param subject the event subject
   * @return this builder
   */
  public EventBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the start date (required).
   *
   * @param startDate the start date
   * @return this builder
   */
  public EventBuilder startDate(Date startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Sets the start time (required).
   *
   * @param startTime the start time
   * @return this builder
   */
  public EventBuilder startTime(Time startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Sets the end date (optional, defaults to startDate if not set).
   *
   * @param endDate the end date
   * @return this builder
   */
  public EventBuilder endDate(Date endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Sets the end time (optional, defaults to 5pm if not set).
   *
   * @param endTime the end time
   * @return this builder
   */
  public EventBuilder endTime(Time endTime) {
    this.endTime = endTime;
    return this;
  }

  /**
   * Sets the description (optional).
   *
   * @param description the event description
   * @return this builder
   */
  public EventBuilder description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the location (optional).
   *
   * @param location the event location
   * @return this builder
   */
  public EventBuilder location(Location location) {
    this.location = location;
    return this;
  }

  /**
   * Sets the status (optional).
   *
   * @param status the event status
   * @return this builder
   */
  public EventBuilder status(Status status) {
    this.status = status;
    return this;
  }

  /**
   * Builds the Event object.
   *
   * @return a new Event instance
   * @throws IllegalArgumentException if required fields are not set
   */
  public Event build() {
    return new Event(subject, startDate, startTime, endDate, endTime,
        description, location, status);
  }
}

