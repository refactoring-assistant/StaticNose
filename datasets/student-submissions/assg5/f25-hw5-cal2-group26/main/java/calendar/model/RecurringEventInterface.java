package calendar.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a group of events that repeat on certain days(like "Team Meeting
 * every Monday and Wednesday").
 * It creates all the individual events in the series. The series isn't stored
 * as one big event, it generates regular events when needed.
 */
public interface RecurringEventInterface {

  /**
   * Gets one specific event from the series by its position.
   * For example, if the series runs 6 times, index 3 gives the 4th event.
   *
   * @param occurrenceIndex which event in the series (starts at 0).
   * @return the event at that position, or null if the index is too high.
   */
  EventInterface getEventInstance(int occurrenceIndex);

  /**
   * Creates and returns all the individual events in this series. This is useful when you want to
   * add them to the calendar or show them to the user.
   *
   * @return a list of all events in the series (never null, may be empty).
   */
  List<EventInterface> getAllEvents();

  /**
   * Tells how many times this series should repeat.
   *
   * @return the number of times to repeat, or 0 if it uses an end date instead.
   */
  int getOccurrences();

  /**
   * Sets how many times the series should repeat. Used when creating a series like
   * "repeat 5 times".
   *
   * @param occurrences how many times to repeat (must be positive).
   */
  void setOccurrences(int occurrences);

  /**
   * Gets the list of weekdays this event repeats on(Returns short names like "M", "T",
   * "W" for Monday, Tuesday, Wednesday).
   *
   * @return list of weekday codes (e.g., ["M", "W"]), never null.
   */
  List<Weekday> getWeekdays();

  /**
   * Sets which days of the week the event should repeat(Use letters: M=Monday, T=Tuesday,
   * W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday).
   *
   * @param weekdays list of weekday codes (e.g., ["M", "W"]).
   */
  void setWeekdays(List<Weekday> weekdays);

  /**
   * Gets the last date this series should run. For example, "until May 30, 2025"
   * means include events on that day if they match the weekday.
   *
   * @return the end date and time, or null if it uses occurrences instead.
   */
  ZonedDateTime getEndDate();

  /**
   * Sets the final date for this series. The series stops after any event that
   * starts on or before this date.
   *
   * @param endDate when to stop the series (can be null if using occurrences).
   */
  void setEndDate(ZonedDateTime endDate);

  /**
   * Gets a list of dates of skipped instances in the series.
   *
   * @return list of skipped event start times (never null, may be empty).
   */
  List<ZonedDateTime> getSkippedInstances();

  /**
   * Adds skipped instances to the series. Useful when copying series.
   *
   * @param skipped list of skipped event start times.
   */
  void addSkippedInstances(List<ZonedDateTime> skipped);

  /**
   * Gets a map of modified instances in the series. Each key is the original event's start time,
   * and the value is the modified event.
   *
   * @return map of modified events (never null, may be empty).
   */
  Map<ZonedDateTime, EventInterface> getModifiedInstances();

  /**
   * Adds modified instances to the series. Useful when copying series.
   *
   * @param modified map of modified events.
   */
  void addModifiedInstances(Map<ZonedDateTime, EventInterface> modified);

}