package calendar.model;


import java.util.List;

/**
 * Calender Interface.
 */
public interface IcalendarModel {
  /**
   * Creates a new all-day event.
   *
   * @param subject subject the event subject
   * @param date    the date of teh event(YYYY-MM-DD)
   * @throws Exception if the event cannot be created
   */
  void createAllDayEvent(String subject, String date) throws Exception;

  /**
   * Creates a new timed event.
   *
   * @param subject       the event subject
   * @param startDateTime the start time
   * @param endDateTime   the end time
   * @throws Exception if the event cannot be created
   */
  void createTimedEvent(String subject, String startDateTime, String endDateTime) throws
      Exception;

  /**
   * Created a recurring event.
   *
   * @param subject       the event subject
   * @param startDateTime he start time
   * @param endDateTime   the end time
   * @param repeatDays    the days to repeat
   * @param occurrences   the no of occurences
   * @throws Exception if the event cannot be created
   */
  void createRecurringEvent(String subject, String startDateTime, String endDateTime,
                            String repeatDays, int occurrences) throws
      Exception;

  /**
   * Create a recurring event that repeats until a specified end date.
   *
   * @param subject       the event subject
   * @param startDateTime he start time
   * @param endDateTime   the end time
   * @param repeatDays    the days to repeat
   * @throws Exception if the event cannot be created
   */
  void createRecurringEventUntil(String subject, String startDateTime, String endDateTime,
                                 String repeatDays, String endDate) throws
      Exception;

  /**
   * Creates a recurring all-day event for specified no of occurences.
   *
   * @param subject     the event subject
   * @param repeatDays  the days to repeat
   * @param occurrences the no of occurences
   * @throws Exception if the event cannot be created
   */
  void createAllDayRecurringEvent(String subject, String startDate, String repeatDays,
                                  int occurrences) throws
      Exception;

  /**
   * Creates a recurring all-day event that repeats until a given day.
   *
   * @param subject    the event subject
   * @param repeatDays the days to repeat
   * @throws Exception if the event cannot be created
   */
  void createAllDayRecurringEventUntil(String subject, String startDate, String repeatDays,
                                       String endDate) throws
      Exception;

  /**
   * Edits a single event property.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param property      te property to edit
   * @param newValue      the new value to add
   * @throws Exception if the event cannot be edited
   */
  void editEvent(String subject, String startDateTime, String property, String newValue) throws
      Exception;

  /**
   * /**
   * Edits all future events.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param property      te property to edit
   * @param newValue      the new value to add
   * @throws Exception if the event cannot be edited
   */
  void editEventsFrom(String subject, String startDateTime, String property, String newValue) throws
      Exception;

  /**
   * Edits all events in the series.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param property      te property to edit
   * @param newValue      the new value to add
   * @throws Exception if the event cannot be edited
   */
  void editSeries(String subject, String startDateTime, String property, String newValue) throws
      Exception;

  /**
   * Returns a list of all events occurring on a given date.
   *
   * @param date the date to check
   * @return a list of events on the specified date
   * @throws Exception the events cannot be retrieved
   */
  List<Event> getEventsOnDate(String date) throws Exception;

  /**
   * Returns all events ocurring within a given time interval.
   *
   * @return a list of events on the specified date
   * @throws Exception the events cannot be retrieved
   */

  List<Event> getEventsInInterval(String startDateTime, String endDateTime) throws
      Exception;

  /**
   * checks if there is an event at given date and time.
   *
   * @param dateTime the date and time to check
   * @return true is time is busy, false otherwise
   * @throws Exception if the check cannot be performed
   */
  boolean isBusyAt(String dateTime) throws Exception;

  /**
   * Export all events to aCSV file.
   *
   * @return a message
   * @throws Exception if the file cannot be exported
   */
  String exportToCsv() throws Exception;

  /**
   * Returns all the events currently stored.
   *
   * @return a lsit of all events
   */
  List<Event> getAllEvents();

