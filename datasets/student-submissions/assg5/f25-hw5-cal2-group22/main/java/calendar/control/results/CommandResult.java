package calendar.control.results;

/**
 * Represents the result of a command execution.
 * This decouples command logic from view presentation.
 */
public abstract class CommandResult {

  /**
   * Returns a message describing the result.
   */
  public abstract String getMessage();

  /**
   * Factory method for success results.
   */
  public static CommandResult success(String message) {
    return new SuccessResult(message);
  }

  /**
   * Factory method for error results.
   */
  public static CommandResult error(String message) {
    return new ErrorResult(message);
  }

  /**
   * Factory method for warning results (success with warnings).
   */
  public static CommandResult warning(String message, String warning) {
    return new WarningResult(message, warning);
  }
}