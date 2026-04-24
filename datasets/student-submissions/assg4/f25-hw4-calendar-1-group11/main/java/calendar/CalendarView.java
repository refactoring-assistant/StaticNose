package calendar;

/**
 * Interface for the calendar view component that handles displaying information to users.
 */
public interface CalendarView {

  /**
   * Displays a success message to the user.
   *
   * @param message the success message to display
   */
  void displaySuccess(String message);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  void displayError(String message);

  /**
   * Displays a prompt to the user.
   */
  void displayPrompt();

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);
}