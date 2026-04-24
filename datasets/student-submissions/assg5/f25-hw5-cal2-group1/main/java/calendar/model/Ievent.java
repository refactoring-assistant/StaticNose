package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents an event in a calendar.
 */
public interface Ievent {
  /**
   * Gets the name of the event.
   *
   * @return the event name
   */
  String getName();

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date time
   */
  ZonedDateTime getStartDateTime();

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date time
   */
  ZonedDateTime getEndDateTime();

  /**
   * Checks if the event occurs on a specific date.
   *
   * @param date the date to check
   * @return true if the event occurs on that date
   */
  boolean occursOn(LocalDate date);

  /**
   * Gets all occurrences of the event in a date range.
   *
   * @param start the start date (inclusive)
   * @param end   the end date (inclusive)
   * @return list of start times for occurrences
   */
  List<ZonedDateTime> getOccurrencesInRange(LocalDate start, LocalDate end);

  /**
   * Creates a copy of this event for a target calendar.
   *
   * @param newStart the new start time in the target calendar's timezone
   * @return a new event instance
   */
  Ievent copyToNewStart(ZonedDateTime newStart);

  /**
   * Checks if this event is part of a recurring series.
   *
   * @return true if recurring
   */
  boolean isRecurring();

  /**
   * Gets the description of the event.
   *
   * @return the description
   */
  String getDescription();
}
