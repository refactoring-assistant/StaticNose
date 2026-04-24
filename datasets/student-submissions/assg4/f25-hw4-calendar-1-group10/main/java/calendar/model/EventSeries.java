package calendar.model;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a recurring event pattern that can generate multiple Event instances.
 * Each EventSeries is defined by:
 * - a base event (the first occurrence),
 * - a set of days of the week it repeats on,
 * - either a repeat count (number of occurrences) or an end date,
 * - and a unique series ID shared by all generated events.
 */
public class EventSeries {

  private final Event baseEvent;
  private final Set<DayOfWeek> repeatDays;
  private final Integer repeatCount;
  private final LocalDate endDate;
  private final String seriesId;

  /**
   * Creates a series that repeats on specific weekdays for a fixed number of occurrences.
   *
   * @param baseEvent   the first event in the series
   * @param repeatDays  the set of days of the week the event repeats on
   * @param repeatCount number of total occurrences in the series
   */
  public EventSeries(Event baseEvent, Set<DayOfWeek> repeatDays, int repeatCount) {
    this(baseEvent, repeatDays, Integer.valueOf(repeatCount), null);
  }

  /**
   * Creates a series that repeats on specific weekdays until a given date.
   *
   * @param baseEvent  the first event in the series
   * @param repeatDays the set of days of the week the event repeats on
   * @param endDate    the final date to include in the series
   */
  public EventSeries(Event baseEvent, Set<DayOfWeek> repeatDays, LocalDate endDate) {
    this(baseEvent, repeatDays, null, endDate);
  }

  /**
   * Private constructor used internally by both public constructors.
   */
  private EventSeries(Event baseEvent, Set<DayOfWeek> repeatDays,
                      Integer repeatCount, LocalDate endDate) {

    if (baseEvent == null || repeatDays == null) {
      throw new IllegalArgumentException("Base event and repeat days are required.");
    }

    if ((repeatCount == null || repeatCount <= 0) && endDate == null) {
      throw new IllegalArgumentException("Either repeat count or end date must be provided.");
    }

    this.baseEvent = baseEvent;
    this.repeatDays = Collections.unmodifiableSet(new HashSet<DayOfWeek>(repeatDays));
    this.repeatCount = repeatCount;
    this.endDate = endDate;
    this.seriesId = UUID.randomUUID().toString();
  }

  /**
   * Generates the list of individual Event instances for this recurring series.
   *
   * @return a list of Event objects representing all occurrences
   */
  public List<Event> generateInstances() {
    List<Event> instances = new ArrayList<Event>();

    LocalDate startDate = baseEvent.getStart().toLocalDate();
    LocalTime startTime = baseEvent.getStart().toLocalTime();
    LocalTime endTime = baseEvent.getEnd().toLocalTime();

    int created = 0;
    LocalDate current = startDate;

    // Iterate day by day from start date, generating events on matching days.
    while (true) {

      // If today matches one of the repeat days, create an event instance
      if (repeatDays.contains(current.getDayOfWeek())) {
        LocalDateTime s = LocalDateTime.of(current, startTime);
        LocalDateTime e = LocalDateTime.of(current, endTime);

        Event ev = new Event(
            baseEvent.getSubject(),
            s,
            e,
            baseEvent.getDescription(),
            baseEvent.getLocation(),
            baseEvent.getStatus(),
            seriesId
        );

        instances.add(ev);
        created++;

        // Stop if we’ve reached the repeat count
        if (repeatCount != null && created >= repeatCount) {
          break;
        }
      }

      // Move to the next calendar day
      current = current.plusDays(1);

      // Stop if we’ve passed the end date (if applicable)
      if (endDate != null && current.isAfter(endDate)) {
        break;
      }
    }

    return instances;
  }

  /**
   * Gets the unique ID for this series.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Gets the base event for this series.
   *
   * @return the base event
   */
  public Event getBaseEvent() {
    return baseEvent;
  }

  /**
   * Gets the set of days of the week this series repeats on.
   *
   * @return the set of days
   */
  public Set<DayOfWeek> getRepeatDays() {
    return repeatDays;
  }

  /**
   * Gets the number of occurrences for this series.
   *
   * @return the repeat count
   */
  public Integer getRepeatCount() {
    return repeatCount;
  }

  /**
   * Gets the end date for this series.
   *
   * @return the end date
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Returns a string representation of this series.
   *
   * @return the string representation
   */
  @Override
  public String toString() {
    return "EventSeries{"
        + "subject='" + baseEvent.getSubject() + '\''
        + ", repeatDays=" + repeatDays
        + ", repeatCount=" + repeatCount
        + ", endDate=" + endDate
        + ", seriesId='" + seriesId.substring(0, 8) + '\''
        + '}';
  }
}
