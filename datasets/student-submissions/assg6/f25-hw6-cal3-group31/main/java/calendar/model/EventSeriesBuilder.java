package calendar.model;

import java.util.Set;

/**
 * Builder class for creating EventSeries objects with a fluent API.
 * This pattern helps avoid constructors with too many parameters (code smell).
 *
 * <p>Example usage:
 * <pre>
 * EventSeries series = new EventSeriesBuilder()
 *     .subject("Weekly Standup")
 *     .startDate(new Date(2025, 5, 15))
 *     .startTime(new Time(9, 0))
 *     .endTime(new Time(9, 30))
 *     .repeatDays(Set.of(Day.MONDAY, Day.WEDNESDAY, Day.FRIDAY))
 *     .occurrences(10)
 *     .description("Daily team sync")
 *     .location(Location.ONLINE)
 *     .status(Status.PUBLIC)
 *     .build();
 * </pre>
 */
public class EventSeriesBuilder {
  private String subject;
  private Date startDate;
  private Time startTime;
  private Time endTime;
  private Set<Day> repeatDays;
  private Integer occurrences;
  private Date endDate;
  private String description;
  private Location location;
  private Status status;

  /**
   * Sets the subject (required).
   *
   * @param subject the event subject
   * @return this builder
   */
  public EventSeriesBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the start date (required).
   *
   * @param startDate the start date
   * @return this builder
   */
  public EventSeriesBuilder startDate(Date startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Sets the start time (required).
   *
   * @param startTime the start time
   * @return this builder
   */
  public EventSeriesBuilder startTime(Time startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Sets the end time (required).
   *
   * @param endTime the end time
   * @return this builder
   */
  public EventSeriesBuilder endTime(Time endTime) {
    this.endTime = endTime;
    return this;
  }

  /**
   * Sets the repeat days (required).
   *
   * @param repeatDays the days of the week to repeat on
   * @return this builder
   */
  public EventSeriesBuilder repeatDays(Set<Day> repeatDays) {
    this.repeatDays = repeatDays;
    return this;
  }

  /**
   * Sets the number of occurrences (use this OR endDate, not both).
   *
   * @param occurrences the number of occurrences
   * @return this builder
   */
  public EventSeriesBuilder occurrences(int occurrences) {
    this.occurrences = occurrences;
    return this;
  }

  /**
   * Sets the end date (use this OR occurrences, not both).
   *
   * @param endDate the end date (inclusive)
   * @return this builder
   */
  public EventSeriesBuilder endDate(Date endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Sets the description (optional).
   *
   * @param description the event description
   * @return this builder
   */
  public EventSeriesBuilder description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the location (optional).
   *
   * @param location the event location
   * @return this builder
   */
  public EventSeriesBuilder location(Location location) {
    this.location = location;
    return this;
  }

  /**
   * Sets the status (optional).
   *
   * @param status the event status
   * @return this builder
   */
  public EventSeriesBuilder status(Status status) {
    this.status = status;
    return this;
  }

  /**
   * Builds the EventSeries object.
   *
   * @return a new EventSeries instance
   * @throws IllegalArgumentException if required fields are not set or if both occurrences
   *                                  and endDate are set
   */
  public EventSeries build() {
    if (occurrences != null && endDate != null) {
      throw new IllegalArgumentException("Cannot set both occurrences and endDate");
    }
    if (occurrences == null && endDate == null) {
      throw new IllegalArgumentException("Must set either occurrences or endDate");
    }

    if (occurrences != null) {
      return new EventSeries(subject, startDate, startTime, endTime, repeatDays,
          occurrences, description, location, status);
    } else {
      return new EventSeries(subject, startDate, startTime, endTime, repeatDays,
          endDate, description, location, status);
    }
  }
}

