package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.CalendarManager;

/**
 * Sets the active calendar context.
 */
public class UseCalendarCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public UseCalendarCommand(CalendarManager manager, ParsedCommand cmd) {
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
    String name = cmd.args.get("name");
    manager.useCalendar(name);
    return "ok";
  }
}