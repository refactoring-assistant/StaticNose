package calendar.model.exceptions;

/**
 * Represents an exception class.
 */
public class ConflictException extends RuntimeException {

  /**
   * Throws ConflictException.
   *
   * @param message The message to be included in exception.
   */
  public ConflictException(String message) {
    super(message);
  }
}
