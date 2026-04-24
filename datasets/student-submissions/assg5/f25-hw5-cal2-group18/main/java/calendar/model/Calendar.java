package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Represents a calendar that can store and manage multiple events.
 */
public interface Calendar {

  /**
   * Adds a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event is null or already exists
   */
  void addEvent(Event event) throws IllegalArgumentException;

  /**
   * Creates and adds a recurring event series to the calendar.
   *
   * @param event       the base event
   * @param weekDays    the days to repeat on
   * @param occurrences the number of times to repeat
   * @return the series ID for this series
   * @throws IllegalArgumentException if parameters are invalid
   */
  String addEventSeries(Event event, List<WeekDay> weekDays, int occurrences)
      throws IllegalArgumentException;

  /**
   * Creates and adds a recurring event series until the specified end date.
   *
   * @param event    the base event
   * @param weekDays the days to repeat on
   * @param endDate  last date to create event
   * @return the series ID for this series
   * @throws IllegalArgumentException if parameters are invalid
   */
  String addEventSeriesUntil(Event event, List<WeekDay> weekDays, LocalDate endDate)
      throws IllegalArgumentException;

  /**
   * Finds and returns the requested event.
   *
   * @param subject       subject of the event
   * @param startDateTime the start date and time
   * @return matching event or null if not found
   */
  Event findEvent(String subject, LocalDateTime startDateTime);

  /**
   * Finds and returns the requested event.
   *
   * @param subject       subject of the event
   * @param startDateTime the start date and time
   * @return list of matching event or null if not found
   */
  List<Event> findEvents(String subject, LocalDateTime startDateTime);

  /**
   * Edits a single event.
   *
   * @param subject       subject of the event to edit
   * @param startDateTime the start time of the event to edit
   * @param property      the property to change
   * @param newValue      the changed value for that property
   * @throws IllegalArgumentException if the parameters are invalid
   */
  void editEvent(String subject, LocalDateTime startDateTime, String property, String newValue)
      throws IllegalArgumentException;

  /**
   * Edits an event and all future events in its series.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if parameters are invalid
   */
  void editEventsFromThisForward(String subject, LocalDateTime startDateTime, String property,
                                 String newValue) throws IllegalArgumentException;

  /**
   * Edits all events in a series.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start time of the event to edit
   * @param property      the property to change
   * @param newValue      the new value for the property
   * @throws IllegalArgumentException if parameters are invalid
   */
  void editEntireSeries(String subject, LocalDateTime startDateTime, String property,
                        String newValue) throws IllegalArgumentException;

  /**
   * Retyrns all the events scheduled on this date.
   *
   * @param date the query date
   * @return list of events on that date
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Gets all events that occur within a date or time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return list of events in the range
   */
  List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date/time to check
   * @return true if there are any events at that time
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events (empty if none)
   */
  List<Event> getAllEvents();

  /**
   * Gets all events in a specific series.
   *
   * @param seriesId the series ID
   * @return list of events in that series
   */
  List<Event> getEventsBySeries(String seriesId);

  /**
   * Finds an event by subject, start time, and end time.
   *
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @param endDateTime   the end date/time
   * @return the event if found, null otherwise
   */
  Event findEventByTimes(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime);

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   */
  void removeEvent(Event event);

  /**
   * Clears all events from the calendar.
   */
  void clear();

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  String getName();

  /**
   * Sets the name of this calendar.
   *
   * @param name the new calendar name
   * @throws IllegalArgumentException if name is null or empty
   */
  void setName(String name);

  /**
   * Gets the timezone of this calendar.
   *
   * @return the calendar's timezone
   */
  ZoneId getTimezone();

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone the new timezone
   * @throws IllegalArgumentException if timezone is null
   */
  void setTimezone(ZoneId timezone);
}

