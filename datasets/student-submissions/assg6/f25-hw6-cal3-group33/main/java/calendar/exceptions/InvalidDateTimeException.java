package calendar.exceptions;

/**
 * Exception thrown when a date or time string cannot be parsed
 * or when date/time validation fails.
 * This wraps Java's DateTimeParseException and other date/time validation errors
 * to provide user-friendly error messages specific to the calendar application.
 */
public class InvalidDateTimeException extends Exception {

  /**
   * Creates an exception with a detailed message.
   *
   * @param message the error message describing what went wrong
   */
  public InvalidDateTimeException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message and underlying cause.
   * Used when wrapping Java's DateTimeParseException.
   *
   * @param message the error message describing what went wrong
   * @param cause the underlying exception that caused this error
   */
  public InvalidDateTimeException(String message, Throwable cause) {
    super(message, cause);
  }
}