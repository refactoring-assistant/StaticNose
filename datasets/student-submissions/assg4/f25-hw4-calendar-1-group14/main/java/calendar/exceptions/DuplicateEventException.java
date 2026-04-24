package calendar.exceptions;

/**
 * Exception class for Duplicate events.
 */
public class DuplicateEventException extends Exception {

  /**
   * Constructor for exception.
   *
   * @param message to be displayed during the exception.
   */
  public DuplicateEventException(String message) {
    super(message);
  }
}
