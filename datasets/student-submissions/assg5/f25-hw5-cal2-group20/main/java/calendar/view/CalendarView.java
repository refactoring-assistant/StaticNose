package calendar.view;

/**
 * Represents a Calendar view interface needed for MVC design pattern. Responsible for showing
 * useful messages to the client during application run.
 */
public interface CalendarView {

  /**
   * Prints a bulleted list of all events given in a string form.
   *
   * @param events all events to print in a string form.
   */
  void printEvents(String events);

  /**
   * Renders a provided message to the client.
   *
   * @param message the message to show to users.
   */
  void renderMessage(String message);

  /**
   * Prints busy or available status supplied from model in string format.
   *
   * @param status the status to show.
   */
  void showUserStatus(String status);

}
