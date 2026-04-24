package calendar.exception;

/**
 * Base exception class for all calendar-related errors.
 * Provides common exception handling for the calendar application.
 */
public class CalendarException extends Exception {

  /**
   * Constructs a CalendarException with a message.
   *
   * @param message the error message
   */
  public CalendarException(String message) {
    super(message);
  }

  /**
   * Constructs a CalendarException with a message and cause.
   *
   * @param message the error message
   * @param cause   the underlying cause of the exception
   */
  public CalendarException(String message, Throwable cause) {
    super(message, cause);
  }
}