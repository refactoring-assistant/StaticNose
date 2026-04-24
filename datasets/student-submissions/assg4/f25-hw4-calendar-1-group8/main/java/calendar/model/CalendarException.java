package calendar.model;

/**
 * Class to define a custom exception for app specific errors in calendar model.
 */
public class CalendarException extends Exception {

  /**
   * Constructor to construct a new CalendarException with a specific message for what error
   * encountered.
   *
   * @param message text to be displayed when an error is thrown
   */
  public CalendarException(String message) {
    super(message);
  }
}