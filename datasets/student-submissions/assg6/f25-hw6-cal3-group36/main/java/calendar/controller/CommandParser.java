package calendar.controller;

import calendar.controller.commands.CopyCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditCommand;
import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.HelpCommand;
import calendar.controller.commands.PrintCommand;
import calendar.controller.commands.ShowCommand;
import calendar.controller.commands.UseCalendarCommand;

/**
 * Parsing the user input, that produces the suitable command instance.
 */
public class CommandParser {

  /**
   * Parsing the command line and returning the command object.
   *
   * @param input command line input by user.
   * @return command instance.
   * @throws IllegalArgumentException Invalid command.
   */
  public Command parse(String input) {
    String normalized = input.trim().toLowerCase();
    if (normalized.startsWith("create calendar")) {
      return new CreateCalendarCommand(input);
    } else if (normalized.startsWith("edit calendar")) {
      return new EditCalendarCommand(input);
    } else if (normalized.startsWith("use calendar")) {
      return new UseCalendarCommand(input);
    } else if (normalized.startsWith("create event")) {
      return new CreateCommand(input);
    } else if (normalized.startsWith("edit events") || normalized.startsWith("edit event")
        || normalized.startsWith("edit series")) {
      return new EditCommand(input);
    } else if (normalized.startsWith("print events")) {
      return new PrintCommand(input);
    } else if (normalized.startsWith("show status")) {
      return new ShowCommand(input);
    } else if (normalized.startsWith("export")) {
      return new ExportCommand(input);
    } else if (normalized.startsWith("copy")) {
      return new CopyCommand(input);
    } else if (normalized.startsWith("help")) {
      return new HelpCommand();
    } else if (normalized.startsWith("exit")) {
      return new ExitCommand();
    }
    throw new IllegalArgumentException("Invalid command: '" + input + "'");
  }
}
