package calendar.view;

/**
 * This interface is the view of the application. It holds messages, and shows messages, including
 * errors and directions.
 */
public interface CalendarViewInterface {

  /**
   * Shows error to user.
   *
   * @param errorMessage the error message
   */
  void showError(String errorMessage);

  /**
   * Shows message to user.
   *
   * @param message the message
   */
  void showMessage(String message);

  /**
   * Gets command from user.
   *
   * @return the command entered
   */
  String getCommand();
}
