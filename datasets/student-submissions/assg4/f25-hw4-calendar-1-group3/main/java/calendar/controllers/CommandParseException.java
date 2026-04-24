package calendar.controllers;

/**
 * Exception class used for invalid commands.
 */
public class CommandParseException extends Exception {

  /**
   * Create the exception for invalid commands.
   *
   * @param message error describing the invalid command
   */
  public CommandParseException(String message) {
    super(message);
  }
}