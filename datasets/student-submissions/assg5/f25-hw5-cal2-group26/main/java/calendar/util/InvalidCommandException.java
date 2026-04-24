package calendar.util;

/**
 * Exception thrown when an invalid or unrecognized command
 * is encountered by the calendar application.
 */
public class InvalidCommandException extends Exception {

  /**
   * Constructs an InvalidCommandException with no message.
   */
  public InvalidCommandException() {
    super();
  }

  /**
   * Constructs an InvalidCommandException with a specific message.
   *
   * @param message the detail message describing the invalid command.
   */
  public InvalidCommandException(String message) {
    super(message);
  }

  /**
   * Constructs an InvalidCommandException with a specific message and cause.
   *
   * @param message the detail message describing the invalid command.
   * @param cause the underlying cause of the exception.
   */
  public InvalidCommandException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs an InvalidCommandException with a cause.
   *
   * @param cause the underlying cause of the exception.
   */
  public InvalidCommandException(Throwable cause) {
    super(cause);
  }
}

