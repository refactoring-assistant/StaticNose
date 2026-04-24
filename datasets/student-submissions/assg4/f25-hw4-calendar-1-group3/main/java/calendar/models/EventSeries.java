package calendar.models;

import java.util.Set;

/**
 * Represents a recurring series of related events in a calendar.
 *
 * <p>An {@code EventSeries} groups multiple {@link Event} instances that follow
 * a common recurrence pattern — for example, weekly team meetings or daily reminders. Each
 * individual event in the series can be retrieved as part of the set.
 * </p>
 */
public interface EventSeries {

  /**
   * Returns all events that belong to this series.
   *
   * <p>The returned set contains each occurrence of the event generated
   * according to the recurrence rule. Implementations should ensure that the set does not contain
   * duplicates and is sorted or ordered consistently (e.g., chronologically) if applicable.
   * </p>
   *
   * @return set of {@link Event} objects representing each event in the series; never {@code null}
   */
  Set<Event> getEvents();

  /**
   * Gets the unique identifier for this series.
   *
   * @return the series ID
   */
  String getSeriesId();

  /**
   * Get Recurrence Rule of the series.
   *
   * @return the Recurrence Rule
   */
  RecurrenceRule getRecurrenceRule();
}