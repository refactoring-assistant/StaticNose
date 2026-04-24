package calendar.view;

import calendar.model.Event;
import java.util.List;

/**
 * Interface for the calendar application view.
 */
public interface CalendarView {

  /**
   * Displays a message to the user.
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
   * Displays a list of events in a formatted manner.
   *
   * @param events the list of events to display
   */
  void displayEvents(List<Event> events);

  /**
   * Displays a welcome message when the application starts.
   */
  void displayWelcome();

  /**
   * Displays a goodbye message when the application exits.
   */
  void displayGoodbye();

  /**
   * Displays a prompt for user input.
   *
   * @param prompt the prompt text to display
   */
  void displayPrompt(String prompt);

  /**
   * Displays the result of a command execution.
   *
   * @param result the result message to display
   */
  void displayResult(String result);
}