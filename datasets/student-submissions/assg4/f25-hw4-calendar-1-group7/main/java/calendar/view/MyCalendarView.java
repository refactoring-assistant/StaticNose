package calendar.view;

import calendar.model.Event;
import java.util.List;

/**
 * Interface for displaying calendar information to the user.
 * Supports multiple output formats (text, GUI, etc.)
 */
public interface MyCalendarView {

  /**
   * Displays a list of events.
   *
   * @param events the events to display
   */
  void displayEvents(List<Event> events);

  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message
   */
  void displayError(String error);

  /**
   * Reads a command from the user.
   *
   * @return the command string
   */
  String readCommand();
}