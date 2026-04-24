package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.CalendarManager;

/**
 * Edits a calendar property (name or timezone).
 */
public class EditCalendarCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public EditCalendarCommand(CalendarManager manager, ParsedCommand cmd) {
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
    String property = cmd.args.get("property");
    String value = cmd.args.get("value");

    manager.editCalendar(name, property, value);
    return "ok";
  }
}