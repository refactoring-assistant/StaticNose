package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a series of recurring events in the calendar.
 * Manages the generation and retrieval of events that repeat on specific days.
 */
public interface EventSeries {

  /**
   * Gets the unique identifier for this event series.
   *
   * @return the series ID
   */
  String getSeriesId();

  /**
   * Gets the subject of the events in this series.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Gets the start date and time for events in this series.
   *
   * @return the start date and time
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end date and time for events in this series.
   *
   * @return the end date and time
   */
  LocalDateTime getEndDateTime();

  /**
   * Gets the number of times this series repeats.
   *
   * @return the repeat count, or null if repeating until a specific date
   */
  Integer getRepeatCount();

  /**
   * Gets the date until which this series repeats.
   *
   * @return the repeat until date, or null if repeating a specific number of times
   */
  LocalDate getRepeatUntil();

  /**
   * Gets the status of the events in this series.
   *
   * @return the event status (PUBLIC or PRIVATE)
   */
  Status getStatus();

  /**
   * Gets the days of the week on which events in this series occur.
   *
   * @return list of weekdays
   */
  List<DayOfWeek> getWeekdays();

  /**
   * Gets the location of the events in this series.
   *
   * @return the location, or empty string if none
   */
  String getLocation();

  /**
   * Gets the description of the events in this series.
   *
   * @return the description, or empty string if none
   */
  String getDescription();

  /**
   * Generates all individual events based on the series parameters.
   */
  void generateEvents();

  /**
   * Gets all events that have been generated for this series.
   *
   * @return list of all events in the series
   */
  List<Event> getAllEvents();

  /**
   * Gets all events in the series starting from a specific event onwards.
   *
   * @param event the event to start from
   * @return list of events from the specified event onwards
   */
  List<Event> getEventsStartingFrom(Event event);
}