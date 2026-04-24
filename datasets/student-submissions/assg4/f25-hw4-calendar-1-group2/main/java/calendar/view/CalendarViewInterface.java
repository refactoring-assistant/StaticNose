package calendar.view;

import calendar.enums.UserStatus;
import calendar.event.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for the calendar view component.
 * Defines operations for displaying calendar information and messages.
 */
public interface CalendarViewInterface {
  /**
   * Display events scheduled on a specific date.
   *
   * @param events the list of events on the given date
   * @param date the date to display events for
   */
  void displayEventsOnDate(List<Event> events, LocalDate date);

  /**
   * Display events within a date-time range.
   *
   * @param events the list of events within the range
   * @param start the start date-time of the range
   * @param end the end date-time of the range
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
   * @param content the content to export (CSV formatted)
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
}