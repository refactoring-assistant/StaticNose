package calendar.controller;

/**
 * Represents the outcome of executing a command.
 */
public class CommandResult {
  private final String message;
  private final boolean shouldExit;

  /**
   * Creates a command result.
   *
   * @param message    message to present to the user
   * @param shouldExit whether the runner should stop processing further commands
   */
  public CommandResult(String message, boolean shouldExit) {
    this.message = message;
    this.shouldExit = shouldExit;
  }

  /**
   * Gets the text we plan to share with the user.
   *
   * @return text of the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Reports if the caller should wrap up and exit.
   *
   * @return true when the session should end
   */
  public boolean shouldExit() {
    return shouldExit;
  }

  /**
   * Convenience factory for a non-terminating result.
   *
   * @param message output message
   * @return the command result
   */
  public static CommandResult message(String message) {
    return new CommandResult(message, false);
  }

  /**
   * Convenience factory for an exit result.
   *
   * @param message final message
   * @return the command result
   */
  public static CommandResult exit(String message) {
    return new CommandResult(message, true);
  }
}
