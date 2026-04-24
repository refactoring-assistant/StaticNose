package calendarmodel.exceptions;

/**
 * An exception thrown when an edit or find operation
 * cannot locate the specified event.
 */
public class EventNotFoundException extends Exception {
  /**
   * Constructs a new EventNotFoundException with the specified detail message.
   *
   * @param message The detail message.
   */
  public EventNotFoundException(String message) {
    super(message);
  }
}
