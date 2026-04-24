package calendar.controller.commands;

import calendar.model.IntCalendar;
import calendar.view.IntView;
import java.util.Objects;

/**
 * Handles the "exit" command for exiting the program.
 */
public class ExitCommand implements IntCommand {
  private final IntView out;

  /**
   * Constructs an Exit handler.
   *
   * @param out the output view
   * @throws IllegalArgumentException if out or calendar is null
   */
  public ExitCommand(IntView out) {
    this.out = Objects.requireNonNull(out);
  }

  /**
   * Processes the exit command.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  @Override
  public void go(String input, IntCalendar calendar) {
    out.writeln("Exit");
  }
}