  /**
   * Copies a single event instance to a target calendar.
   *
   * @param subject the name of the event
   * @param sourceStartDateTime the start date/time of the event instance
   * @param targetCalName the name of the target calendar
   * @param targetStartDateTime the start date/time for the copied event in the target calendar
   * @throws Exception if the event is not found
   */
  void copyEvent(String subject, String sourceStartDateTime,
                 String targetCalName, String targetStartDateTime) throws Exception;

  /**
   * Copies all events occurring on a specific date to a target calendar.
   *
   * @param sourceDateStr the source date
   * @param targetCalName the name of the target calendar
   * @param targetDateStr the target date in the target calendar
   * @throws Exception if the target calendar doesn't exist
   */
  void copyEventsOnDate(String sourceDateStr, String targetCalName,
                        String targetDateStr) throws Exception;

  /**
   * Copies all events within a date interval to a target calendar.
   *
   * @param sourceStartStr the start date of the interval
   * @param sourceEndStr the end date of the interval
   * @param targetCalName the name of the target calendar
   * @param targetDateStr the starting date in the target calendar
   * @throws Exception if the target calendar doesn't exist
   */
  void copyEventsInInterval(String sourceStartStr, String sourceEndStr,
                            String targetCalName, String targetDateStr) throws Exception;

  /**
   * Exports all events in the active calendar to an iCal formatted string.
   *
   * @return the iCal string content.
   * @throws Exception if the export fails.
   */
  String exportToIcal() throws Exception;

  /**
   * Gets the name of the currently active calendar.
   */
  String getActiveCalendarName() throws Exception;


  /**
   * Creates a new calendar with specified name and timezone.
   *
   * @param name   the name of the new calendar
   * @param zoneId the zoneId (String) of the new calendar
   * @throws Exception if the name already exist
   */
  void createCalendar(String name, String zoneId) throws Exception;

  /**
   * Updates an existing calendar's timezone.
   *
   * @param name      the name of the calendar to update
   * @param newzoneId the new timezone (String) to assign
   * @throws Exception if the calendar doesn't exist
   */
  void updateCalendarTimeZone(String name, String newzoneId) throws Exception;

  /**
   * Sets the specified calendar as active.
   *
   * @param name the name of the calendar to be set as active
   * @throws Exception if the calendar doesn't exist
   */
  void setActiveCalendar(String name) throws Exception;

  /**
   * Deletes a single event instance matching the subject and exact start date/time.
   *
   * @param subject       the event subject
   * @param startDateTime the exact start date/time of the instance to delete (YYYY-MM-DDTHH:MM)
   * @throws Exception if the event cannot be deleted or is not found
   */
  void deleteEvent(String subject, String startDateTime) throws Exception;

  /**
   * Deletes all future events for a series starting from a specified date/time.
   *
   * @param subject       the event subject
   * @param startDateTime the date/time from which to start deleting occurrences (YYYY-MM-DDTHH:MM)
   * @throws Exception if the events cannot be deleted or the series is not found
   */
  void deleteEventsFrom(String subject, String startDateTime) throws Exception;

  /**
   * Deletes the entire series/all events with the specified subject.
   *
   * @param subject the event subject (name)
   * @throws Exception if the series cannot be deleted or is not found
   */
  void deleteSeries(String subject) throws Exception;

  /**
   * Deletes the specified calendar.
   *
   * @param name the name of the calendar to delete
   * @throws Exception if the calendar doesn't exist or cannot be deleted
   */
  void deleteCalendar(String name) throws Exception;

  /**
   * Renames an existing calendar.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws Exception if the calendar doesn't exist or new name is taken
   */
  void renameCalendar(String oldName, String newName) throws Exception;

  /**
   * Returns a list of all existing calendar names.
   *
   * @return a list of calendar names
   */
  List<String> getAllCalendarNames();

}
