package calendar.exception;

/**
 * Exception thrown when attempting to create an event that duplicates an existing event.
 * Two events are considered duplicates if they have the same subject, start date/time,
 * and end date/time.
 */
public class DuplicateEventException extends CalendarException {

  /**
   * Constructs a DuplicateEventException with a message.
   *
   * @param message the error message
   */
  public DuplicateEventException(String message) {
    super(message);
  }
}