package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a series of recurring events.
 */
public class EventSeries {
  private final String seriesId;
  private final String baseSubject;
  private final LocalDateTime baseStartTime;
  private final LocalDateTime baseEndTime;
  private final String baseDescription;
  private final String baseLocation;
  private final EventStatus baseStatus;
  private final Set<DayOfWeek> repeatDays;
  private final Integer occurrences;
  private final LocalDate untilDate;

  /**
   * Constructs an event series.
   */
  public EventSeries(String baseSubject, LocalDateTime baseStartTime, LocalDateTime baseEndTime,
                     String baseDescription, String baseLocation, EventStatus baseStatus,
                     Set<DayOfWeek> repeatDays, Integer occurrences, LocalDate untilDate) {
    this.seriesId = UUID.randomUUID().toString();
    this.baseSubject = baseSubject;
    this.baseStartTime = baseStartTime;
    this.baseEndTime = baseEndTime;
    this.baseDescription = baseDescription;
    this.baseLocation = baseLocation;
    this.baseStatus = baseStatus;
    this.repeatDays = EnumSet.copyOf(repeatDays);
    this.occurrences = occurrences;
    this.untilDate = untilDate;

    if (repeatDays.isEmpty()) {
      throw new IllegalArgumentException("Repeat days cannot be empty");
    }
    if (occurrences == null && untilDate == null) {
      throw new IllegalArgumentException("Either occurrences or until date must be specified");
    }
    if (occurrences != null && occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    if (untilDate != null && untilDate.isBefore(baseStartTime.toLocalDate())) {
      throw new IllegalArgumentException("Until date cannot be before start date");
    }
  }

  /**
   * Gets the series ID.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Gets the base subject.
   *
   * @return the base subject
   */
  public String getBaseSubject() {
    return baseSubject;
  }

  /**
   * Gets the base start time.
   *
   * @return the base start time
   */
  public LocalDateTime getBaseStartTime() {
    return baseStartTime;
  }

  /**
   * Gets the base end time.
   *
   * @return the base end time
   */
  public LocalDateTime getBaseEndTime() {
    return baseEndTime;
  }

  /**
   * Gets the base description.
   *
   * @return the base description
   */
  public String getBaseDescription() {
    return baseDescription;
  }

  /**
   * Gets the base location.
   *
   * @return the base location
   */
  public String getBaseLocation() {
    return baseLocation;
  }

  /**
   * Gets the base status.
   *
   * @return the base status
   */
  public EventStatus getBaseStatus() {
    return baseStatus;
  }

  /**
   * Gets the repeat days.
   *
   * @return the repeat days
   */
  public Set<DayOfWeek> getRepeatDays() {
    return Set.copyOf(repeatDays);
  }

  /**
   * Gets the occurrences.
   *
   * @return the occurrences
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the until date.
   *
   * @return the until date
   */
  public LocalDate getUntilDate() {
    return untilDate;
  }

  /**
   * Checks if this is an all-day series.
   *
   * @return true if all-day series
   */
  public boolean isAllDaySeries() {
    return baseStartTime.toLocalTime().equals(java.time.LocalTime.of(8, 0))
        && baseEndTime.toLocalTime().equals(java.time.LocalTime.of(17, 0));
  }
}