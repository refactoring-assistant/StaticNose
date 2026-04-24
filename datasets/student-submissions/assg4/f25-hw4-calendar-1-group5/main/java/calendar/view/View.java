package calendar.view;

/**
 * This interface represents the required methods to supported by the view layer of the calendar
 * application.
 */
public interface View {
  /**
   * A method to display a String message on to the view.
   *
   * @param message String message to be displayed.
   */
  void render(String message);
}
