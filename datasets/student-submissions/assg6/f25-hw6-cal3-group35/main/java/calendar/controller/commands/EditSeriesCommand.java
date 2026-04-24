package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import java.time.LocalDateTime;

/**
 * Edits all instances in a series.
 */
public class EditSeriesCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public EditSeriesCommand(CalendarManager manager, ParsedCommand cmd) {
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
    Calendar current = manager.getCurrentCalendar()
        .orElseThrow(() -> new IllegalStateException("No calendar in use"));

    current.getService().editSeries(
        cmd.args.get("subject"),
        LocalDateTime.parse(cmd.args.get("start")),
        cmd.args.get("property"),
        cmd.args.get("value"));
    return "ok";
  }
}