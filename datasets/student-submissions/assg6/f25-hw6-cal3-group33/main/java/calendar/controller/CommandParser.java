package calendar.controller;

import calendar.controller.command.Command;
import calendar.controller.command.HelpCommand;
import calendar.exceptions.InvalidCommandException;
import java.util.regex.Matcher;

/**
 * Parses user command strings into executable Command objects.
 *
 * <p>This class acts as the entry point for command processing in the calendar application.
 * It takes a raw command string
 * (e.g., "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00"),
 * matches it against known command patterns, and returns the appropriate Command object
 * that can be executed by the controller.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Validate command syntax using regex patterns</li>
 *   <li>Match commands to the appropriate command type</li>
 *   <li>Delegate command object creation to the factory</li>
 * </ul>
 *
 * <p><b>Design:</b>
 * The parser follows the Command Pattern and delegates responsibilities to:
 * <ul>
 *   <li>CommandPattern - Defines all valid command patterns</li>
 *   <li>CommandFactory - Creates Command objects from matched patterns</li>
 * </ul>
 */
public class CommandParser {

  private final CommandFactory factory;

  /**
   * Creates a new CommandParser with a CommandFactory for creating Command objects.
   */
  public CommandParser() {
    this.factory = new CommandFactory();
  }

  /**
   * Parses a command string and returns the corresponding Command object.
   *
   * <p>The parser attempts to match the command string against all known patterns
   * in order of specificity. More specific patterns (e.g., "create event ... repeats ...")
   * are checked before general patterns (e.g., "create event ...") to ensure correct matching.
   *
   * <p><b>Supported Commands:</b>
   * <ul>
   *   <li>Calendar Management: create calendar, edit calendar, use calendar</li>
   *   <li>Event Creation: create event (single, repeating, all-day)</li>
   *   <li>Event Editing: edit event, edit events, edit series</li>
   *   <li>Queries: print events, show status</li>
   *   <li>Export: export cal</li>
   *   <li>Copy: copy event, copy events</li>
   *   <li>Help: help</li>
   * </ul>
   *
   * @param commandLine the command string to parse (e.g., "create event Meeting from ...")
   * @return a Command object that can be executed
   * @throws InvalidCommandException if the command syntax doesn't match any known pattern
   */
  public Command parse(String commandLine) throws InvalidCommandException {

    if (commandLine.equalsIgnoreCase("help")) {
      return new HelpCommand();
    }

    for (CommandPattern pattern : CommandPattern.values()) {
      Matcher matcher = pattern.matcher(commandLine);
      if (matcher.matches()) {
        return factory.createCommand(pattern, matcher);
      }
    }

    throw new InvalidCommandException(commandLine);
  }
}