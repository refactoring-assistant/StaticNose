package calendarmodel.exceptions;

/**
 * An exception thrown when an operation would result
 * in a duplicate event (same subject, start, and end time).
 */
public class DuplicateEventException extends Exception {
  /**
   * Constructs a new DuplicateEventException with the specified detail message.
   *
   * @param message The detail message.
   */
  public DuplicateEventException(String message) {
    super(message);
  }
}
