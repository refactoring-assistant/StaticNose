package calendar.exception;

/**
 * Exception thrown when attempting to create a calendar with a name that already exists.
 */
public class DuplicateCalendarException extends CalendarException {

  /**
   * Constructs a DuplicateCalendarException with a message.
   *
   * @param message the error message
   */
  public DuplicateCalendarException(String message) {
    super(message);
  }
}