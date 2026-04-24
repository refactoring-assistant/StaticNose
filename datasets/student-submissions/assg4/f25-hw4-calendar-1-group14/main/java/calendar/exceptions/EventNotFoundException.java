package calendar.exceptions;

/**
 * Exception class for not finding events.
 */
public class EventNotFoundException extends Exception {
  /**
   * Constructor for exception.
   *
   * @param message to be displayed during the exception.
   */
  public EventNotFoundException(String message) {
    super(message);
  }
}
