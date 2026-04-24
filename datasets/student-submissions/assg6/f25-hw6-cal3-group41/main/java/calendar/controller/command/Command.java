package calendar.controller.command;

/**
 * Interface for command pattern. Each command encapsulates a request as an object.
 */
public interface Command {

  /**
   * Executes the command and returns the result message.
   *
   * @param commandString the full command string to parse and execute
   * @return the result message from executing the command
   * @throws IllegalArgumentException if the command format is invalid
   */
  String execute(String commandString);
}

