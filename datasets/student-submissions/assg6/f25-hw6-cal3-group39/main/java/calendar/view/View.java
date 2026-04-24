package calendar.view;

import calendar.model.InterfaceEvent;
import java.util.List;

/**
 * Represents the view component in the MVC pattern.
 * Responsible for displaying information to the user and prompting for input.
 */
public interface View {

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display
   */
  void showMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param errorMessage the error message to display
   */
  void showError(String errorMessage);

  /**
   * Displays a list of events in a user-readable format.
   *
   * @param events the list of events to display
   */
  void displayEvents(List<InterfaceEvent> events);

  /**
   * Prompts the user to enter a command.
   * Typically used in interactive mode to indicate readiness for input.
   */
  void promptCommand();
}
