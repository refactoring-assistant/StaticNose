package calendar.model.manager;

import calendar.exceptions.CalendarNotFoundException;
import calendar.exceptions.DuplicateCalendarException;
import calendar.exceptions.InvalidDateTimeException;
import calendar.exceptions.InvalidTimezoneException;
import calendar.exceptions.NoCalendarInUseException;
import calendar.model.calendar.Calendar;
import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.CalendarPropertiesAdapter;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages a catalog of calendars, tracking which calendar is currently in use.
 * Provides operations to create, edit, select, and retrieve calendars.
 *
 * <p>This class implements the Registry Pattern, maintaining a collection of named
 * calendars and tracking which calendar is currently active for operations.
 *
 * <p>Calendar names are case-sensitive and will be trimmed of leading/trailing whitespace.
 *
 * <p>CLASS INVARIANTS:
 * - Calendar names are unique (case-sensitive)
 * - currentCalendarName is null or references an existing calendar
 * - All calendars have non-null, non-empty names
 * - All calendars have valid timezones
 */
public class CalendarCatalog implements CalendarManager {

  private final Map<String, CalendarPropertiesAdapter> calendars;
  private String currentCalendarName;

  /**
   * Constructs an empty CalendarCatalog with no calendars.
   * No calendar is selected initially.
   */
  public CalendarCatalog() {
    this.calendars = new HashMap<>();
    this.currentCalendarName = null;
  }

  /**
   * Creates a new calendar with the given name and timezone.
   * The calendar name will be trimmed of leading/trailing whitespace.
   * Calendar names are case-sensitive.
   *
   * @param name        the unique name for the calendar (will be trimmed)
   * @param timezoneStr IANA timezone identifier (e.g., "America/New_York", "Europe/London")
   * @throws DuplicateCalendarException if a calendar with this name already exists
   * @throws InvalidTimezoneException   if the timezone string is not a valid IANA timezone
   * @throws IllegalArgumentException   if name is null or empty after trimming
   */
  @Override
  public void createCalendar(String name, String timezoneStr)
      throws DuplicateCalendarException, InvalidTimezoneException {

    String trimmedName = validateAndTrimName(name);

    if (calendars.containsKey(trimmedName)) {
      throw new DuplicateCalendarException(trimmedName);
    }

    ZoneId timezone = parseTimezone(timezoneStr);

    CalendarInterface calendar = new Calendar(trimmedName, timezone);
    CalendarPropertiesAdapter adapter = new CalendarPropertiesAdapter(calendar);
    calendars.put(trimmedName, adapter);
  }

  /**
   * Changes the name of an existing calendar.
   * If the renamed calendar is currently in use, the current calendar reference
   * is automatically updated to the new name.
   * The new calendar name will be trimmed of leading/trailing whitespace.
   * Calendar names are case-sensitive.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar (will be trimmed)
   * @throws CalendarNotFoundException  if the calendar with oldName does not exist
   * @throws DuplicateCalendarException if a calendar with newName already exists
   * @throws IllegalArgumentException   if newName is null or empty after trimming
   */
  @Override
  public void editCalendarName(String oldName, String newName)
      throws CalendarNotFoundException, DuplicateCalendarException {

    validateCalendarExists(oldName);
    String trimmedNewName = validateAndTrimName(newName);

    if (oldName.equals(trimmedNewName)) {
      return;
    }

    if (calendars.containsKey(trimmedNewName)) {
      throw new DuplicateCalendarException(trimmedNewName);
    }

    CalendarPropertiesAdapter adapter = calendars.remove(oldName);
    adapter.setName(trimmedNewName);
    calendars.put(trimmedNewName, adapter);

    if (oldName.equals(currentCalendarName)) {
      currentCalendarName = trimmedNewName;
    }
  }

  /**
   * Changes the timezone of an existing calendar.
   * All existing events in the calendar remain unchanged; only the calendar's
   * timezone context is updated for future operations.
   *
   * @param name           the name of the calendar to edit
   * @param newTimezoneStr IANA timezone identifier (e.g., "Europe/London", "Asia/Tokyo")
   * @throws CalendarNotFoundException if the calendar does not exist
   * @throws InvalidTimezoneException  if the timezone string is not a valid IANA timezone
   */
  @Override
  public void editCalendarTimezone(String name, String newTimezoneStr)
      throws CalendarNotFoundException, InvalidTimezoneException, InvalidDateTimeException {

    validateCalendarExists(name);
    ZoneId newTimezone = parseTimezone(newTimezoneStr);

    CalendarPropertiesAdapter adapter = calendars.get(name);
    adapter.setTimezone(newTimezone);
  }

