package calendar.exceptions;

/**
 * Exception thrown when attempting to perform operations that require a current calendar,
 * but no calendar has been selected.
 */
public class NoCalendarInUseException extends Exception {

  /**
   * Constructs a NoCalendarInUseException with a default message.
   */
  public NoCalendarInUseException() {
    super("No calendar is currently in use. Use 'use calendar <name>' to select one");
  }

  /**
   * Constructs a NoCalendarInUseException with a custom message.
   *
   * @param message the custom error message
   */
  public NoCalendarInUseException(String message) {
    super(message);
  }
}