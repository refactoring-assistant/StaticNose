package calendar.controller.parser;

import calendar.controller.command.Icommand;
import calendar.exceptions.InvalidCommandException;

/**
 * Interface for parsing text commands into Command objects.
 */
public interface IcommandParser {

  /**
   * Parse a command string and return the appropriate Command object.
   *
   * @param commandString the command string to parse
   * @return the Command object representing the parsed command, or null for exit command
   * @throws InvalidCommandException if the command cannot be parsed or is invalid
   */
  Icommand parseCommand(String commandString) throws InvalidCommandException;
}

