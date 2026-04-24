package calendar.model;


import calendar.enums.EventStatus;
import calendar.enums.UserStatus;
import calendar.event.Event;
import calendar.event.EventContext;
import calendar.event.EventSeries;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

/**
 * This interface contains necessary operations that a calendar should support.
 */
public interface CalendarInterface {

  /**
   * To create a single event with necessary subject, startDateTime, endDateTime and optional
   * properties. If not specify endDateTime, the event will be an all-day event with
   * default time range from 8am to 5pm.
   * A single event may span across days.
   * Two events cannot have the same subject, start date/time and end date/time.
   *
   * @param context  the parameter object to pass event properties
   * @param seriesId null by default; if this event is a single independent event, then null;
   *                 if belongs to a series, then not null
   * @return the single event that is created; if fail to create, return null
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  Event createSingleEvent(EventContext context, String seriesId)
      throws IllegalArgumentException;

  /**
   * To create an event series with necessary subject, startDateTime, endDateTime and optional
   * properties. All events in an event series are required to have the same start time.
   * An event series must specify its occurrences in which weekdays, and then specify
   * repeatTimes or until a specific seriesEndDateTime.
   * A single event in a series can only span one day (it must start and finish on the same day).
   * Two events in the calendar cannot have the same subject, start date/time and end date/time.
   *
   * @param context           the parameter object to pass event properties
   * @param weekdays          (optional) a sequence of characters where each character denotes a day
   *                          of the event's occurrence e.g., MRU. 'M' is Monday, 'T' is Tuesday,
   *                          'W' is Wednesday, 'R' is Thursday, 'F' is Friday, 'S' is Saturday,
   *                          and 'U' is Sunday
   * @param repeatTimes       (optional) the int that denotes occurrences of the event in a week
   * @param seriesEndDateTime (optional) the end date and/or time of the series
   * @return the event series that is created; if fail to create, return null
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  EventSeries createEventSeries(EventContext context,
                                String weekdays, int repeatTimes, String seriesEndDateTime)
      throws IllegalArgumentException;

  /**
   * To get a single event with subject, startDateTime and optional properties given.
   *
   * @param subject       the theme of the event series on calendar (must specify)
   * @param startDateTime the start date and/or time of the event series (must specify)
   * @param endDateTime   the end date and/or time of the event series (must specify)
   *                      if input is null, search for an all-day event
   * @return the single event that is created; if fail to create, return null
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  Event getSingleEvent(String subject, String startDateTime, String endDateTime)
      throws IllegalArgumentException;

  /**
   * To edit a single event given subject, startDateTime and endDateTime.
   * Error will show if the event to be edited does not exist.
   *
   * @param subject       the theme of the event to be edited on calendar
   * @param startDateTime the start date and/or time of the event to be edited
   * @param endDateTime   the end date and/or time of the event to be edited
   * @param newContext    the parameter object to pass new event properties
   * @return the event that has been updated; null if edition fails
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  Event editSingleEvent(String subject, String startDateTime, String endDateTime,
                        EventContext newContext)
      throws IllegalArgumentException;

  /**
   * To edit an event series given subject, startDateTime and endDateTime.
   * Error will show if the event series to be edited does not exist.
   *
   * @param subject       the theme of the event series to be edited on calendar
   * @param startDateTime the start date and/or time of the event series to be edited
   * @param endDateTime   the end date and/or time of the event series to be edited
   * @param newContext    the parameter object to pass new event properties
   * @return the event series that has been updated; null if edition fails
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  EventSeries editEventSeries(String subject, String startDateTime, String endDateTime,
                              EventContext newContext)
      throws IllegalArgumentException;

  /**
   * Get all events that take place on the given date.
   *
   * @param date the date to query
   * @return a List of Event objects scheduled on the date, sorted by start time
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Retrieves all events within the given time range.
   *
   * @param start the start date & time of the query range
   * @param end   the end date & time of the query range
   * @return a List of Event objects within the range, sorted by start time
   */
  List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Show UserStatus on a given date and/or time.
   *
   * @param queryTime the given query time
   * @return If there exists event on the given date and/or time, return BUSY; else, AVAILABLE
   */
  UserStatus getUserStatus(LocalDateTime queryTime);

  /**
   * Exports all events in the calendar to a CSV formatted string.
   *
   * @return a String representing the full CSV content, including headers.
   */
  String exportToCsv();

  /**
   * Exports all events in the calendar to iCal format.
   *
   * @param calendarName the name of the calendar
   * @param timezone     the timezone of the calendar
   * @return an iCal formatted string
   */
  String exportToIcal(String calendarName, ZoneId timezone);

  /**
   * Get all events in the calendar.
   *
   * @return the collection of events.
   */
  Collection<Event> getEvents();

  /**
   * Get the event series with given seriesId.
   *
   * @param seriesId the seriesId to identify an event series
   * @return the event series with given seriesId
   */
  EventSeries getEventSeries(String seriesId);

  /**
   * Convert events in the calendar to a new timezone.
   *
   * @param oldZone the old timezone
   * @param newZone the new timezone
   */
  void convertTimezone(ZoneId oldZone, ZoneId newZone);
}
