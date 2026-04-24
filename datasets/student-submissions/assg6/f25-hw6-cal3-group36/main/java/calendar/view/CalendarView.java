package calendar.view;

import java.io.IOException;

/**
 * Defines an abstract view layer for the calendar application.
 */
public interface CalendarView {

  /**
   * Displays the provided message to the user.
   *
   * @param message message text
   * @throws IOException if writing fails
   */
  void renderMessage(String message) throws IOException;

  /**
   * Reads the next line of input if applicable.
   *
   * @return input line or null if not available
   * @throws IOException if reading fails
   */
  String readInput() throws IOException;
}