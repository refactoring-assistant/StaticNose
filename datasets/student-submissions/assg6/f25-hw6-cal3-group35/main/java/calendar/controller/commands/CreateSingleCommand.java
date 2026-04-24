package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Creates a single timed event in the current calendar.
 */
public class CreateSingleCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CreateSingleCommand(CalendarManager manager, ParsedCommand cmd) {
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

    Event e = new Event(
        cmd.args.get("subject"),
        LocalDateTime.parse(cmd.args.get("start")),
        LocalDateTime.parse(cmd.args.get("end")),
        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    current.getService().createSingleEvent(e);
    return "ok";
  }
}