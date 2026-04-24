package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Creates a single all-day event.
 */
public class CreateAllDaySingleCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CreateAllDaySingleCommand(CalendarManager manager, ParsedCommand cmd) {
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

    Event e = Event.allDay(
        cmd.args.get("subject"),
        LocalDate.parse(cmd.args.get("date")),
        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    current.getService().createSingleEvent(e);
    return "ok";
  }
}