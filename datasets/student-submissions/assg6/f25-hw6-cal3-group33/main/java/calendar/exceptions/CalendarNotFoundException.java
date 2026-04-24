package calendar.exceptions;

/**
 * Exception thrown when attempting to access a calendar that does not exist.
 */
public class CalendarNotFoundException extends Exception {

  /**
   * Constructs a CalendarNotFoundException with a default message.
   *
   * @param calendarName the name of the calendar that was not found
   */
  public CalendarNotFoundException(String calendarName) {
    super("Calendar '" + calendarName + "' not found");
  }
}