  /**
   * Selects a calendar to be the current calendar for operations.
   * All subsequent event operations will be performed on this calendar
   * until a different calendar is selected.
   *
   * @param name the name of the calendar to use
   * @throws CalendarNotFoundException if the calendar does not exist
   */
  @Override
  public void useCalendar(String name) throws CalendarNotFoundException {
    validateCalendarExists(name);
    currentCalendarName = name;
  }

  /**
   * Returns the currently selected calendar.
   * This is the calendar that event operations will be performed on.
   *
   * @return the current calendar
   * @throws NoCalendarInUseException if no calendar is currently selected,
   *                                  with a message indicating whether calendars exist or not
   */
  @Override
  public CalendarInterface getCurrentCalendar() throws NoCalendarInUseException {

    if (currentCalendarName == null) {
      if (calendars.isEmpty()) {
        throw new NoCalendarInUseException(
            "No calendars exist. Create one with 'create calendar <name> <timezone>'"
        );
      } else {
        throw new NoCalendarInUseException(
            "No calendar selected. Use 'use calendar <name>' to select one"
        );
      }
    }

    CalendarPropertiesAdapter adapter = calendars.get(currentCalendarName);
    return adapter.getUnderlyingCalendar();
  }

  /**
   * Returns the name of the currently selected calendar.
   * This is a query method that does not throw exceptions.
   *
   * @return the current calendar name, or null if no calendar is selected
   */
  @Override
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  /**
   * Retrieves a specific calendar by name.
   * This method is used for operations that need to access a calendar
   * other than the current one (e.g., copy operations between calendars).
   *
   * @param name the name of the calendar to retrieve
   * @return the calendar with the given name
   * @throws CalendarNotFoundException if the calendar does not exist
   */
  @Override
  public CalendarInterface getCalendar(String name) throws CalendarNotFoundException {
    validateCalendarExists(name);
    CalendarPropertiesAdapter adapter = calendars.get(name);
    return adapter.getUnderlyingCalendar();
  }

  /**
   * Checks if a calendar with the given name exists.
   * This is a query method that does not throw exceptions.
   *
   * @param name the calendar name to check
   * @return true if the calendar exists, false otherwise
   */
  @Override
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }

  /**
   * Returns the names of all calendars in the catalog.
   * Returns a copy of the names to prevent external modification.
   *
   * @return a set of all calendar names (empty set if no calendars exist)
   */
  @Override
  public Set<String> getAllCalendarNames() {
    return new HashSet<>(calendars.keySet());
  }

  /**
   * Deletes a calendar from the catalog.
   *
   * <p><b>Note:</b> This operation is not currently supported as it is not
   * required by the assignment specifications. Calendar deletion may be
   * implemented in future versions.
   *
   * @param name the name of the calendar to delete
   * @throws CalendarNotFoundException if the calendar does not exist
   * @throws UnsupportedOperationException always thrown - deletion not supported
   */
  @Override
  public void deleteCalendar(String name) throws CalendarNotFoundException {
    throw new UnsupportedOperationException(
        "Calendar deletion is not supported in this version"
    );
  }

  // ==================== PRIVATE HELPER METHODS ====================

  /**
   * Validates that a calendar with the given name exists.
   *
   * @param name the calendar name to check
   * @throws CalendarNotFoundException if the calendar does not exist
   */
  private void validateCalendarExists(String name) throws CalendarNotFoundException {
    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException(name);
    }
  }

  /**
   * Validates and trims a calendar name.
   *
   * @param name the calendar name to validate
   * @return the trimmed name
   * @throws IllegalArgumentException if name is null or empty after trimming
   */
  private String validateAndTrimName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    return name.trim();
  }

  /**
   * Parses and validates a timezone string.
   *
   * @param timezoneStr IANA timezone identifier (e.g., "America/New_York")
   * @return the parsed ZoneId
   * @throws InvalidTimezoneException if the timezone string is not valid
   */
  private ZoneId parseTimezone(String timezoneStr) throws InvalidTimezoneException {
    try {
      return ZoneId.of(timezoneStr);
    } catch (Exception e) {
      throw new InvalidTimezoneException(timezoneStr);
    }
  }
}