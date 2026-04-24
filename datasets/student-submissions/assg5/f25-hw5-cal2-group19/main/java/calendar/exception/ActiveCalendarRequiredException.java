package calendar.exception;

/**
 * Runtime exception thrown when an operation requires an active calendar but none is set.
 * This is a RuntimeException because it's a precondition violation that should be
 * caught and handled by the command parser/controller layer.
 */
public class ActiveCalendarRequiredException extends RuntimeException {

  /**
   * Constructs an ActiveCalendarRequiredException with a message.
   *
   * @param message the error message
   */
  public ActiveCalendarRequiredException(String message) {
    super(message);
  }

}