package calendar.exceptions;

/**
 * Thrown when an edit operation would create an invalid state.
 * Examples:
 * - Edit would create a duplicate event
 * - Edit would violate temporal constraints (start >= end)
 * - Edit would violate series constraints (multi-day event, date changes)
 */
public class EditConflictException extends Exception {

  /**
   * Constructs an EditConflictException with the specified detail message.
   *
   * @param message the detail message explaining the edit conflict
   */
  public EditConflictException(String message) {
    super(message);
  }

  /**
   * Constructs an EditConflictException with the specified detail message and cause.
   *
   * @param message the detail message explaining the edit conflict
   * @param cause the cause of this exception (which is saved for later retrieval)
   */
  public EditConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}