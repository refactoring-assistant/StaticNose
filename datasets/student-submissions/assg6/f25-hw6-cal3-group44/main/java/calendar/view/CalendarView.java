package calendar.view;

/**
 * Interface for Calendar View.
 */
public interface CalendarView {

  /**
   * Used to append and render the string message passed to the view.
   *
   * @param message that is parsed to the view.
   */
  void render(String message);

  /**
   * Used to append and render the string Error message.
   * passed to the view with different styling.
   *
   * @param message that is parsed to the view.
   */
  void renderError(String message);
}
