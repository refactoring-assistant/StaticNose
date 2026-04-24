package calendar.view;

/**
 * An interface representing the view component for displaying output to the user.
 */
public interface IntView {
  /**
   * Writes a message to the output.
   *
   * @param message the message to write
   * @throws IllegalStateException if writing fails
   */
  void write(String message) throws IllegalStateException;

  /**
   * Writes a message to the output with a new line at the end.
   *
   * @param message the message to write
   * @throws IllegalStateException if writing fails
   */
  void writeln(String message) throws IllegalStateException;
}
