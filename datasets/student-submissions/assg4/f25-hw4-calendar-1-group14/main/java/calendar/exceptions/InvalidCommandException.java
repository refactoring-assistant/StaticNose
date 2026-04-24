package calendar.exceptions;

/**
 * Exception class for not finding events.
 */
public class InvalidCommandException extends Exception {

  /**
   * Constructor for exception.
   *
   * @param message to be displayed during the exception.
   */
  public InvalidCommandException(String message) {
    super(message);
  }
}
