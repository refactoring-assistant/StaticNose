package calendar.model;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/** Represents the calendar application's core model. */
public interface CalendarModel {

  /**
   * Creates a new calendar with the supplied name and timezone.
   *
   * @param name unique calendar name
   * @param zoneId timezone that all events in the calendar should use
   */
  void createCalendar(String name, ZoneId zoneId);

  /**
   * Renames an existing calendar.
   *
   * @param currentName existing calendar name
   * @param newName desired new name
   */
  void renameCalendar(String currentName, String newName);

  /**
   * Updates the timezone of an existing calendar and shifts all events accordingly.
   *
   * @param calendarName calendar to update
   * @param zoneId new timezone value
   */
  void changeCalendarTimezone(String calendarName, ZoneId zoneId);

  /**
   * Marks the named calendar as the active one for subsequent commands.
   *
   * @param name calendar to activate
   */
  void useCalendar(String name);

  /**
   * Indicates whether a calendar has been selected via {@link #useCalendar(String)}.
   *
   * @return {@code true} if a calendar is active
   */
  boolean hasActiveCalendar();

  /**
   * Returns the name of the currently active calendar.
   *
   * @return active calendar name
   */
  String getActiveCalendarName();

  /**
   * Returns the timezone of the active calendar.
   *
   * @return active calendar timezone
   */
  ZoneId getActiveCalendarZone();

  /**
   * Lists all calendar names in insertion order.
   *
   * @return ordered list of calendar names
   */
  List<String> listCalendars();

  /**
   * Creates a single event in the active calendar.
   *
   * @param subject subject of the event
   * @param start start date/time
   * @param end end date/time
   * @param allDay whether it originated from the all-day command
   * @return the created event
   */
  CalendarEvent createEvent(String subject, LocalDateTime start, LocalDateTime end, boolean allDay);

  /**
   * Creates a recurring series with a fixed number of occurrences.
   *
   * @param subject event subject
   * @param start start of first instance
   * @param end end of first instance
   * @param allDay whether the series came from the all-day command
   * @param weekdays weekdays to schedule on
   * @param occurrences number of events to create (including the first)
   * @return created events in chronological order
   */
  List<CalendarEvent> createRecurringEventsByCount(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      boolean allDay,
      Set<DayOfWeek> weekdays,
      int occurrences);

  /**
   * Creates a recurring series that repeats until the supplied inclusive date.
   *
   * @param subject event subject
   * @param start start of first instance
   * @param end end of first instance
   * @param allDay whether it came from the all-day command
   * @param weekdays weekdays to include
   * @param until inclusive final date for the series
   * @return created events
   */
  List<CalendarEvent> createRecurringEventsUntil(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      boolean allDay,
      Set<DayOfWeek> weekdays,
      LocalDate until);

  /**
   * Modifies a single event identified by subject/start/end.
   *
   * @param subject event subject
   * @param start original start
   * @param end original end
   * @param property property to edit
   * @param newValue replacement value for the property
   * @return updated event
   */
  CalendarEvent editSingleEvent(
      String subject,
      LocalDateTime start,
      LocalDateTime end,
      EventProperty property,
      Object newValue);

  /**
   * Applies an edit to every instance in a series starting from the supplied event.
   *
   * @param subject event subject
   * @param start start of the reference instance
   * @param property property to edit
   * @param newValue replacement value
   * @return impacted events in chronological order
   */
  List<CalendarEvent> editEventsFrom(
      String subject, LocalDateTime start, EventProperty property, Object newValue);

  /**
   * Edits every event in the series containing the supplied reference event.
   *
   * @param subject event subject
   * @param start start of reference event
   * @param property property to edit
   * @param newValue replacement value
   * @return updated events
   */
  List<CalendarEvent> editEntireSeries(
      String subject, LocalDateTime start, EventProperty property, Object newValue);

  /**
   * Retrieves the events scheduled on the supplied date.
   *
   * @param date date to inspect
   * @return events sorted by start time
   */
  List<CalendarEvent> eventsOn(LocalDate date);

  /**
   * Retrieves events that overlap the supplied interval (inclusive bounds).
   *
   * @param start interval start
   * @param end interval end
   * @return matching events
   */
  List<CalendarEvent> eventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Indicates whether an event overlaps the supplied moment.
   *
   * @param moment time to test
   * @return {@code true} if an event overlaps the moment
   */
  boolean isBusy(LocalDateTime moment);

  /**
   * Copies a single event into the target calendar at the specified start.
   *
   * @param subject subject of the event to copy
   * @param start original start time
   * @param targetCalendarName destination calendar name
   * @param targetStart new start time in the destination calendar
   * @return the new event created in the target calendar
   */
  CalendarEvent copyEvent(
      String subject, LocalDateTime start, String targetCalendarName, LocalDateTime targetStart);

  /**
   * Copies every event that occurs on a specific date to another calendar.
   *
   * @param sourceDate date to extract from the current calendar
   * @param targetCalendarName destination calendar name
   * @param targetDate date that the copied events should align to
   * @return created events
   */
  List<CalendarEvent> copyEventsOn(
      LocalDate sourceDate, String targetCalendarName, LocalDate targetDate);

  /**
   * Copies all events that overlap the supplied date interval to another calendar.
   *
   * @param startDate inclusive start of interval
   * @param endDate inclusive end of interval
   * @param targetCalendarName destination calendar name
   * @param targetStartDate date in the destination calendar that should align with {@code
   *     startDate}
   * @return created events
   */
  List<CalendarEvent> copyEventsBetween(
      LocalDate startDate, LocalDate endDate, String targetCalendarName, LocalDate targetStartDate);

  /**
   * Exports the active calendar to CSV or iCal depending on the file extension.
   *
   * @param outputFile desired output path
   * @return the normalized absolute output path
   * @throws IOException if writing fails
   */
  Path exportCalendar(Path outputFile) throws IOException;
}
