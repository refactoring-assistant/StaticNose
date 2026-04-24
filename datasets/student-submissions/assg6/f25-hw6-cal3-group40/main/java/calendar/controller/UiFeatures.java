package calendar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for the features supported by the calendar application.
 * These methods are called by the view to trigger actions in the controller.
 */
public interface UiFeatures {

  /**
   * Creates a new calendar.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone of the calendar
   */
  void createCalendar(String name, String timezone);

  /**
   * Selects a calendar to be the active one.
   *
   * @param name the name of the calendar to select
   */
  void selectCalendar(String name);

  /**
   * Lists all available calendars.
   */
  void listCalendars();

  /**
   * Gets the name of the current calendar.
   */
  void getCurrentCalendarName();

  /**
   * Creates a new event.
   *
   * @param subject     the subject of the event
   * @param fromStr     the start date/time string
   * @param toStr       the end date/time string
   * @param onStr       the date string (for all-day or specific date events if
   *                    applicable)
   * @param description the description of the event
   * @param location    the location of the event
   * @param isPrivate   whether the event is private
   * @param repeats     the repetition rule (e.g., "daily", "weekly")
   * @param occurrences the number of occurrences
   * @param untilStr    the until date string
   */
  void createEvent(String subject, String fromStr, String toStr,
                   String onStr, String description, String location, boolean isPrivate,
                   String repeats, Integer occurrences, String untilStr);

  /**
   * Edits an existing event.
   *
   * @param subject           the subject of the event to edit
   * @param fromStr           the start date/time of the event to identify it
   * @param toStr             the end date/time of the event to identify it
   * @param property          the property to change
   * @param newValueStr       the new value for the property
   * @param singleEventUpdate whether to update only this single event (for
   *                          recurring events)
   * @param updateAll         whether to update all future occurrences (for
   *                          recurring events)
   */
  void editEvent(String subject, String fromStr, String toStr,
                 String property, String newValueStr,
                 boolean singleEventUpdate, boolean updateAll);

  /**
   * Requests events for a specific date.
   *
   * @param date the date to get events for
   */
  void getEventsOn(LocalDate date);

  /**
   * Requests events between two dates/times.
   *
   * @param start the start date/time
   * @param end   the end date/time
   */
  void getEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Edits the current calendar's name and timezone.
   *
   * @param newName     the new name
   * @param newTimezone the new timezone
   */
  void editCalendar(String newName, String newTimezone);

  /**
   * Updates the start and end time of an event.
   *
   * @param subject           the subject of the event
   * @param currentStartStr   the current start time string
   * @param newStartStr       the new start time string
   * @param newEndStr         the new end time string
   * @param singleEventUpdate whether to update only this single event
   * @param updateAll         whether to update all events in the series
   */
  void updateEventTime(String subject, String currentStartStr, String newStartStr, String newEndStr,
                       boolean singleEventUpdate, boolean updateAll);
}
