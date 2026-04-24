package calendar.model;

import calendar.controller.CopySpec;
import calendar.controller.CreateSpec;
import calendar.controller.EditSpec;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * The model interface.
 */
public interface Model {

  /**
   * The below method creates a new calendar.
   *
   * @param name name of calendar.
   * @param timezone timezone of calendar.
   */
  void createCalendar(String name, String timezone);


  /**
   * The below method edits the properties name or timezone of a given calendar.
   *
   * @param name the name of calendar.
   * @param property the property of the calendar to be edited.
   * @param newPropValue the new property value
   */
  void editCalendar(String name, String property, String newPropValue);

  /**
   * Creates a single event or an event series based on the user input.
   *
   * @param createDto The Create Data Transfer object.
   */
  void create(String calName, CreateSpec createDto);

  /**
   * Edits a single event, a set of events, or a series of events based on the command.
   *
   * @param editDto The edit data transfer object.
   */
  void edit(String calName, EditSpec editDto);

  /**
   * Returns a list of all calendars which are stored.
   *
   * @return The list of all calendars.
   */
  String allCals();

  /**
   * The function below is used to return the timezone of a specific calendar.
   *
   * @param calName The name of the calendar.
   * @return The timezone in the form of a string.
   * @throws IllegalArgumentException if calendar does not exist
   */
  String calTimezone(String calName);


  /**
   * The method below is used to check if a particular calendar exists or not.
   *
   * @param calName the name of the calendar to be checked
   * @return True if it exists else false.
   */
  boolean exists(String calName);

  /**
   * The method below is used to copy an event or events from one calendar to a different timeline
   * in the same calendar or even to a different calendar.
   *
   * @param calName   Name of context calendar.
   * @param copyDto  the copy spec data transfer object.
   */
  void copy(String calName, CopySpec copyDto);

  /**
   * Queries a list of events from the calendar.
   *
   * @param calName the name of calendar in use
   * @param startDate the start date to query from
   * @param startTime the start time to query from
   * @param endDate the end date to query till, inclusive
   * @param endTime the end time to query till
   * @return the list of events
   */
  List<InterfaceEvent> queryEvents(String calName, LocalDate startDate, LocalTime startTime,
                                   LocalDate endDate, LocalTime endTime, boolean export);

  /**
   * Checks whether the user is busy at the specified date and time.
   *
   * @param dateTimeStr String calName, String in the format "YYYY-MM-DDThh:mm"
   * @return true if an event exists at the given date/time, false otherwise.
   */
  boolean isBusy(String calName, String dateTimeStr);

}
