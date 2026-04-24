package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.controller.Token;
import calendar.model.Date;
import calendar.model.IntCalendar;
import calendar.model.Time;
import calendar.view.IntView;
import java.util.Objects;

/**
 * Handles the "show" command for displaying status information.
 */
public class ShowCommand implements IntCommand {
  private final IntView out;

  /**
   * Constructs a Show handler.
   *
   * @param out the output view
   * @throws IllegalArgumentException if out or calendar is null
   */
  public ShowCommand(IntView out) {
    this.out = Objects.requireNonNull(out);
  }

  /**
   * Processes the show command.
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

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    Date date = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    Time time = Parser.extractTime(input);

    if (calendar.isBusy(date, time)) {
      out.writeln("Busy");
    } else {
      out.writeln("Available");
    }
  }
}

