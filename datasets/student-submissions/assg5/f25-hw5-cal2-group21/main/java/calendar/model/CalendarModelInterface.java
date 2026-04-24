package calendar.model;

import calendar.Property;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * outlines methods required for classes implementing CalendarModel interface.
 */
public interface CalendarModelInterface {

  /**
   * Method to create event with specified subject, start date/time, and end date/time.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date/time of the event
   * @param endDateTime   the end date/time of the event
   * @throws IllegalArgumentException if any required field is null or if event already exists with
   *                                  specified fields
   */
  void createEvent(String subject, Date startDateTime, Date endDateTime)
      throws IllegalArgumentException;

  /**
   * Method to create an event at specified start and end time which repeats on specified week days
   * a specified number of times.
   *
   * @param subject       the subject of the events
   * @param startDateTime the earliest possible start date time
   * @param endDateTime   the earliest possible end date time
   * @param weekdays      the weekdays it repeats on
   * @param repeat        the number of times it repeats
   * @throws IllegalArgumentException if required field is null, weekday array is invalid,
   repeat is less than 1, or event with outlined fields already exists at any day in series
   */
  void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      int repeat) throws IllegalArgumentException;

  /**
   * Method to create an event at specified start and end time which repeats on specified week days
   * until a specified date.
   *
   * @param subject       the subject of the events
   * @param startDateTime the earliest possible start date time
   * @param endDateTime   the earliest possible end date time
   * @param weekdays      the weekdays it repeats on
   * @param untilDate     the date it repeats until (inclusive)
   * @throws IllegalArgumentException if required field is null, weekday array is invalid,
   until date is before start, or event with outlined fields already exists at any day in series
   */
  void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate) throws IllegalArgumentException;

  /**
   * Creates an event from 8AM to 5PM on specified day.
   *
   * @param subject   the subject of the event
   * @param startDate the date of the event
   * @throws IllegalArgumentException if required field is null or if event already exists with
   *                                  specified fields
   */
  void createAllDayEvent(String subject, Date startDate) throws IllegalArgumentException;

  /**
   * Create series of events from 8AM to 5PM starting on specified date and repeating on specified
   * weekdays a specified number of times.
   *
   * @param subject   the subject of the events
   * @param startDate the earliest possible start date
   * @param weekdays  the weekdays to repeat on
   * @param repeat    the number of times to repeat
   * @throws IllegalArgumentException if required field is null, weekday array is invalid,
   repeat is less than 1, or event with outlined fields already exists at any day in series
   */
  void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat)
      throws IllegalArgumentException;

  /**
   * Create series of events from 8AM to 5PM starting on specified date and repeating on specified
   * weekdays until a specified date.
   *
   * @param subject   the subject of the events
   * @param startDate the earliest possible start date of the event
   * @param weekdays  the weekdays to repeat on
   * @param untilDate the date to repeat until (inclusive)
   * @throws IllegalArgumentException if required field is null, weekday array is invalid,
   until date is before start, or event with outlined fields already exists at any day in series
   */
  void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate)
      throws IllegalArgumentException;

  /**
   * Edit specified property to specified new value of event at given start date/time, end date/time
   * and with given subject.
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
   * Edit specified property to specified new value of event starting at specified start date/time
   * with specified subject. If event is in a series, also make edit for all events in series
   * after.
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
   * with specified subject. If event is in a series, also make edit for all events in series.
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
   * Method collects set of events on specified date.
   *
   * @param date date to search
   * @return events on searched date
   */
  Set<Event> getEventsOnDate(Date date);

  /**
   * Method collects set of events between given specified range.
   *
   * @param startDateTime start date/time of range
   * @param endDateTime   end date/time of range
   * @return set with all events fully or partially inside range
   */
  Set<Event> getEventsInRange(Date startDateTime, Date endDateTime);

  /**
   * Returns boolean representing if there is an event at specified date and time.
   *
   * @param dateTime date and time to search
   * @return if there is an event at this date and time
   */
  boolean eventAt(Date dateTime);

  /**
   * get method for timezone field.
   *
   * @return timezone of calendar
   */
  TimeZone getTimeZone();

  /**
   * Set method of timezone field.
   *
   * @param timeZone new value of timezone
   */
  void setTimeZone(TimeZone timeZone);
}
