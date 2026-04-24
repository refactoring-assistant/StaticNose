package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.CalendarManager;

/**
 * Signals the application to exit.
 */
public class ExitCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public ExitCommand(CalendarManager manager, ParsedCommand cmd) {
    this.manager = manager;
    this.cmd = cmd;
  }

  /**
   * Executes the command.
   *
   * @return result string
   */
  @Override
  public String execute() {
    return "exit";
  }
}