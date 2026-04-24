package calendar.view;

import calendar.model.Event;
import java.util.List;

/**
 * View interface for displaying calendar information to the user.
 */
public interface CalendarView {
  /**
   * Display a general message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Display an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Display a list of events.
   *
   * @param events the events to display
   */
  void displayEvents(List<Event> events);

  /**
   * Display the busy/available status.
   *
   * @param isBusy true if busy, false if available
   */
  void displayStatus(boolean isBusy);
}
