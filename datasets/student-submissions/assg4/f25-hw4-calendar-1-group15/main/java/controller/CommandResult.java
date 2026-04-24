
package controller;

/**
 * Represents the result of executing a command in the calendar system.
 * This class encapsulates the success status, message, and optionally
 * indicates whether the application should exit.
 */
public class CommandResult {
  private final boolean success;
  private final String message;
  private final boolean shouldExit;

  /**
   * Constructs a CommandResult with success status and message.
   * The shouldExit flag defaults to false.
   *
   * @param success true if the command succeeded, false otherwise
   * @param message the result or error message
   */
  public CommandResult(boolean success, String message) {
    this(success, message, false);
  }

  /**
   * Constructs a CommandResult with all properties specified.
   *
   * @param success    true if the command succeeded, false otherwise
   * @param message    the result or error message
   * @param shouldExit true if the application should terminate
   */
  public CommandResult(boolean success, String message, boolean shouldExit) {
    this.success = success;
    this.message = message;
    this.shouldExit = shouldExit;
  }

  /**
   * Checks if the command executed successfully.
   *
   * @return true if successful, false otherwise
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Gets the result or error message.
   *
   * @return the message describing the command result
   */
  public String getMessage() {
    return message;
  }

  /**
   * Checks if the application should exit.
   *
   * @return true if the application should terminate, false otherwise
   */
  public boolean shouldExit() {
    return shouldExit;
  }

  /**
   * Returns the message as the string representation.
   *
   * @return the result message
   */
  @Override
  public String toString() {
    return message;
  }
}