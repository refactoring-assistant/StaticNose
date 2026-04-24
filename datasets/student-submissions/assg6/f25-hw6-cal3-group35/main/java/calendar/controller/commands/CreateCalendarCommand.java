package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.CalendarManager;
import java.time.ZoneId;

/**
 * Creates a new calendar with a name and timezone.
 */
public class CreateCalendarCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CreateCalendarCommand(CalendarManager manager, ParsedCommand cmd) {
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
    String timezoneStr = cmd.args.get("timezone");

    try {
      ZoneId timezone = ZoneId.of(timezoneStr);
      manager.createCalendar(name, timezone);
      return "ok";
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create calendar: "
          + e.getMessage());
    }
  }
}