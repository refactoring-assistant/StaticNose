package calendar.exception;

/**
 * Exception thrown when date or time values are invalid or improperly formatted.
 */
public class InvalidDateTimeException extends CalendarException {

  /**
   * Constructs an InvalidDateTimeException with a message.
   *
   * @param message the error message describing the date/time issue
   */
  public InvalidDateTimeException(String message) {
    super(message);
  }

  /**
   * Constructs an InvalidDateTimeException with a message and cause.
   *
   * @param message the error message
   * @param cause   the underlying cause
   */
  public InvalidDateTimeException(String message, Throwable cause) {
    super(message, cause);
  }
}