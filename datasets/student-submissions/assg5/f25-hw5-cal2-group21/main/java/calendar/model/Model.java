package calendar.model;

import calendar.CalendarProperty;
import calendar.Property;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * Interface for wrapper that acts as the model.
 */
public interface Model {

  /**
   * Method to create event for currently active Calendar.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date/time of the event
   * @param endDateTime   the end date/time of the event
   * @throws IllegalArgumentException if any required field is null or if event already exists with
   *                                  specified fields
   * @throws IllegalStateException    if no calendar currently active
   */
  void createEvent(String subject, Date startDateTime, Date endDateTime)
      throws IllegalArgumentException;

  /**
   * Method to create event for specified Calendar.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date/time of the event
   * @param endDateTime   the end date/time of the event
   * @param name          name of calendar to perform action on
   * @throws IllegalArgumentException if any required field is null or if event already exists with
   *                                  specified fields
   * @throws IllegalStateException    if no calendar exists with name
   */
  void createEvent(String subject, Date startDateTime, Date endDateTime, String name)
      throws IllegalArgumentException;

  /**
   * Method to create an event at specified start and end time which repeats on specified week days
   * a specified number of times for currently active Calendar.
   *
   * @param subject       the subject of the events
   * @param startDateTime the earliest possible start date time
   * @param endDateTime   the earliest possible end date time
   * @param weekdays      the weekdays it repeats on
   * @param repeat        the number of times it repeats
   * @throws IllegalArgumentException if required field is null, weekday array is invalid, or event
   *                                  with outlined fields already exists at any day in series
   */
  void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      int repeat) throws IllegalArgumentException;

  /**
   * Method to create an event at specified start and end time which repeats on specified week days
   * a specified number of times for specified Calendar.
   *
   * @param subject       the subject of the events
   * @param startDateTime the earliest possible start date time
   * @param endDateTime   the earliest possible end date time
   * @param weekdays      the weekdays it repeats on
   * @param repeat        the number of times it repeats
   * @param name          the name of the calendar to edit
   * @throws IllegalArgumentException if required field is null, weekday array is invalid, event
   *                                  with outlined fields already exists at any day in series, or
   *                                  if no calendar exists with name
   */
  void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      int repeat, String name) throws IllegalArgumentException;


  /**
   * Method to create an event at specified start and end time which repeats on specified week days
   * until a specified date for currently active Calendar.
   *
   * @param subject       the subject of the events
   * @param startDateTime the earliest possible start date time
   * @param endDateTime   the earliest possible end date time
   * @param weekdays      the weekdays it repeats on
   * @param untilDate     the date it repeats until (inclusive)
   * @throws IllegalArgumentException if required field is null, weekday array is invalid, or event
   *                                  with outlined fields already exists at any day in series
   */
  void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate) throws IllegalArgumentException;

  /**
   * Method to create an event at specified start and end time which repeats on specified week days
   * until a specified date for specified Calendar.
   *
   * @param subject       the subject of the events
   * @param startDateTime the earliest possible start date time
   * @param endDateTime   the earliest possible end date time
   * @param weekdays      the weekdays it repeats on
   * @param untilDate     the date it repeats until (inclusive)
   * @param name          the name fo the calendar to edit
   * @throws IllegalArgumentException if required field is null, weekday array is invalid, event
   *                                  with outlined fields already exists at any day in series, or
   *                                  no calendar exists with name
   */
  void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate, String name) throws IllegalArgumentException;


  /**
   * Creates an event from 8AM to 5PM on specified day for currently active Calendar..
   *
   * @param subject   the subject of the event
   * @param startDate the date of the event
   * @throws IllegalArgumentException if required field is null or if event already exists with
   *                                  specified fields
   */
  void createAllDayEvent(String subject, Date startDate) throws IllegalArgumentException;


  /**
   * Creates an event from 8AM to 5PM on specified day for currently active Calendar.
   *
   * @param subject   the subject of the event
   * @param startDate the date of the event
   * @param name      the name of the calendar to edit
   * @throws IllegalArgumentException if required field is null, if event already exists with
   *                                  specified fields, or if no calendar with name exists
   */
  void createAllDayEvent(String subject, Date startDate, String name)
      throws IllegalArgumentException;


  /**
   * Create series of events from 8AM to 5PM starting on specified date and repeating on specified
   * weekdays a specified number of times for currently active Calendar.
   *
   * @param subject   the subject of the events
   * @param startDate the earliest possible start date
   * @param weekdays  the weekdays to repeat on
   * @param repeat    the number of times to repeat
   * @throws IllegalArgumentException if required field is null or if event exists with specified
   *                                  fields at any point in the series
   */
  void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat)
      throws IllegalArgumentException;

  /**
   * Create series of events from 8AM to 5PM starting on specified date and repeating on specified
   * weekdays a specified number of times for specified Calendar.
   *
   * @param subject   the subject of the events
   * @param startDate the earliest possible start date
   * @param weekdays  the weekdays to repeat on
   * @param repeat    the number of times to repeat
   * @param name      the name of the calendar to edit
   * @throws IllegalArgumentException if required field is null or if event exists with specified
   *                                  fields at any point in the series
   */
  void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat,
      String name) throws IllegalArgumentException;

  /**
   * Create series of events from 8AM to 5PM starting on specified date and repeating on specified
   * weekdays until a specified date for currently active Calendar.
   *
   * @param subject   the subject of the events
   * @param startDate the earliest possible start date of the event
   * @param weekdays  the weekdays to repeat on
   * @param untilDate the date to repeat until (inclusive)
   * @throws IllegalArgumentException if required field is null or if event exists with specified
   *                                  days at any point in the series
   */
  void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate)
      throws IllegalArgumentException;

  /**
   * Create series of events from 8AM to 5PM starting on specified date and repeating on specified
   * weekdays until a specified date for specified Calendar.
   *
   * @param subject   the subject of the events
   * @param startDate the earliest possible start date of the event
   * @param weekdays  the weekdays to repeat on
   * @param untilDate the date to repeat until (inclusive)
   * @param name      the name of the calendar to edit
   * @throws IllegalArgumentException if required field is null, if event exists with specified days
   *                                  at any point in the series, or if no calendar exists with
   *                                  name
   */
  void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate,
      String name) throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event at given start date/time, end date/time
   * and with given subject for currently active Calendar.
   *
   * @param property      the property to edit
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param endDateTime   the end date/time of the event to edit
   * @param newProperty   the new value of the property
   * @throws IllegalArgumentException if event doesn't exist or if edit would violate rules of event
   *                                  property uniqueness
   */
  void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty) throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event at given start date/time, end date/time
   * and with given subject for specified Calendar.
   *
   * @param property      the property to edit
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param endDateTime   the end date/time of the event to edit
   * @param newProperty   the new value of the property
   * @param name          the name of the calendar to edit
   * @throws IllegalArgumentException if event doesn't exist, if edit would violate rules of event
   *                                  property uniqueness, or if no calendar with given name exists
   */
  void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty, String name) throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event starting at specified start date/time
   * with specified subject. If event is in a series, also make edit for all events in series after
   * for currently active Calendar.
   *
   * @param property      the property to edit
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param newProperty   the new value of the property to set
   * @throws IllegalArgumentException if event doesn't exist or if edit will result in violation
   *                                  rules of event property uniqueness for any event in the
   *                                  series
   */
  void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty) throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event starting at specified start date/time
   * with specified subject. If event is in a series, also make edit for all events in series after
   * for specified Calendar.
   *
   * @param property      the property to edit
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param newProperty   the new value of the property to set
   * @param name          the name of the calendar to edit
   * @throws IllegalArgumentException if event doesn't exist, if edit will result in violation rules
   *                                  of event property uniqueness for any event in the series, or
   *                                  if no calendar with name exists
   */
  void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty, String name) throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event starting at specified start date/time
   * with specified subject. If event is in a series, also make edit for all events in series for
   * currently active Calendar.
   *
   * @param property      the property to edit
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param newProperty   the new value of the property to set
   * @throws IllegalArgumentException if event doesn't exist or if edit will result in violation
   *                                  rules of event property uniqueness for any event in the
   *                                  series
   */
  void editSeries(Property property, String subject, Date startDateTime, Object newProperty)
      throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event starting at specified start date/time
   * with specified subject. If event is in a series, also make edit for all events in series for
   * specified Calendar.
   *
   * @param property      the property to edit
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param newProperty   the new value of the property to set
   * @param name          the name of the calendar to edit
   * @throws IllegalArgumentException if event doesn't exist, if edit will result in violation rules
   *                                  of event property uniqueness for any event in the series, or
   *                                  if no calendar with name exists
   */
  void editSeries(Property property, String subject, Date startDateTime, Object newProperty,
      String name)
      throws IllegalArgumentException;

  /**
   * Method collects set of events on specified date for currently active Calendar.
   *
   * @param date date to search
   * @return events on searched date
   */
  Set<Event> getEventsOnDate(Date date);

  /**
   * Method collects set of events on specified date for specified Calendar.
   *
   * @param date date to search
   * @param name the name of the calendar to edit
   * @return events on searched date
   * @throws IllegalArgumentException if no calendar with name exists
   */
  Set<Event> getEventsOnDate(Date date, String name) throws IllegalArgumentException;

  /**
   * Method collects set of events between given specified range for currently active Calendar.
   *
   * @param startDateTime start date/time of range
   * @param endDateTime   end date/time of range
   * @return set with all events fully or partially inside range
   */
  Set<Event> getEventsInRange(Date startDateTime, Date endDateTime);

  /**
   * Method collects set of events between given specified range for specified Calendar.
   *
   * @param startDateTime start date/time of range
   * @param endDateTime   end date/time of range
   * @param name          the name of the calendar to edit
   * @return set with all events fully or partially inside range
   * @throws IllegalArgumentException if no calendar with name exists
   */
  Set<Event> getEventsInRange(Date startDateTime, Date endDateTime, String name)
      throws IllegalArgumentException;

  /**
   * Returns boolean representing if there is an event at specified date and time for currently
   * active Calendar.
   *
   * @param dateTime date and time to search
   * @return if there is an event at this date and time
   */
  boolean eventAt(Date dateTime);

  /**
   * Returns boolean representing if there is an event at specified date and time for specified
   * Calendar.
   *
   * @param dateTime date and time to search
   * @param name     the name of the calendar to edit
   * @return if there is an event at this date and time
   * @throws IllegalArgumentException if no calendar with name exists
   */
  boolean eventAt(Date dateTime, String name) throws IllegalArgumentException;

  /**
   * Method to create new calendar with specified name and timezone.
   *
   * @param name     name of calendar
   * @param timeZone timezone of calendar
   * @throws IllegalArgumentException if calendar already exists with name
   */
  void createCalendar(String name, TimeZone timeZone) throws IllegalArgumentException;

  /**
   * Method to switch active calendar.
   *
   * @param name name of calendar to make active
   * @throws IllegalArgumentException if calendar with name doesn't exist
   */
  void useCalendar(String name) throws IllegalArgumentException;

  /**
   * Method to edit property of calendar.
   *
   * @param calendarProperty property to edit
   * @param name             name of calendar to edit
   * @param newProperty      new value of property
   * @throws IllegalArgumentException if calendar with name doesn't exist
   */
  void editCalendar(CalendarProperty calendarProperty, String name, Object newProperty)
      throws IllegalArgumentException;


  /**
   * gets the timezone of currently active calendar.
   *
   * @return the timezone of active calendar
   * @throws IllegalStateException if there is no active calendar
   */
  TimeZone getTimeZone() throws IllegalStateException;

  /**
   * gets the timezone of the specified calendar.
   *
   * @param name name of calendar to get timezone of
   * @return the timezone of specified calendar
   * @throws IllegalArgumentException if no calendar with that name exists
   */
  TimeZone getTimeZone(String name) throws IllegalArgumentException;


}
