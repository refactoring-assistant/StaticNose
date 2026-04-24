package calendar.model;

import calendar.model.utils.DayOfWeek;
import calendar.model.utils.EventStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Represents the rules and template for a recurring event series.
 */
public final class EventSeries extends AbstractEvent {

  private final LocalTime startTime;
  private final LocalTime endTime;
  private final LocalDate startDate;
  private final List<DayOfWeek> daysOfWeek;
  private final Integer occurrences;
  private final LocalDate untilDate;

  /**
   * Private constructor, accessible only by the internal Builder.
   */
  private EventSeries(String subject, LocalTime startTime, LocalTime endTime,
                      String description, String location, EventStatus status,
                      LocalDate startDate, List<DayOfWeek> daysOfWeek,
                      Integer occurrences, LocalDate untilDate) {
    super(subject, description, location, status);
    this.startTime = startTime;
    this.endTime = endTime;
    this.startDate = startDate;
    this.daysOfWeek = daysOfWeek;
    this.occurrences = occurrences;
    this.untilDate = untilDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public List<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  public Integer getOccurrences() {
    return occurrences;
  }

  public LocalDate getUntilDate() {
    return untilDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventSeries that = (EventSeries) o;
    return subject.equals(that.subject)
        && startTime.equals(that.startTime)
        && endTime.equals(that.endTime)
        && startDate.equals(that.startDate)
        && daysOfWeek.equals(that.daysOfWeek)
        && Objects.equals(occurrences, that.occurrences)
        && Objects.equals(untilDate, that.untilDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startTime, endTime, startDate, daysOfWeek, occurrences, untilDate);
  }

  @Override
  public String toString() {
    return "EventSeries{"
        + "subject='" + subject + '\''
        + ", startDate=" + startDate
        + ", startTime=" + startTime
        + ", days=" + daysOfWeek
        + (occurrences != null ? ", occurrences=" + occurrences : ", until=" + untilDate)
        + '}';
  }


  /**
   * The public-facing class for constructing an EventSeries.
   */
  public static class Builder extends AbstractEvent.AbstractBuilder<Builder> {
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate startDate;
    private List<DayOfWeek> daysOfWeek;
    private Integer occurrences = null;
    private LocalDate untilDate = null;

    /**
     * Creates a new Builder for an EventSeries.
     *
     * @param subject    The subject for all events in the series.
     * @param startDate  The date of the *first* event.
     * @param startTime  The start time for all events.
     * @param endTime    The end time for all events.
     * @param daysOfWeek The list of days (M, T, R, etc.) to repeat on.
     */
    public Builder(String subject, LocalDate startDate, LocalTime startTime,
                   LocalTime endTime, List<DayOfWeek> daysOfWeek) {
      super();
      this.withSubject(subject);
      this.startDate = startDate;
      this.startTime = startTime;
      this.endTime = endTime;
      this.daysOfWeek = daysOfWeek;
    }

    /**
     * Creates a new Builder based on an existing EventSeries.
     *
     * @param existingSeries The series to copy properties from.
     */
    public Builder(EventSeries existingSeries) {
      super();
      this.withSubject(existingSeries.subject);
      this.withDescription(existingSeries.description);
      this.withLocation(existingSeries.location);
      this.withStatus(existingSeries.status);

      this.startTime = existingSeries.startTime;
      this.endTime = existingSeries.endTime;
      this.startDate = existingSeries.startDate;
      this.daysOfWeek = existingSeries.daysOfWeek;
      this.occurrences = existingSeries.occurrences;
      this.untilDate = existingSeries.untilDate;
    }


    /**
     * Sets the start time for all events in the series.
     *
     * @param startTime The start time.
     * @return This builder for chaining.
     */
    public Builder withStartTime(LocalTime startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the end time for all events in the series.
     *
     * @param endTime The end time.
     * @return This builder for chaining.
     */
    public Builder withEndTime(LocalTime endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the start date for the series (date of the first event).
     *
     * @param startDate The date the series begins.
     * @return This builder for chaining.
     */
    public Builder withStartDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the days of the week for recurrence.
     *
     * @param daysOfWeek A list of DayOfWeek enums.
     * @return This builder for chaining.
     */
    public Builder withDaysOfWeek(List<DayOfWeek> daysOfWeek) {
      this.daysOfWeek = daysOfWeek;
      return this;
    }

    /**
     * Clears any existing end condition (occurrences or until date).
     *
     * @return This builder for chaining.
     */
    public Builder clearEndCondition() {
      this.occurrences = null;
      this.untilDate = null;
      return this;
    }

    /**
     * Sets the series to repeat for a specific number of occurrences.
     * This overrides any existing 'until' date.
     *
     * @param occurrences The total number of events in the series.
     * @return This builder for chaining.
     */
    public Builder forOccurrences(int occurrences) {
      this.untilDate = null;
      this.occurrences = occurrences;
      return this;
    }

    /**
     * Sets the series to repeat until a specific end date (inclusive).
     * This overrides any existing 'occurrences' count.
     *
     * @param untilDate The last possible date for an event.
     * @return This builder for chaining.
     */
    public Builder until(LocalDate untilDate) {
      this.occurrences = null;
      this.untilDate = untilDate;
      return this;
    }

    /**
     * Validates the data and constructs the final, immutable EventSeries object.
     *
     * @return A new EventSeries instance.
     * @throws IllegalArgumentException if validation fails.
     */
    public EventSeries build() {

      if (startDate == null || startTime == null || endTime == null) {
        throw new IllegalArgumentException("Start date, start time, and end time are required.");
      }
      if (daysOfWeek == null || daysOfWeek.isEmpty()) {
        throw new IllegalArgumentException(
            "At least one day of the week must be specified for a series.");
      }
      if (endTime.isBefore(startTime)) {
        throw new IllegalArgumentException(
            "End time cannot be before start time for an event series.");
      }
      if ((occurrences == null && untilDate == null)) {
        throw new IllegalArgumentException(
            "An event series must specify either a number of occurrences or an 'until' date.");
      }
      if (occurrences != null && occurrences <= 0) {
        throw new IllegalArgumentException("Number of occurrences must be positive.");
      }
      if (untilDate != null && untilDate.isBefore(startDate)) {
        throw new IllegalArgumentException("The 'until' date cannot be before the start date.");
      }

      return new EventSeries(subject, startTime, endTime, description,
          location, status, startDate, daysOfWeek,
          occurrences, untilDate);
    }
  }
}