package calendar.model;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents metadata for a recurring series of events.
 */
public class EventSeries {
  private final String id;
  private final Set<DayOfWeek> daysOfWeek;
  private final boolean allDay;

  /**
   * Creates a new event series metadata record.
   *
   * @param daysOfWeek the days of week included in the series
   * @param allDay     whether the series was created as all-day events
   */
  public EventSeries(Set<DayOfWeek> daysOfWeek, boolean allDay) {
    this(UUID.randomUUID().toString(), daysOfWeek, allDay);
  }

  private EventSeries(String id, Set<DayOfWeek> daysOfWeek, boolean allDay) {
    this.id = Objects.requireNonNull(id);
    this.daysOfWeek = Collections.unmodifiableSet(daysOfWeek);
    this.allDay = allDay;
  }

  public String getId() {
    return id;
  }

  public Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  public boolean isAllDay() {
    return allDay;
  }

  /**
   * Creates a copy of this series metadata.
   *
   * @return copy with identical attributes
   */
  public EventSeries copy() {
    return new EventSeries(id, daysOfWeek, allDay);
  }
}
