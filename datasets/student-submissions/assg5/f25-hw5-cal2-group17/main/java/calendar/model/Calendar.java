package calendar.model;

import calendar.util.Exporter;
import calendar.util.TimezoneConverter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * Interface for a calendar that manages events.
 * Each calendar has a unique name and timezone.
 */
public interface Calendar {

  // ========== CALENDAR PROPERTIES ==========

  /**
   * Gets the name of this calendar.
   *
   * @return Calendar name
   */
  String getName();

  /**
   * Sets the name of this calendar.
   *
   * @param name New calendar name (must not be null or empty)
   * @throws IllegalArgumentException if name is null or empty
   */
  void setName(String name);

  /**
   * Gets the timezone of this calendar.
   *
   * @return ZoneId representing the calendar's timezone
   */
  ZoneId getTimezone();

  /**
   * Sets the timezone of this calendar.
   * Note: This does NOT convert existing event times.
   *
   * @param timezone New timezone (must not be null)
   * @throws IllegalArgumentException if timezone is null
   */
  void setTimezone(ZoneId timezone);

  // ========== TIMEZONE UTILITIES (NEW) ==========

  /**
   * Converts a LocalDateTime from this calendar's timezone to another timezone.
   *
   * @param dateTime   DateTime in this calendar's timezone
   * @param targetZone Target timezone
   * @return DateTime in target timezone
   */
  default LocalDateTime convertToTimezone(LocalDateTime dateTime, ZoneId targetZone) {
    return TimezoneConverter.convertBetweenTimezones(dateTime, this.getTimezone(), targetZone);
  }

  /**
   * Converts a LocalDateTime from another timezone to this calendar's timezone.
   *
   * @param dateTime   DateTime in source timezone
   * @param sourceZone Source timezone
   * @return DateTime in this calendar's timezone
   */
  default LocalDateTime convertFromTimezone(LocalDateTime dateTime, ZoneId sourceZone) {
    return TimezoneConverter.convertBetweenTimezones(dateTime, sourceZone, this.getTimezone());
  }

  // ========== EVENT OPERATIONS ==========

  /**
   * Adds a single event to the calendar.
   *
   * @param event Event to add
   * @throws IllegalArgumentException if event already exists (duplicate)
   */
  void addEvent(Event event);

  /**
   * Creates and adds an event series that repeats on specific weekdays.
   *
   * @param subject       Event subject
   * @param startDateTime Start date and time of first occurrence
   * @param endDateTime   End date and time (null for all-day)
   * @param weekdays      Set of days to repeat on
   * @param occurrences   Number of occurrences (if greater than 0), or -1 to use untilDate
   * @param untilDate     Repeat until this date (inclusive), or null to use occurrences
   * @throws IllegalArgumentException if series would create duplicates or spans multiple days
   */
  void addEventSeries(String subject, LocalDateTime startDateTime,
                      LocalDateTime endDateTime, Set<DayOfWeek> weekdays,
                      int occurrences, LocalDate untilDate);

  /**
   * Finds events matching the given criteria.
   *
   * @param subject       Event subject to match
   * @param startDateTime Start date/time to match
   * @return List of matching events (empty if none found)
   */
  List<Event> findEvents(String subject, LocalDateTime startDateTime);

  /**
   * Gets all events on a specific date.
   *
   * @param date Date to query
   * @return List of events on that date, sorted by start time
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Gets all events within a date/time range.
   *
   * @param startRange Start of range (inclusive)
   * @param endRange   End of range (inclusive)
   * @return List of events in range, sorted by start time
   */
  List<Event> getEventsInRange(LocalDateTime startRange, LocalDateTime endRange);

  /**
   * Checks if user is busy at a specific date/time.
   *
   * @param dateTime Date and time to check
   * @return true if any event is scheduled at that time
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return List of all events
   */
  List<Event> getAllEvents();

  /**
   * Removes an event from the calendar.
   *
   * @param event Event to remove
   */
  void removeEvent(Event event);

  /**
   * Gets all events in the same series as the given event.
   *
   * @param event Event to find series for
   * @return List of events in the same series (single item if not part of series)
   */
  List<Event> getEventsInSeries(Event event);

  /**
   * Gets events in the same series starting from a specific event (inclusive).
   *
   * @param event Starting event
   * @return List of events in series from this point onwards
   */
  List<Event> getEventsInSeriesFrom(Event event);

  /**
   * Accepts an exporter visitor to export this calendar.
   * This is the "accept" method in the Visitor pattern, enabling double dispatch.
   *
   * @param exporter Exporter to use
   * @param filename Output filename
   * @return Absolute path of exported file
   * @throws IOException if export fails
   */
  default String accept(Exporter exporter, String filename) throws IOException {
    return exporter.export(this, filename);
  }
}