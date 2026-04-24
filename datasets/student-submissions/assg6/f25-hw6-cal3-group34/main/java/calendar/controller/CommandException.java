package calendar.controller;

/**
 * Represents an error encountered while parsing or executing a command.
 */
public class CommandException extends Exception {
  /**
   * Creates a new command exception with the supplied message.
   *
   * @param message details describing the error
   */
  public CommandException(String message) {
    super(message);
  }

  /**
   * Creates a new command exception with a message and cause.
   *
   * @param message explanation of the error
   * @param cause   the underlying cause
   */
  public CommandException(String message, Throwable cause) {
    super(message, cause);
  }
}
