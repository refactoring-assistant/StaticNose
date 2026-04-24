package calendar.exceptions;

/**
 * Exception thrown when attempting to create a calendar with a name that already exists.
 */
public class DuplicateCalendarException extends Exception {

  /**
   * Constructs a DuplicateCalendarException with a default message.
   *
   * @param calendarName the name of the duplicate calendar
   */
  public DuplicateCalendarException(String calendarName) {
    super("Calendar with name '" + calendarName + "' already exists");
  }

  /**
   * Constructs a DuplicateCalendarException with a custom message.
   *
   * @param message the custom error message
   */
  public DuplicateCalendarException(String calendarName, String message) {
    super(message);
  }
}