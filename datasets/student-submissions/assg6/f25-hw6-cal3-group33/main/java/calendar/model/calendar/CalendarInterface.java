package calendar.model.calendar;

import calendar.exceptions.DuplicateEventException;
import calendar.exceptions.EditConflictException;
import calendar.exceptions.EventNotFoundException;
import calendar.exceptions.InvalidDateTimeException;
import calendar.exceptions.InvalidPropertyException;
import calendar.exceptions.MultipleEventsFoundException;
import calendar.model.event.EventBuilder;
import calendar.model.event.EventInterface;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

/**
 * Interface for a calendar that manages events and event series.
 * Provides methods for creating, editing, querying, and exporting events.
 * All datetime strings use format YYYY-MM-DDThh:mm (e.g., 2025-05-05T14:30).
 * All date strings use format YYYY-MM-DD (e.g., 2025-05-05).
 * Timezone: All events stored in America/New_York (Eastern Time).
 */
public interface CalendarInterface extends ReadOnlyCalendar {

  /**
   * Sets the name of this calendar.
   *
   * @param name the new calendar name
   */
  void setName(String name);

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone the new timezone
   */
  void setTimezone(ZoneId timezone) throws InvalidDateTimeException;

  /**
   * Creates a new event builder for fluent event creation.
   * Auto-detects timed vs all-day events based on startDateTime format.
   *
   * @param subject       the event subject, must not be null or empty
   * @param startDateTime start time (YYYY-MM-DDThh:mm) or date (YYYY-MM-DD)
   * @return EventBuilder for chaining additional properties
   */
  EventBuilder newEvent(String subject, String startDateTime);

  /**
   * Stores built events in the calendar.
   * Intended to be called by EventBuilder.create().
   * Note: Accepts EventInterface for flexibility, but internally casts to Event
   * since this Calendar implementation requires access to package-private methods
   * like setSeriesId(). This is safe because EventBuilder only creates Event objects.
   *
   * @param events list of events to store
   * @throws DuplicateEventException if any event already exists (same subject, start, end)
   */
  void storeEvents(List<? extends EventInterface> events) throws DuplicateEventException;

  /**
   * Checks if the user is busy at a specific date and time.
   * Returns true if any event is scheduled at that exact time.
   *
   * @param dateTimes the datetime to check in format YYYY-MM-DDThh:mm
   * @return true if busy (event scheduled at that time), false if available
   * @throws InvalidDateTimeException if datetime format is invalid
   */
  boolean busyStatus(String dateTimes) throws InvalidDateTimeException;

  /**
   * Edits a single event instance.
   * If event is part of series and temporal property (start/end) changed,
   * it becomes standalone (removed from series).
   *
   * @param property      the property to edit: subject, start, end, description, location, status
   * @param subject       the current subject of the event to find
   * @param startDateTime the current start datetime in format YYYY-MM-DDThh:mm
   * @param endDateTime   the current end datetime in format YYYY-MM-DDThh:mm
   * @param newValue      the new value for the property
   * @throws EventNotFoundException       if no event matches the criteria
   * @throws MultipleEventsFoundException if multiple events match (ambiguous)
   * @throws EditConflictException        if edit would create duplicate or violate constraints
   * @throws InvalidDateTimeException     if datetime format is invalid
   */
  void editEvent(String property, String subject, String startDateTime,
                 String endDateTime, String newValue)
      throws EventNotFoundException, MultipleEventsFoundException, EditConflictException,
      InvalidDateTimeException, InvalidPropertyException;

  /**
   * Edits events in a series from a specific datetime forward.
   * For temporal properties (start/end), splits the series into two separate series.
   * For non-temporal properties, updates all events from datetime forward in same series.
   *
   * @param property      the property to edit: subject, start, end, description, location, status
   * @param subject       the current subject of the event to find
   * @param startDateTime the start datetime of occurrence to begin editing from (YYYY-MM-DDThh:mm)
   * @param newValue      the new value for the property
   * @throws EventNotFoundException       if no event matches the criteria
   * @throws MultipleEventsFoundException if multiple events match (ambiguous)
   * @throws EditConflictException        if edit would create duplicate or violate constraints
   * @throws InvalidDateTimeException     if datetime format is invalid
   */
  void editEventsFrom(String property, String subject, String startDateTime, String newValue)
      throws EventNotFoundException, MultipleEventsFoundException, EditConflictException,
      InvalidDateTimeException, InvalidPropertyException;

  /**
   * Edits all events in a series.
   * Updates all occurrences with the new value, maintaining series relationship.
   *
   * @param property      the property to edit: subject, start, end, description, location, status
   * @param subject       the current subject of any event in the series
   * @param startDateTime the start datetime of any event in the series (YYYY-MM-DDThh:mm)
   * @param newValue      the new value for the property
   * @throws EventNotFoundException       if no event matches the criteria
   * @throws MultipleEventsFoundException if multiple events match (ambiguous)
   * @throws EditConflictException        if edit would create duplicate or violate constraints
   * @throws InvalidDateTimeException     if datetime format is invalid
   */
  void editSeries(String property, String subject, String startDateTime, String newValue)
      throws EventNotFoundException, MultipleEventsFoundException, EditConflictException,
      InvalidDateTimeException, InvalidPropertyException;
}