package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Interface that represents the operations that can be performed on the calendar.
 */
public interface InterfaceCalendarModel {
  String TIME_ZONE_ID = "America/New_York";

  /**
   * Using enum to break down the edit process in 3 parts and handling each module seprately
   * to ensure Single Responsibility.
   */
  enum EditScope {
    /**
     * Represents the editing of only this single instance.
     * If this is a part of series, then it will break from it if time is changed.
     */
    SINGLE_INSTANCE,
    /**
     * Represents editing this and the following events with same properties like subject.
     * If this is a part of series, the series will be split from this event.
     */
    ALL_FOLLOWING,
    /**
     * Represents editing all events in the series i.e. the entire series
     */
    ENTIRE_SERIES
  }

  /**
   * Method to add a single and non-recurring event to the calendar.
   *
   * @param event CalendarObject that needs to be added to the calendar
   * @throws CalendarException if the event conflicts with an existing event
   */
  void addEvent(CalendarEvent event) throws CalendarException;

  /**
   * Method to add an event series based on a master event template.
   *
   * @param master SeriesMaster object that contains the template and the rule
   * @throws CalendarException if there are any event conflicts
   */
  void addEventSeries(SeriesMaster master) throws CalendarException;

  /**
   * Method to modify an event in the calendar and delegate the necessary action to helpers
   * depending on the scope of edit.
   *
   * @param originalEvent CalendarEvent object to be edited
   * @param updatedTemplate CalendarEvent object with the edited properties
   * @param scope ENUM object specifying scope of edit
   * @throws CalendarException if the event to be edited does not exist or the desired
   *                           edition causes a conflict of events
   */
  void editEvent(CalendarEvent originalEvent, CalendarEvent updatedTemplate, EditScope scope)
      throws CalendarException;

  /**
   * Method to get a specific event with its unique identifying properties as arguments.
   *
   * @param subject String object containing the subject of the event
   * @param start   ZonedDateTime object representing the start of an event
   * @param end     ZonedDateTime object representing the end of an event
   * @return the requested CalendarEvent object
   * @throws CalendarException if no unique event was found with given properties
   */
  CalendarEvent getUniqueEvent(String subject, ZonedDateTime start, ZonedDateTime end)
      throws CalendarException;

  /**
   * Method to query and get events that are present in the given range of datetime.
   *
   * @param start ZonedDateTime object representing the start of the range
   * @param end   ZonedDateTime object representing the end of the range
   * @return List of CalendarEvent objects present in that range
   */
  List<CalendarEvent> getEventsInRange(ZonedDateTime start, ZonedDateTime end);

  /**
   * Method to get events that are present on a specific date.
   *
   * @param date LocalDate object that represents the specified date
   * @return List of CalendarEvent objects which are present on that date
   */
  List<CalendarEvent> getEventsOnDay(LocalDate date);

  /**
   * Method to check if the user is busy at a specific point in time.
   *
   * @param checkTime ZonedDateTime object representing date and time to be checked
   * @return true if the given time is at the same time of an existing event, else false
   */
  boolean isBusy(ZonedDateTime checkTime);

  /**
   * Method to get a list of all events currently in the calendar.
   *
   * @return A List of all CalendarEvent objects.
   */
  List<CalendarEvent> getAllEvents();

  /**
   * Method to get the event using subject and start date time.
   *
   * @param subject String object representing subject of event
   * @param start   ZonedDateTime object representing start of the event
   * @return the CalendarEvent found
   * @throws CalendarException if no match or multiple matches for the event are found.
   */
  CalendarEvent getEventBySubjectAndStart(String subject, ZonedDateTime start)
      throws CalendarException;
}