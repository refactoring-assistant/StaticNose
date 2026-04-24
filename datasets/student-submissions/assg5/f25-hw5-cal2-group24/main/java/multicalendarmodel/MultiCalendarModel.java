package multicalendarmodel;

import calendarmodel.exceptions.DuplicateEventException;
import calendarmodel.exceptions.EventNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Manages a collection of named, time-zone-aware calendars.
 *
 * <p>This interface defines the top-level API for an application that
 * supports multiple calendars, each with its own name, time zone, and
 * set of events.</p>
 */
public interface MultiCalendarModel {

  /**
   * Creates a new, empty calendar.
   *
   * @param name The unique name for the new calendar.
   * @param zone The initial time zone for the new calendar.
   * @throws CalendarNameException if the name is blank or already exists.
   */
  void createCalendar(String name, ZoneId zone) throws CalendarNameException;

  /**
   * Renames an existing calendar.
   *
   * @param oldName The current (and unique) name of the calendar.
   * @param newName The new (and unique) name for the calendar.
   * @throws CalendarNameException if the old name is not found, or the new
   *                               name is blank or already exists.
   */
  void renameCalendar(String oldName, String newName) throws CalendarNameException;

  /**
   * Changes the time zone of an existing calendar.
   *
   * <p>This will change the {@link ZoneId} used to interpret and display all
   * events within that calendar.</p>
   *
   * @param calendarName The name of the calendar to modify.
   * @param newZone      The new {@link ZoneId} to set.
   * @throws CalendarNameException if the calendar name is not found.
   */
  void changeCalendarZone(String calendarName, ZoneId newZone) throws CalendarNameException;

  /**
   * Retrieves a read-only list of all calendar names.
   *
   * @return A list of all current calendar names.
   */
  List<String> getAllCalendarNames();

  /**
   * Gets a specific calendar by its name.
   *
   * <p>The returned {@link ZonedCalendarModel} is the live model. Any changes
   * made to it (e.g., adding events) are persistent.</p>
   *
   * @param calendarName The name of the calendar to retrieve.
   * @return The {@link ZonedCalendarModel} associated with the name.
   * @throws CalendarNameException if the calendar name is not found.
   */
  ZonedCalendarModel getCalendar(String calendarName) throws CalendarNameException;

  /**
   * Copies all events from a source calendar within a specified time interval
   * to a target calendar, starting at a new date and time.
   *
   * <p>All time parameters ({@code sourceIntervalStart},
   * {@code sourceIntervalEnd}, and {@code newIntervalStart}) are
   * interpreted as "wall times" in their respective calendar's time zones.</p>
   *
   * <p>For example, copying 9:00 AM - 5:00 PM from a "New York" calendar
   * to a "London" calendar will preserve the 9-to-5 "wall time"
   * relative to the new start date, not the universal instant.</p>
   *
   * <p>The relative time difference between all copied events is preserved.</p>
   *
   * @param sourceCalendarName  The name of the calendar to copy from.
   * @param sourceIntervalStart The start time of the interval in the source zone.
   * @param sourceIntervalEnd   The end time of the interval in the source zone.
   * @param targetCalendarName  The name of the calendar to copy to.
   * @param newIntervalStart    The new start time for the *first* event in the
   *                            copied set, interpreted in the target zone.
   * @throws CalendarNameException   if either calendar name is not found.
   * @throws DuplicateEventException if a copied event conflicts in the target.
   * @throws EventNotFoundException  (should not happen) if an event is inconsistent.
   */
  void copyEventInterval(String sourceCalendarName,
                         LocalDateTime sourceIntervalStart, LocalDateTime sourceIntervalEnd,
                         String targetCalendarName, LocalDateTime newIntervalStart)
      throws CalendarNameException, DuplicateEventException, EventNotFoundException;

  /**
   * Copies a single event to a new start time in a target calendar.
   *
   * <p>The event to copy is identified by its key parameters in the source
   * calendar's time zone.</p>
   *
   * @param sourceCalendarName The name of the calendar to copy from.
   * @param findSubject        The subject of the event to find.
   * @param findStartTime      The start time of the event in the source zone.
   * @param findEndTime        The end time of the event in the source zone.
   * @param targetCalendarName The name of the calendar to copy to.
   * @param newStartTime       The new start time for the copied event,
   *                           interpreted in the target zone.
   * @throws CalendarNameException   if either calendar name is not found.
   * @throws EventNotFoundException  if the source event is not found.
   * @throws DuplicateEventException if the copied event conflicts in the target.
   */
  void copyEvent(String sourceCalendarName, String findSubject,
                 LocalDateTime findStartTime, LocalDateTime findEndTime,
                 String targetCalendarName, LocalDateTime newStartTime)
      throws CalendarNameException, EventNotFoundException, DuplicateEventException;
}