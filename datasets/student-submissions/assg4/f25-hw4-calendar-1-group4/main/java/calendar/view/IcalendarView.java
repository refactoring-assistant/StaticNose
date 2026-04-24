package calendar.view;

/**
 * Defines the contract for the calendar application's view layer.
 * The view is responsible for displaying messages, results, and feedback
 * to the user in both interactive and headless modes.
 */
public interface IcalendarView {

  /**
   * Displays a message or result to the user.
   *
   * @param output the message or result to display
   */
  void showOutput(String output);
}
