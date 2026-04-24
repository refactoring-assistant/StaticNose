package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.model.IntCalendar;
import calendar.view.IntView;
import java.util.Objects;

/**
 * Handles the "export" command for exporting the calendar.
 */
public class ExportCommand implements IntCommand {
  private final IntView out;

  /**
   * Constructs an Export handler.
   *
   * @param out the output view
   * @throws IllegalArgumentException if out or calendar is null
   */
  public ExportCommand(IntView out) {
    this.out = Objects.requireNonNull(out);
  }

  /**
   * Processes the export command.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  public void go(String input, IntCalendar calendar) {
    if (calendar == null) {
      out.writeln("Must set a calendar first");
      return;
    }

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    String fileName = input.trim();

    try {
      String absoluteFilePath = calendar.export(fileName);
      out.writeln("Calendar exported to: " + absoluteFilePath);
    } catch (RuntimeException e) {
      out.writeln(e.getMessage());
    }
  }
}

