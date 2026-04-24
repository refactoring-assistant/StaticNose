package calendar.controller.features;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Features exposed by the GUI controller for use by the Swing view.
 * Implementations of this interface act as the bridge between the view and
 * the underlying calendar system model.
 */
public interface GuiFeatures {

  /**
   * Creates a new calendar with the given name and time zone.
   *
   * @param name the calendar name
   * @param zone the time zone to associate with the calendar
   */
  void onCreateCalendar(String name, ZoneId zone);

  /**
   * Switches the active calendar to the given name.
   *
   * @param name the calendar to use
   */
  void onUseCalendar(String name);

  /**
   * Renames an existing calendar.
   *
   * @param oldName the current calendar name
   * @param newName the desired new name
   */
  void onRenameCalendar(String oldName, String newName);

  /**
   * Changes the time zone of the specified calendar.
   *
   * @param calName the calendar name
   * @param zone    the new time zone
   */
  void onChangeCalendarTimezone(String calName, ZoneId zone);

  /**
   * Requests to display all events on the given date in the active calendar.
   *
   * @param date the date to view
   */
  void onViewDay(LocalDate date);

  /**
   * Creates a single, non-recurring event in the active calendar.
   *
   * @param subject     the event subject
   * @param start       the event start instant
   * @param end         the event end instant
   * @param description optional description (may be empty but not null)
   * @param location    optional location (may be empty but not null)
   * @param isPublic    true if public, false if private
   * @param zone        time zone for interpreting start and end when rendering
   */
  void onCreateSingleEvent(String subject, Instant start, Instant end,
                           String description, String location,
                           boolean isPublic, ZoneId zone);

  /**
   * Creates a recurring event in the active calendar using a count-based rule.
   *
   * @param subject     the event subject
   * @param start       start of the first occurrence
   * @param end         end of the first occurrence
   * @param days        days of week on which the event may occur
   * @param count       number of occurrences to create
   * @param description optional description (may be empty but not null)
   * @param location    optional location (may be empty but not null)
   * @param isPublic    true if public, false if private
   * @param zone        time zone for the occurrences
   */
  void onCreateRecurringByCount(String subject, Instant start, Instant end,
                                List<DayOfWeek> days, int count,
                                String description, String location,
                                boolean isPublic, ZoneId zone);

  /**
   * Creates a recurring event in the active calendar using an until-date rule.
   *
   * @param subject     the event subject
   * @param start       start of the first occurrence
   * @param end         end of the first occurrence
   * @param days        days of week on which the event may occur
   * @param until       ISO-8601 date string (yyyy-MM-dd) for the last allowed day
   * @param description optional description (may be empty but not null)
   * @param location    optional location (may be empty but not null)
   * @param isPublic    true if public, false if private
   * @param zone        time zone for the occurrences
   */
  void onCreateRecurringUntil(String subject, Instant start, Instant end,
                              List<DayOfWeek> days, String until,
                              String description, String location,
                              boolean isPublic, ZoneId zone);

  /**
   * Edits a single event occurrence in the active calendar.
   *
   * @param subject       subject of the event to edit
   * @param originalStart original start instant identifying the event
   * @param property      property name to edit (subject, description, location,
   *                      status, start, or end)
   * @param newValue      new value for the property
   */
  void onEditEventSingle(String subject, Instant originalStart,
                         String property, String newValue);

  /**
   * Edits all events in a series from the given occurrence forward.
   *
   * @param subject    subject of the series
   * @param pivotStart start instant that identifies the pivot occurrence
   * @param property   property name to edit (subject, description, location,
   *                   status, start, or end)
   * @param newValue   new value for the property
   */
  void onEditEventsFrom(String subject, Instant pivotStart,
                        String property, String newValue);

  /**
   * Edits all events in a series, regardless of date.
   *
   * @param subject  subject of the series
   * @param anyStart start instant of any occurrence in the series
   * @param property property name to edit (subject, description, location,
   *                 status, start, or end)
   * @param newValue new value for the property
   */
  void onEditSeriesAll(String subject, Instant anyStart,
                       String property, String newValue);

  /**
   * Notifies the controller that a calendar month is being shown so the
   * controller can compute which days in that month contain events and
   * inform the view accordingly.
   *
   * @param firstOfMonth the first day of the month that is displayed
   */
  void onMonthShown(LocalDate firstOfMonth);
}
