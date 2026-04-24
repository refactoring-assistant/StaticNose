package calendar.model;

import java.util.List;

/**
 * Defines the core operations of the calendar model.
 * This interface encapsulates all event-related functionality,
 * including creation, editing, exporting, and status checking.
 */
public interface Icalendar {

  /**
   * Creates a new calendar event with the specified subject and time range.
   *
   * @param subject the event title or name
   * @param startDateTime the event start time in ISO-8601 format (e.g., 2025-11-01T09:00)
   * @param endDateTime the event end time in ISO-8601 format (e.g., 2025-11-01T10:00)
   * @return the internal numeric ID of the created event, or -1 if a time conflict occurs
   */
  int createEvent(String subject, String startDateTime, String endDateTime);

  /**
   * Converts a single event into a recurring series based on the specified recurrence pattern.
   * Either the number of occurrences or an end date must be provided.
   *
   * @param eventId the ID of the base event to convert
   * @param weekdays a string representing the repeat pattern (e.g., "MWF", "R", "MRW")
   * @param occurrences the total number of times the event should repeat (may be null)
   * @param untilDate the date until which the event repeats (may be null)
   * @return a unique series identifier if recurrence is successfully created, otherwise null
   */
  String updateEventDetails(int eventId, String weekdays, Integer occurrences, String untilDate);

  /**
   * Edits a single event instance identified by subject and exact start and end date-time.
   * This applies to both one-time and recurring events, but only the matched instance is edited.
   *
   * @param subject the subject of the event
   * @param startDateTime the start date-time (ISO_LOCAL_DATE_TIME)
   * @param endDateTime the end date-time (ISO_LOCAL_DATE_TIME)
   * @param propertyName the name of the property to edit (e.g., "location", "description")
   * @param newPropertyValue the new value to assign to the property
   * @return the event ID if found and edited, -1 otherwise
   */
  int editSingleEventInstance(String subject, String startDateTime, String endDateTime,
                              String propertyName, String newPropertyValue);

  /**
   * Edits a series of events starting from a specific date-time.
   * If applyToFutureOnly is true, only events at or after the given time are updated.
   * If false, all events in the series are updated.
   * If the identified event is not recurring, only that instance is edited.
   *
   * @param subject the subject of the event
   * @param startDateTime the date-time from which edits should start (ISO_LOCAL_DATE_TIME)
   * @param propertyName the name of the property to edit
   * @param newPropertyValue the new value for the property
   * @param applyToFutureOnly true to edit only future events, false to edit the entire series
   * @return the event ID of the matched event, -1 if not found
   */
  int editSeriesEvents(String subject, String startDateTime,
                       String propertyName, String newPropertyValue,
                       boolean applyToFutureOnly);

  /**
   * Retrieves all events occurring on the specified date.
   *
   * @param dateString the date in ISO-8601 format (e.g., 2025-11-01)
   * @return a list of formatted event details for that date
   */
  List<String> printEventsOn(String dateString);

  /**
   * Retrieves all events within a given date-time interval.
   *
   * @param startDateTime the start of the interval in ISO-8601 format
   * @param endDateTime the end of the interval in ISO-8601 format
   * @return a list of formatted event details within the interval
   */
  List<String> printEventsInInterval(String startDateTime, String endDateTime);

  /**
   * Exports all events in the calendar to a CSV file that conforms to
   * Google Calendar import format.
   *
   * @param filename the desired CSV filename
   * @return the absolute path of the exported file
   */
  List<String[]> exportCalendar(String filename);

  /**
   * Checks whether the user is busy or available at the given date and time.
   *
   * @param dateTime the date-time to check (in ISO-8601 format)
   * @return true if there is a conflicting event; false otherwise
   */
  boolean checkBusyStatus(String dateTime);

}
