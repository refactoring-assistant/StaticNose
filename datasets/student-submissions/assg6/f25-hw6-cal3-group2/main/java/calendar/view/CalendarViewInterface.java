package calendar.view;

import calendar.controller.Features;
import calendar.enums.UserStatus;
import calendar.event.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents operations for displaying calendar information and messages.
 * Supports both text-based and graphical user interfaces.
 */
public interface CalendarViewInterface {

  /**
   * Display a month view with events.
   * This is primarily for GUI implementation.
   *
   * @param year   the year to display
   * @param month  the month to display (1-12)
   * @param events a map of dates to lists of events on those dates
   */
  void displayMonthView(int year, int month, Map<LocalDate, List<Event>> events);

  /**
   * Display the currently selected calendar information.
   *
   * @param calendarName the name of the current calendar
   * @param timezone     the timezone of the current calendar
   */
  void displayCurrentCalendar(String calendarName, String timezone);

  /**
   * Display a list of available calendars.
   *
   * @param calendarNames the list of calendar names
   */
  void displayAvailableCalendars(List<String> calendarNames);

  /**
   * Display events scheduled on a specific date.
   *
   * @param events the list of events on the given date
   * @param date   the date to display events for
   */
  void displayEventsOnDate(List<Event> events, LocalDate date);

  /**
   * Display events within a date-time range.
   *
   * @param events the list of events within the range
   * @param start  the start date-time of the range
   * @param end    the end date-time of the range
   */
  void displayEventsBetween(List<Event> events, LocalDateTime start, LocalDateTime end);

  /**
   * Display the user's availability status.
   *
   * @param status the user status (BUSY or AVAILABLE)
   */
  void displayUserStatus(UserStatus status);

  /**
   * Export calendar data to a file.
   *
   * @param content  the content to export (CSV or iCal formatted)
   * @param fileName the destination file name
   */
  void exportCalendar(String content, String fileName);

  /**
   * Display a success message.
   *
   * @param message the success message to display
   */
  void displaySuccess(String message);

  /**
   * Display an error message.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Display a warning message.
   *
   * @param warning the warning message to display
   */
  void displayWarning(String warning);

  /**
   * Display the welcome message when the application starts.
   */
  void displayWelcome();

  /**
   * Make the view visible (primarily for GUI).
   */
  void makeVisible();

  /**
   * Refresh the view with current data.
   */
  void refresh();

  /**
   * Register callback features with the view.
   *
   * @param feature the Features implementation handle user interaction callbacks from view
   */
  void addFeatures(Features feature);
}