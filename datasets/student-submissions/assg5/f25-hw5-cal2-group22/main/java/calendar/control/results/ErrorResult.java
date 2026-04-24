package calendar.control.results;

/**
 * Represents a failed command execution.
 */
class ErrorResult extends CommandResult {
  private final String message;

  ErrorResult(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return "Error: " + message;
  }
}