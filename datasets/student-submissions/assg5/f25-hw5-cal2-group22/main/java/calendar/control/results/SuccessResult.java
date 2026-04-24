package calendar.control.results;

/**
 * Represents a successful command execution.
 */
class SuccessResult extends CommandResult {
  private final String message;

  SuccessResult(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}