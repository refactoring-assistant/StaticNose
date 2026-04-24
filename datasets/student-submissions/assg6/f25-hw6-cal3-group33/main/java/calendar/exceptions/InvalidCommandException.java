package calendar.exceptions;

/**
 * Thrown when a command cannot be parsed or recognized.
 * This occurs when the user enters a command that does not match
 * any valid command format.
 */
public class InvalidCommandException extends Exception {

  /**
   * Constructs an InvalidCommandException for the specified invalid command.
   * The exception message includes the invalid command and suggests using 'help'.
   *
   * @param command the invalid command that was entered
   */
  public InvalidCommandException(String command) {
    super("Invalid command: '" + command + "'. Type 'help' for available commands.");
  }
}