package calendar.controller;

import calendar.controller.commands.CommandConstants;
import calendar.controller.commands.CopyCommand;
import calendar.controller.commands.CreateCommand;
import calendar.controller.commands.EditCommand;
import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.IntCommand;
import calendar.controller.commands.PrintCommand;
import calendar.controller.commands.ShowCommand;
import calendar.controller.commands.UseCommand;
import calendar.model.IntCalendar;
import calendar.model.IntCalendarManager;
import calendar.view.IntView;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Main controller for the calendar application.
 * Handles command parsing and routing to appropriate command handlers.
 */
public class Controller implements IntController {
  private final Readable in;
  private final IntView out;
  private final Map<String, IntCommand> commands;
  private final IntCalendarManager calendarManager;

  /**
   * Constructs a Controller with the given input and output.
   *
   * @param in              the input stream
   * @param out             the output view
   * @param calendarManager the calendarManager to be used by the commands
   * @throws IllegalArgumentException if in or out is null
   */
  public Controller(Readable in, IntView out,
                    IntCalendarManager calendarManager) {
    this.in = Objects.requireNonNull(in);
    this.out = Objects.requireNonNull(out);
    this.calendarManager = Objects.requireNonNull(calendarManager);
    this.commands = new HashMap<>();
    this.commands.put(CommandConstants.CREATE, new CreateCommand(out, calendarManager));
    this.commands.put(CommandConstants.EDIT, new EditCommand(out, calendarManager));
    this.commands.put(CommandConstants.PRINT, new PrintCommand(out));
    this.commands.put(CommandConstants.EXPORT, new ExportCommand(out));
    this.commands.put(CommandConstants.SHOW, new ShowCommand(out));
    this.commands.put(CommandConstants.EXIT, new ExitCommand(out));
    this.commands.put(CommandConstants.USE, new UseCommand(out, calendarManager));
    this.commands.put(CommandConstants.COPY, new CopyCommand(out, calendarManager));
  }

  /**
   * Starts the controller and processes commands from input looking for
   * matching commands.
   */
  @Override
  public void go() {
    Scanner scanner = new Scanner(in);
    while (scanner.hasNext()) {
      String input = scanner.nextLine();

      int firstSpace = Parser.getFirstSpaceIndex(input);
      String currentWord = input.substring(0, firstSpace);

      IntCommand command = commands.getOrDefault(currentWord, null);
      if (command == null) {
        out.writeln("Command: \"" + currentWord + "\" is not a "
            + "valid input");
      } else if (currentWord.equalsIgnoreCase(CommandConstants.EXIT)) {
        command.go(input, null);
        return;
      } else {
        try {
          IntCalendar calendar = calendarManager.getActiveCalendar();
          command.go(input.substring(firstSpace + 1), calendar);
        } catch (IllegalStateException e) {
          command.go(input.substring(firstSpace + 1), null);
        }
      }
    }
    out.writeln("Input ended without \"exit\" command. Terminating.");
  }
}

