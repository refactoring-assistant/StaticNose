package calendar.exceptions;

/**
 * Thrown when an attempt is made to add an event that already exists in the calendar.
 * An event is considered a duplicate if another event with the same subject,
 * start time, and end time already exists.
 */
public class DuplicateEventException extends Exception {

  /**
   * Constructs a DuplicateEventException with the specified detail message.
   *
   * @param message the detail message explaining which event is duplicated
   */
  public DuplicateEventException(String message) {
    super(message);
  }
}