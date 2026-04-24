package calendar.view;

import calendar.model.EventInterface;
import java.util.List;

/**
 * This interface supports both interactive (e.g., GUI or console) and headless modes for
 * displaying events, status, errors, and retrieving user input.
 */
public interface CalendarViewInterface {

  /**
   * Displays a list of calendar events to the user.
   *
   * @param events the list of {@link EventInterface} objects to be displayed.
   */
  void showEvents(List<EventInterface> events);

  /**
   * Displays the availability status of a time slot, indicating whether it is busy
   * and, if applicable, the conflicting event.
   *
   * @param isBusy true if the time slot is busy, false otherwise.
   * @param conflictingEvent the {@link EventInterface} that causes a conflict, or null if none.
   */
  void showStatus(boolean isBusy, EventInterface conflictingEvent);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to be displayed.
   */
  void showError(String message);

  /**
   * Retrieves input from the user, typically for interactive modes. In headless mode, this
   * may return pre-configured or default input.
   *
   * @return the user input as a String.
   */
  String getUserInput();

  /**
   * Displays a generic message to the user (info, confirmation, or log output).
   * All output from the runner or commands should go through this.
   *
   * @param message the message to display.
   */
  void showMessage(String message);
}