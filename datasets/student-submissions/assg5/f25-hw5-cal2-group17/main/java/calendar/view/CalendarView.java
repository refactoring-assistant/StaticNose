package calendar.view;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for the calendar view component.
 */
public interface CalendarView {

  /**
   * Displays a success message.
   *
   * @param message Message as String
   */
  void displaySuccess(String message);

  /**
   * Displays an error message.
   *
   * @param message Message as String
   */
  void displayError(String message);

  /**
   * Displays events on a specific date.
   *
   * @param date   The date
   * @param events List of events on that date
   */
  void displayEventsOnDate(LocalDate date, List<Event> events);

  /**
   * Displays events within a date/time range.
   *
   * @param start  Start of range
   * @param end    End of range
   * @param events List of events in range
   */
  void displayEventsInRange(LocalDateTime start, LocalDateTime end, List<Event> events);

  /**
   * Displays busy/available status at a specific time.
   *
   * @param dateTime Time to check
   * @param busy     True if busy, false if available
   */
  void displayStatus(LocalDateTime dateTime, boolean busy);

  /**
   * Displays a prompt for user input (interactive mode).
   */
  void displayPrompt();

  /**
   * Displays a welcome message (interactive mode).
   */
  void displayWelcome();
}