package calendar.exception;

/**
 * Exception thrown when attempting to access a calendar that does not exist.
 */
public class CalendarNotFoundException extends CalendarException {

  /**
   * Constructs a CalendarNotFoundException with a message.
   *
   * @param message the error message
   */
  public CalendarNotFoundException(String message) {
    super(message);
  }
}