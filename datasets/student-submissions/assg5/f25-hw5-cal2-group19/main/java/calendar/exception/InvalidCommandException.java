package calendar.exception;

/**
 * Exception thrown when a user provides an invalid or malformed command.
 */
public class InvalidCommandException extends CalendarException {

  /**
   * Constructs an InvalidCommandException with a message.
   *
   * @param message the error message describing why the command is invalid
   */
  public InvalidCommandException(String message) {
    super(message);
  }

  /**
   * Constructs an InvalidCommandException with a message and cause.
   *
   * @param message the error message
   * @param cause   the underlying cause of the exception
   */
  public InvalidCommandException(String message, Throwable cause) {
    super(message, cause);
  }
}