package calendar.util;

/**
 * Exception thrown when an attempt is made to add or edit an event
 * that would conflict with an existing event in the calendar.
 */
public class ConflictException extends Exception {

  /**
   * Constructs a ConflictException with no message.
   */
  public ConflictException() {
    super();
  }

  /**
   * Constructs a ConflictException with a specific message.
   *
   * @param message The detail message describing the conflict.
   */
  public ConflictException(String message) {
    super(message);
  }

  /**
   * Constructs a ConflictException with a specific message and cause.
   *
   * @param message The detail message describing the conflict.
   * @param cause The underlying cause of the conflict.
   */
  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a ConflictException with a cause.
   *
   * @param cause The underlying cause of the conflict.
   */
  public ConflictException(Throwable cause) {
    super(cause);
  }
}
