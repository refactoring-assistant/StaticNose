package calendar.view;

/**
 * Interface for calendar view operations.
 */
public interface IcalendarView {
  /**
   * Displays a message to the user.
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   */
  void displayError(String error);
}