package calendar.view;

/**
 * The controller uses this interface to display output and request input.
 */
public interface CalendarView {

  /**
   * used to show command outputs or errors.
   */
  void println(String s);

  /**
   * Used to indicate that the program is waiting for user input.
   */
  void printPrompt(String prompt);
}
