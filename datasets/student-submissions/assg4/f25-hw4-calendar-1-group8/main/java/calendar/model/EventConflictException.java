package calendar.model;

/**
 * Exception that is thrown when there is a conflict of events when two events tend to have
 * same subject, start date/time and end date/time.
 */
public class EventConflictException extends CalendarException {
  /**
   * Constructor to construct a new EventConflictException.
   *
   * @param message text to be displayed if this exception is thrown
   */
  public EventConflictException(String message) {
    super(message);
  }
}