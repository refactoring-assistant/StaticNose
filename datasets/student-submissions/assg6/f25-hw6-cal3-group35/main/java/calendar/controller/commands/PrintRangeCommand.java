package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prints all events overlapping a time range.
 */
public class PrintRangeCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public PrintRangeCommand(CalendarManager manager, ParsedCommand cmd) {
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

    LocalDateTime f = LocalDateTime.parse(cmd.args.get("from"));
    LocalDateTime t = LocalDateTime.parse(cmd.args.get("to"));
    List<Event> list = current.getService().eventsOverlapping(f, t);
    if (list.isEmpty()) {
      return "(no events)";
    }
    return list.stream()
        .map(Event::toString)
        .collect(Collectors.joining(System.lineSeparator()));
  }
}