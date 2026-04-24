package calendar.view;

import calendar.exceptions.InvalidDateTimeException;
import calendar.model.calendar.ReadOnlyCalendar;

/**
 * Interface for all calendar view implementations.
 * Handles all output/display logic following MVC pattern.
 */
public interface CalendarView {

  /**
   * Displays the welcome message when the application starts in interactive mode.
   */
  void displayWelcome();

  /**
   * Displays the goodbye message when the user exits the application.
   */
  void displayGoodbye();

  /**
   * Displays the list of available commands and their usage.
   */
  void displayCommandOptions();

  /**
   * Displays the command prompt to indicate the application is ready for input.
   */
  void displayPrompt();


  /**
   * Displays a general informational message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  void displayError(String message);

  /**
   * Displays an error message indicating the specified file was not found.
   *
   * @param filePath the path of the file that was not found
   */
  void displayFileNotFound(String filePath);

  /**
   * Displays an error message when a file cannot be read.
   *
   * @param message the detailed error message about the read failure
   */
  void displayFileReadError(String message);

  /**
   * Displays an error message indicating that headless mode requires an 'exit' command.
   */
  void displayNoExitCommand();

  /**
   * Displays all events occurring on a specific date.
   *
   * @param calendar the calendar whose events we are displaying
   * @param date     the date for which events are being displayed
   */
  void displayEvents(ReadOnlyCalendar calendar, String date) throws InvalidDateTimeException;

  /**
   * Displays all events occurring within a specified date range.
   *
   * @param calendar  the calendar whose events we are displaying
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   */
  void displayEventsInRange(ReadOnlyCalendar calendar, String startDate, String endDate)
      throws InvalidDateTimeException;

  /**
   * Displays whether the calendar is busy at a specific date and time.
   *
   * @param calendar true if there are events at the specified time, false otherwise
   * @param dateTime the date and time being queried
   */
  void displayBusyStatus(ReadOnlyCalendar calendar, String dateTime)
      throws InvalidDateTimeException;

  /**
   * Displays a fatal error message that indicates the application cannot continue.
   *
   * @param message the fatal error message to display
   */
  void displayFatalError(String message);

  /**
   * Displays usage information for running the application, including command-line arguments.
   */
  void displayUsageInformation();

  /**
   * Closes the view and releases any resources (such as output streams).
   */
  void close();
}