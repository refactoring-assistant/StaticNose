package calendar.control.results;

/**
 * Represents a successful command with warnings.
 */
class WarningResult extends CommandResult {
  private final String message;
  private final String warning;

  WarningResult(String message, String warning) {
    this.message = message;
    this.warning = warning;
  }

  @Override
  public String getMessage() {
    return message + "\nWarning: " + warning;
  }
}