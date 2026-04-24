package calendar.controller;

import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsCommand;
import calendar.controller.commands.CopyEventsRangeCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.ExportCalendarCommand;
import calendar.controller.commands.PrintEventsCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.controller.commands.UseCalendarCommand;

/**
 * Parses user input into commands.
 */
public class CommandParser {

  /**
   * Parses user input into commands.
   *
   * @param input the user input
   * @return the corresponding command
   * @throws IllegalArgumentException if input is invalid
   */
  public static Command parse(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Empty command");
    }

    String[] tokens = input.trim().split("\\s+");

    switch (tokens[0].toLowerCase()) {
      case "create":
        if (tokens.length > 1) {
          if ("event".equals(tokens[1])) {
            return new CreateEventCommand(tokens);
          } else if ("calendar".equals(tokens[1])) {
            return new CreateCalendarCommand(tokens);
          }
        }
        break;
      case "edit":
        if (tokens.length > 1) {
          if ("event".equals(tokens[1]) || "events".equals(tokens[1])
              || "series".equals(tokens[1])) {
            return new EditEventCommand(tokens);
          } else if ("calendar".equals(tokens[1])) {
            return new EditCalendarCommand(tokens);
          }
        }
        break;
      case "use":
        if (tokens.length > 1 && "calendar".equals(tokens[1])) {
          return new UseCalendarCommand(tokens);
        }
        break;
      case "print":
        if (tokens.length > 1 && "events".equals(tokens[1])) {
          return new PrintEventsCommand(tokens);
        }
        break;
      case "export":
        if (tokens.length > 1 && "cal".equals(tokens[1])) {
          return new ExportCalendarCommand(tokens);
        }
        break;
      case "show":
        if (tokens.length > 1 && "status".equals(tokens[1])) {
          return new ShowStatusCommand(tokens);
        }
        break;
      case "copy":
        if (tokens.length > 1) {
          if ("event".equals(tokens[1])) {
            return new CopyEventCommand(tokens);
          } else if ("events".equals(tokens[1])) {
            // Check if we have at least 3 tokens and the third is "between"
            boolean isBetweenCommand = tokens.length >= 3 && "between".equals(tokens[2]);
            if (isBetweenCommand) {
              return new CopyEventsRangeCommand(tokens);
            } else {
              return new CopyEventsCommand(tokens);
            }
          }
        }
        break;
      case "exit":
        return new ExitCommand();

      default:
        throw new IllegalArgumentException("Invalid command");
    }

    throw new IllegalArgumentException("Unknown command: " + input);
  }
}