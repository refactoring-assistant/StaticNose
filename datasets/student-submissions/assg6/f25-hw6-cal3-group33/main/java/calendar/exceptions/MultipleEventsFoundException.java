package calendar.exceptions;

/**
 * Thrown when event lookup is ambiguous.
 * This occurs when searching for an event by subject, start, and end time
 * returns more than one match.
 */
public class MultipleEventsFoundException extends Exception {

  /**
   * Constructs a MultipleEventsFoundException with the specified detail message.
   *
   * @param message the detail message explaining which events were found
   */
  public MultipleEventsFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a MultipleEventsFoundException with the specified detail message and cause.
   *
   * @param message the detail message explaining which events were found
   * @param cause the cause of this exception (which is saved for later retrieval)
   */
  public MultipleEventsFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}