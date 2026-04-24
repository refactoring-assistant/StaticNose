package calendar.view;

import calendar.model.Ievent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for calendar view operations.
 * This interface decouples the view from the controller,
 * following the Dependency Inversion Principle.
 */
public interface IcalendarView {

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays a prompt for user input (interactive mode).
   */
  void displayPrompt();

  /**
   * Displays events on a specific date.
   *
   * @param date   the date
   * @param events the list of events on that date
   */
  void displayEventsOnDate(LocalDate date, List<Ievent> events);

  /**
   * Displays events within a date-time range.
   *
   * @param start  the start of the range
   * @param end    the end of the range
   * @param events the list of events in the range
   */
  void displayEventsInRange(LocalDateTime start, LocalDateTime end, List<Ievent> events);

  /**
   * Displays the busy/available status at a specific time.
   *
   * @param dateTime the date and time
   * @param busy     true if busy, false if available
   */
  void displayStatus(LocalDateTime dateTime, boolean busy);
}