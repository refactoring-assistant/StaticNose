package calendar.exception;

/**
 * Runtime exception thrown when attempting to modify a read-only calendar.
 * Used during copy operations to prevent modifications to source calendars.
 */
public class ReadOnlyCalendarException extends RuntimeException {

  /**
   * Constructs a ReadOnlyCalendarException with a message.
   *
   * @param message the error message
   */
  public ReadOnlyCalendarException(String message) {
    super(message);
  }

}