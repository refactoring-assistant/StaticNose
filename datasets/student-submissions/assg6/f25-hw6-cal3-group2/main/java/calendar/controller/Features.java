package calendar.controller;

import calendar.event.EventContext;
import calendar.event.EventWrapper;
import java.time.LocalDate;
import java.util.Collection;

/**
 * The features for GUI functions.
 */
public interface Features {

  /**
   * To create a calendar for GUI.
   *
   * @param calendarName the calendar name
   * @param timeZone the timezone of the calendar
   */
  void createCalendar(String calendarName, String timeZone);

  /**
   * Create a new single event in the current calendar.
   *
   * @param context the EventContext containing all event properties (subject, start/end time,
   *                description, location, status)
   */
  void createEvent(EventContext context);

  /**
   * Create a new recurring event series in the current calendar.
   *
   * @param context the EventContext containing event properties for the series
   * @param weekdays a string specifying days to repeat
   * @param occurrences the number of times the event should occur
   * @param seriesEndDate the last date for the series
   */
  void createEventSeries(EventContext context,
                         String weekdays, int occurrences, String seriesEndDate);

  /**
   * To get a collection of all existing calendar names.
   *
   * @return the collection of all existing calendar names
   */
  Collection<String> getAllCalendarNames();

  /**
   * To switch to the designated calendar.
   *
   * @param calendarName the aimed calendar name
   */
  void switchCalendar(String calendarName);

  /**
   * Edit an existing single event in a specified calendar.
   *
   * @param subject the current subject/name to edit
   * @param startDateTime the current start date/time of the event
   * @param endDateTime the current end date/time of the event
   * @param newContext the EventContext containing new values for event properties
   * @param calendarName the name of the calendar containing the event
   */
  void editEvent(String subject, String startDateTime, String endDateTime,
                 EventContext newContext, String calendarName);

  /**
   * Edit a property (name/timezone) of an existing calendar.
   *
   * @param calendarName the name of the calendar to edit
   * @param propertyName the property to edit ("name"/"timezone")
   * @param propertyValue the new value for the property
   */
  void editCalendarProperty(String calendarName, String propertyName, String propertyValue);

  /**
   * Search for events across calendar that match the given keyword.
   *
   * @param keyword the search term to match
   * @return a collection of EventDecorators containing matching events
   */
  Collection<EventWrapper> getEventsAcrossCalendar(String keyword);

  /**
   * Navigate to a specific month and year.
   *
   * @param year  the year to navigate to
   * @param month the month to navigate to
   */
  void navigateToMonth(int year, int month);

  /**
   * Export the current calendar to a file.
   *
   * @param fileName the file name (with .csv or .ics extension)
   */
  void exportCalendar(String fileName);

  /**
   * View all events on a specific date.
   *
   * @param date the date to view events for
   */
  void viewEventsOnDate(LocalDate date);

  /**
   * Get the name of the currently active calendar.
   *
   * @return string of name of the current calendar
   */
  String getCurrentCalendarName();

  /**
   * Get the timezone of the currently active calendar.
   *
   * @return string of timezone ID of the current calendar
   */
  String getCurrentCalendarTimezone();

  /**
   * Edit an event series in a specified calendar.
   * Modify subject/startTime/endTime from the current events or
   * modify description/location/status for the entire series.
   *
   * @param subject the current subject of an event
   * @param startDateTime the start date/time of the event occurrence to edit
   * @param endDateTime the end date/time of the event occurrence
   * @param newContext the EventContext containing new values for series properties
   * @param calendarName the name of the calendar containing the series
   */
  void editEventSeries(String subject, String startDateTime, String endDateTime,
                       EventContext newContext, String calendarName);
}
