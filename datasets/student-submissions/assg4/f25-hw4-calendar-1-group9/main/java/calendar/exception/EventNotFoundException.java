package calendar.exception;

/**
 * Exception thrown when attempting to access or modify an event that does not exist
 * in the calendar.
 */
public class EventNotFoundException extends CalendarException {

  /**
   * Constructs an EventNotFoundException with a message.
   *
   * @param message the error message
   */
  public EventNotFoundException(String message) {
    super(message);
  }
}
