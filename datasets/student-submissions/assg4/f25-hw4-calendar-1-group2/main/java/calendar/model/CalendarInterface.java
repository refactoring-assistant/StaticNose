package calendar.model;


import calendar.enums.EventStatus;
import calendar.enums.UserStatus;
import calendar.event.Event;
import calendar.event.EventSeries;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
   * @param subject       the theme of the event on calendar (must specify)
   * @param startDateTime the start date and/or time of the event (must specify)
   * @param endDateTime   the end date and/or time of the event (must specify) if input is null,
   *                      note this event as an all-day event with default time from 8am to 5pm
   * @param description   a longer description of the event (optional)
   * @param location      the location of the event (optional)
   * @param eventStatus   the eventStatus (public/private) (optional)
   * @param seriesId      null by default; if this event is a single independent event, then null;
   *                      if belongs to a series, then not null
   * @return the single event that is created; if fail to create, return null
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  Event createSingleEvent(String subject, String startDateTime, String endDateTime,
                          String description, String location, String eventStatus,
                          String seriesId)
      throws IllegalArgumentException;

  /**
   * To create an event series with necessary subject, startDateTime, endDateTime and optional
   * properties. All events in an event series are required to have the same start time.
   * An event series must specify its occurrences in which weekdays, and then specify
   * repeatTimes or until a specific seriesEndDateTime.
   * A single event in a series can only span one day (it must start and finish on the same day).
   * Two events in the calendar cannot have the same subject, start date/time and end date/time.
   *
   * @param subject           the theme of the event series on calendar (must specify)
   * @param startDateTime     the start date and/or time of the event series (must specify)
   * @param endDateTime       the end date and/or time of the event series (must specify)
   *                          if input is null, note every event in this series as an all-day event
   *                          with default time range from 8am to 5pm
   * @param description       a longer description of the event series (optional)
   * @param location          the location of the event series (optional)
   * @param eventStatus       the eventStatus (public/private) (optional)
   * @param weekdays          (optional) a sequence of characters where each character denotes a day
   *                          of the event's occurrence e.g., MRU. 'M' is Monday, 'T' is Tuesday,
   *                          'W' is Wednesday, 'R' is Thursday, 'F' is Friday, 'S' is Saturday,
   *                          and 'U' is Sunday
   * @param repeatTimes       (optional) the int that denotes occurrences of the event in a week
   * @param seriesEndDateTime (optional) the end date and/or time of the series
   * @return the event series that is created; if fail to create, return null
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  EventSeries createEventSeries(String subject, String startDateTime, String endDateTime,
                                String description, String location, String eventStatus,
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
   * @param subject          the theme of the event to be edited on calendar
   * @param startDateTime    the start date and/or time of the event to be edited
   * @param endDateTime      the end date and/or time of the event to be edited
   * @param newSubject       the new theme of the event on calendar
   * @param newStartDateTime the new start date and/or time of the event on calendar
   * @param newEndDateTime   the new end date and/or time of the event on calendar
   * @param newDescription   the new description of the event
   * @param newLocation      the new location of the event
   * @param newEventStatus   the new eventStatus (public/private) of the event
   * @return the event that has been updated; null if edition fails
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  Event editSingleEvent(String subject, String startDateTime, String endDateTime,
                        String newSubject, String newStartDateTime, String newEndDateTime,
                        String newDescription, String newLocation, String newEventStatus)
      throws IllegalArgumentException;

  /**
   * To edit an event series given subject, startDateTime and endDateTime.
   * Error will show if the event series to be edited does not exist.
   *
   * @param subject          the theme of the event series to be edited on calendar
   * @param startDateTime    the start date and/or time of the event series to be edited
   * @param endDateTime      the end date and/or time of the event series to be edited
   * @param newSubject       the new theme of the event series on calendar
   * @param newStartDateTime the new start date and/or time of the event series on calendar
   * @param newEndDateTime   the new end date and/or time of the event series on calendar
   * @param newDescription   the new description of the event series
   * @param newLocation      the new location of the event series
   * @param newEventStatus   the new eventStatus (public/private) of the event series
   * @return the event series that has been updated; null if edition fails
   * @throws IllegalArgumentException if inputs are not illegally formatted
   */
  EventSeries editEventSeries(String subject, String startDateTime, String endDateTime,
                              String newSubject, String newStartDateTime, String newEndDateTime,
                              String newDescription, String newLocation, String newEventStatus)
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
   * @param end the end date & time of the query range
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
}
