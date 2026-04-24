package calendar.controller;

import calendar.view.dto.AvailabilityDto;
import calendar.view.dto.CopyEventDto;
import calendar.view.dto.CreateCalDto;
import calendar.view.dto.CreateEventDto;
import calendar.view.dto.EditCalDto;
import calendar.view.dto.EditEventDto;
import calendar.view.dto.ExportCalDto;
import calendar.view.dto.QueryEventDto;
import calendar.view.dto.SelectCalDto;
import calendar.view.dto.SelectDayDto;

/**
 * Defines the set of high-level actions (features) that the Graphical User Interface (GUI)
 * can request from the Controller.
 *
 * <p>This interface acts as the contract between the View and the Controller in the MVC
 * architecture. The View invokes these methods in response to user interactions (e.g., button
 * clicks), passing data via Data Transfer Objects (DTOs). The Controller implementing this
 * interface is responsible for parsing the DTOs, interacting with the Model, and updating the View.
 */
public interface Features {

  /**
   * Initializes the application state upon startup.
   * This typically involves loading a default calendar based on system settings
   * and populating the initial view to ensure the user does not see a blank screen.
   */
  void initialize();

  /**
   * Advances the calendar view to the next month.
   * Triggers a refresh of the calendar grid display.
   */
  void nextMonth();

  /**
   * Moves the calendar view to the previous month.
   * Triggers a refresh of the calendar grid display.
   */
  void prevMonth();

  /**
   * Handles the selection of a specific day in the calendar grid.
   * This triggers the retrieval and display of events occurring on the selected date.
   *
   * @param dto the data transfer object containing the selected date
   */
  void selectDay(SelectDayDto dto);

  /**
   * Performs a query or search for events based on specific criteria.
   * This can handle range-based queries or single-date lookups to filter the event list.
   *
   * @param dto the data transfer object containing the search criteria (dates, times, etc.)
   */
  void querEvents(QueryEventDto dto);

  /**
   * Checks if the user is busy at a specific date and time.
   * This method is used for immediate UI feedback (e.g., validating a time slot).
   *
   * @param dto the data transfer object containing the date and time to check
   * @return true if there is an event at the specified time, false otherwise
   */
  boolean checkAvailability(AvailabilityDto dto);

  /**
   * Creates a new calendar with the specified properties.
   *
   * @param dto the data transfer object containing the new calendar's name and timezone
   */
  void createCalendar(CreateCalDto dto);

  /**
   * Switches the currently active calendar context.
   * All subsequent operations (create event, view month) will apply to the selected calendar.
   *
   * @param dto the data transfer object containing the name of the calendar to select
   */
  void selectCalendar(SelectCalDto dto);

  /**
   * Edits the properties (e.g., name, timezone) of the currently active calendar.
   *
   * @param dto the data transfer object containing the new properties for the calendar
   */
  void editCalendar(EditCalDto dto);

  /**
   * Creates a new event in the currently active calendar.
   * Handles parsing of raw input data for event details, including recurrence.
   *
   * @param dto the data transfer object containing raw user input for the event
   */
  void createEvent(CreateEventDto dto);

  /**
   * Edits an existing event in the currently active calendar.
   * Supports editing single events or entire recurrence series based on the DTO scope.
   *
   * @param dto the data transfer object containing the original event reference and new values
   */
  void editEvent(EditEventDto dto);

  /**
   * Copies an event (or events) to a target calendar and date.
   * Supports copying single events, date ranges, or all events on a specific day.
   *
   * @param dto the data transfer object containing the source event/date and target destination
   */
  void copyEvent(CopyEventDto dto);

  /**
   * Exports the events of the currently active calendar to a file.
   * Supports different formats (e.g., CSV, iCal) as specified in the DTO.
   *
   * @param dto the data transfer object containing the filename and desired format
   */
  void exportCalendar(ExportCalDto dto);
}