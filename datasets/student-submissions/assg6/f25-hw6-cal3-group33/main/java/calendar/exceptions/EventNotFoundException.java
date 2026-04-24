package calendar.exceptions;

/**
 * Thrown when an event cannot be found during lookup.
 * This occurs when searching for an event by subject, start, and end time
 * returns zero matches.
 */
public class EventNotFoundException extends Exception {

  /**
   * Constructs an EventNotFoundException with the specified detail message.
   *
   * @param message the detail message explaining why the event was not found
   */
  public EventNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs an EventNotFoundException with the specified detail message and cause.
   *
   * @param message the detail message explaining why the event was not found
   * @param cause the cause of this exception (which is saved for later retrieval)
   */
  public EventNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}