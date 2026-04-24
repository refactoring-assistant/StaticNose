package multicalendarmodel;

/**
 * Thrown when an operation on a {@link MultiCalendarModel} fails
 * due to a calendar name issue.
 *
 * <p>This includes cases such as:
 * <ul>
 * <li>Attempting to create a calendar with a name that already exists.</li>
 * <li>Attempting to modify or retrieve a calendar that does not exist.</li>
 * <li>Attempting to rename a calendar to a name that already exists.</li>
 * </ul>
 * </p>
 */
public class CalendarNameException extends Exception {

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message.
   */
  public CalendarNameException(String message) {
    super(message);
  }
}