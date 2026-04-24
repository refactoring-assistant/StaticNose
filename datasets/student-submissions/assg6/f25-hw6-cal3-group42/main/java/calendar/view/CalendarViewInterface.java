package calendar.view;

/**
 * Interface dictating the types of display methods the view can do.
 */
public interface CalendarViewInterface {

  /**
   * Displays any message sent by the controller.
   *
   * @param message the message string
   */
  void display(String message);

  /**
   * Displays any error sent by the controller.
   *
   * @param message the error message string
   */
  void displayError(String message);
}
