package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.BusyStatus;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import java.time.LocalDateTime;

/**
 * Shows busy/available status at a moment.
 */
public class StatusCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public StatusCommand(CalendarManager manager, ParsedCommand cmd) {
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

    LocalDateTime x = LocalDateTime.parse(cmd.args.get("instant"));
    return current.getService().statusAt(x) == BusyStatus.BUSY
        ? "busy" : "available";
  }
}
