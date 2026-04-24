package calendar.view;

import java.io.IOException;
import java.util.List;

/**
 * Interface for displaying calendar information.
 */
public interface Iview {

  /**
   * Display a general message to the user.
   *
   * @param message the message to display.
   */
  void displayMessage(String message) throws IOException;

  /**
   * Display an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error) throws IOException;

  /**
   * Display a list of output lines.
   *
   * @param output the lines to display
   */
  void displayOutput(List<String> output) throws IOException;
}
