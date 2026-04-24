package calendar.model;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * Interface for a calendar that manages events and event series.
 * Supports creating, editing, querying, and exporting calendar events.
 */
public interface IntCalendar {
  /**
   * Creates a single event with start and end date/time.
   *
   * @param subject     the subject of the event
   * @param startDate   the start date
   * @param startTime   the start time
   * @param endDate     the end date
   * @param endTime     the end time
   * @param description the description
   * @param location    the location
   * @param status      the status
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
                   String description, Location location, Status status);

  /**
   * Creates a single event with start and end date/time and description.
   *
   * @param subject     the subject of the event
   * @param startDate   the start date
   * @param startTime   the start time
   * @param endDate     the end date
   * @param endTime     the end time
   * @param description the description
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
                   String description);

  /**
   * Creates a single event with start and end date/time and location.
   *
   * @param subject   the subject of the event
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   * @param location  the location
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
                   Location location);

  /**
   * Creates a single event with start and end date/time and status.
   *
   * @param subject   the subject of the event
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   * @param status    the status
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
                   Status status);

  /**
   * Creates a single event with start and end date/time (without optional fields).
   *
   * @param subject   the subject of the event
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime);

  /**
   * Creates a single all-day event (8am to 5pm).
   *
   * @param subject     the subject of the event
   * @param date        the date of the event
   * @param description the description
   * @param location    the location
   * @param status      the status
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createAllDayEvent(String subject, Date date, String description,
                         Location location, Status status);

  /**
   * Creates a single all-day event (8am to 5pm) with description.
   *
   * @param subject     the subject of the event
   * @param date        the date of the event
   * @param description the description
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createAllDayEvent(String subject, Date date, String description);

  /**
   * Creates a single all-day event (8am to 5pm) with location.
   *
   * @param subject  the subject of the event
   * @param date     the date of the event
   * @param location the location
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createAllDayEvent(String subject, Date date, Location location);

  /**
   * Creates a single all-day event (8am to 5pm) with status.
   *
   * @param subject the subject of the event
   * @param date    the date of the event
   * @param status  the status
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createAllDayEvent(String subject, Date date, Status status);

  /**
   * Creates a single all-day event (8am to 5pm) without optional fields.
   *
   * @param subject the subject of the event
   * @param date    the date of the event
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void createAllDayEvent(String subject, Date date);

  /**
   * Creates an event series that repeats for a specific number of occurrences.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param description the description
   * @param location    the location
   * @param status      the status
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, int occurrences, String description,
                         Location location, Status status);

  /**
   * Creates an event series that repeats for a specific number of occurrences with description.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param description the description
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, int occurrences, String description);

  /**
   * Creates an event series that repeats for a specific number of occurrences with location.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param location    the location
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, int occurrences, Location location);

  /**
   * Creates an event series that repeats for a specific number of occurrences with status.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @param status      the status
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, int occurrences, Status status);

  /**
   * Creates an event series that repeats for a specific number of occurrences
   * (without optional fields).
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param occurrences the number of occurrences
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, int occurrences);

  /**
   * Creates an event series that repeats until a specific date.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param endDate     the end date (inclusive)
   * @param description the description
   * @param location    the location
   * @param status      the status
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, Date endDate, String description,
                         Location location, Status status);

  /**
   * Creates an event series that repeats until a specific date with description.
   *
   * @param subject     the subject of the events
   * @param startDate   the start date of the first event
   * @param startTime   the start time for all events
   * @param endTime     the end time for all events
   * @param repeatDays  the days of the week to repeat on
   * @param endDate     the end date (inclusive)
   * @param description the description
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, Date endDate, String description);

  /**
   * Creates an event series that repeats until a specific date with location.
   *
   * @param subject    the subject of the events
   * @param startDate  the start date of the first event
   * @param startTime  the start time for all events
   * @param endTime    the end time for all events
   * @param repeatDays the days of the week to repeat on
   * @param endDate    the end date (inclusive)
   * @param location   the location
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, Date endDate, Location location);

  /**
   * Creates an event series that repeats until a specific date with status.
   *
   * @param subject    the subject of the events
   * @param startDate  the start date of the first event
   * @param startTime  the start time for all events
   * @param endTime    the end time for all events
   * @param repeatDays the days of the week to repeat on
   * @param endDate    the end date (inclusive)
   * @param status     the status
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, Date endDate, Status status);

  /**
   * Creates an event series that repeats until a specific date (without optional fields).
   *
   * @param subject    the subject of the events
   * @param startDate  the start date of the first event
   * @param startTime  the start time for all events
   * @param endTime    the end time for all events
   * @param repeatDays the days of the week to repeat on
   * @param endDate    the end date (inclusive)
   * @throws IllegalArgumentException if any event violates uniqueness constraints
   */
  void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                         Set<Day> repeatDays, Date endDate);

  /**
   * Edits a single event identified by its properties.
   *
   * @param subject   the subject of the event to edit
   * @param startDate the start date of the event to edit
   * @param startTime the start time of the event to edit
   * @param endDate   the end date of the event to edit
   * @param endTime   the end time of the event to edit
   * @param property  the property to edit
   * @param newValue  the new value for the property
   * @throws IllegalArgumentException if the event cannot be uniquely identified or edit is invalid
   */
  void editEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
                 String property, String newValue);

  /**
   * Edits all events in a series starting from a specific event.
   *
   * @param subject   the subject of the event
   * @param startDate the start date of the event
   * @param startTime the start time of the event
   * @param property  the property to edit
   * @param newValue  the new value for the property
   * @throws IllegalArgumentException if the event cannot be uniquely identified or edit is invalid
   */
  void editEventsFromDate(String subject, Date startDate, Time startTime,
                          String property, String newValue);

  /**
   * Edits all events in a series.
   *
   * @param subject   the subject of the event
   * @param startDate the start date of an event in the series
   * @param startTime the start time of an event in the series
   * @param property  the property to edit
   * @param newValue  the new value for the property
   * @throws IllegalArgumentException if the event cannot be uniquely identified or edit is invalid
   */
  void editSeries(String subject, Date startDate, Time startTime,
                  String property, String newValue);

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to query
   * @return a list of events on that date
   */
  List<IntEvent> getEventsOnDate(Date date);

  /**
   * Gets all events within a date/time range.
   *
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   * @return a list of events that partly or completely lie in the given interval
   */
  List<IntEvent> getEventsInRange(Date startDate, Time startTime, Date endDate, Time endTime);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param date the date to check
   * @param time the time to check
   * @return true if there are events scheduled at that time, false otherwise
   */
  boolean isBusy(Date date, Time time);

  /**
   * Exports the calendar to a file. The format is determined by the file extension.
   * Supported formats: CSV (.csv), iCal (.ical, .ics)
   *
   * @param fileName the name of the file (with extension)
   * @return the absolute path of the generated file
   * @throws IllegalArgumentException if the file format is not supported
   * @throws IllegalStateException    if the export fails
   */
  String export(String fileName);

  /**
   * Gets the name of the calendar.
   *
   * @return the calendar name
   */
  String getName();

  /**
   * Gets the timezone of the calendar.
   *
   * @return the calendar timezone
   */
  ZoneId getTimezone();

  /**
   * Creates a new calendar with the specified name.
   * The calendar is immutable, so this returns a new instance.
   *
   * @param newName the new name for the calendar
   * @return a new calendar with the updated name
   * @throws IllegalArgumentException if newName is null or empty
   */
  IntCalendar withName(String newName);

  /**
   * Creates a new calendar with the specified timezone.
   * The calendar is immutable, so this returns a new instance.
   *
   * @param newTimezone the new timezone for the calendar
   * @return a new calendar with the updated timezone
   * @throws IllegalArgumentException if newTimezone is null
   */
  IntCalendar withTimezone(ZoneId newTimezone);

  /**
   * Copies a single event from this calendar to a target calendar.
   * The event is identified by subject, start date, and start time.
   * Times are converted from this calendar's timezone to the target calendar's timezone.
   *
   * @param subject        the subject of the event to copy
   * @param startDate      the start date of the event to copy
   * @param startTime      the start time of the event to copy
   * @param targetCalendar the calendar to copy the event to
   * @param newStartDate   the new start date in the target calendar
   * @param newStartTime   the new start time in the target calendar
   * @throws IllegalArgumentException if the event is not found
   * @throws IllegalArgumentException if the copied event violates uniqueness in target calendar
   */
  void copyEventTo(String subject, Date startDate, Time startTime,
                   IntCalendar targetCalendar, Date newStartDate, Time newStartTime);

  /**
   * Copies all events on a specific date from this calendar to a target calendar.
   * Times are converted from this calendar's timezone to the target calendar's timezone.
   *
   * @param date           the date to copy events from
   * @param targetCalendar the calendar to copy events to
   * @param newDate        the new date in the target calendar
   * @throws IllegalArgumentException if no events are found on the specified date
   * @throws IllegalArgumentException if any copied event violates uniqueness in target calendar
   */
  void copyEventsOnDateTo(Date date, IntCalendar targetCalendar, Date newDate);

  /**
   * Copies all events in a date range from this calendar to a target calendar.
   * Times are converted from this calendar's timezone to the target calendar's timezone.
   *
   * @param startDate      the start date of the range (inclusive)
   * @param endDate        the end date of the range (inclusive)
   * @param targetCalendar the calendar to copy events to
   * @param newStartDate   the new start date in the target calendar
   * @throws IllegalArgumentException if no events are found in the specified date range
   * @throws IllegalArgumentException if any copied event violates uniqueness in target calendar
   */
  void copyEventsInRangeTo(Date startDate, Date endDate,
                           IntCalendar targetCalendar, Date newStartDate);

  /**
   * Adds an event directly to this calendar.
   * This is a helper method used for copying events between calendars.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if the event violates uniqueness constraints
   */
  void addEvent(IntEvent event);
}
