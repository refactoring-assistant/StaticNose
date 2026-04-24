package calendar.view;

import calendar.model.InEvent;
import java.util.List;

/**
 * View contract for displaying information to user.
 * Keeps view implementation details separate from controller.
 * This interface enables multiple view types (CLI, GUI, web) without changing controller.
 * Controller depends on abstraction, not concrete view.
 */
public interface InCalendarView {

  /**
   * Displays a general message to the user.
   * Used for informational output that is neither success nor error.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   * Should be visually distinct from regular messages (e.g., prefixed with "ERROR:").
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays a success message to the user.
   * Used to confirm successful command execution.
   *
   * @param message the success message to display
   */
  void displaySuccess(String message);

  /**
   * Displays a list of events to the user.
   * Should format events in a readable way with date, time, and location information.
   * If the list is empty, should display "No events found."
   *
   * @param events the list of events to display
   */
  void displayEvents(List<InEvent> events);
}