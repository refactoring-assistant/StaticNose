package calendar.view;


/**
 * Base interface for both interactive and headless views.
 */
public interface Iview {
  /**
   * Displays a message or output to the user.
   *
   * @param message the text to display
   */
  void print(String message);

  /**
   * Reads a single line of user input.
   *
   * @return the input string, or null if none is available
   */
  String readInput();

  /**
   * Returns the path of the input file (used only in headless mode).
   *
   * @return the command source file path, or null if not applicable
   */
  String getSourcePath();
}
