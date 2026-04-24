package calendarmodel.exceptions;

/**
 * An exception thrown when an edit operation matches
 * multiple events, making the command ambiguous.
 */
public class AmbiguousEditException extends Exception {
  /**
   * Constructs a new AmbiguousEditException with the specified detail message.
   *
   * @param message The detail message.
   */
  public AmbiguousEditException(String message) {
    super(message);
  }
}
