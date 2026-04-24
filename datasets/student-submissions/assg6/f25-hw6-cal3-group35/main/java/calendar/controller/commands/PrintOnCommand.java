package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prints all events on a given date.
 */
public class PrintOnCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public PrintOnCommand(CalendarManager manager, ParsedCommand cmd) {
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

    LocalDate d = LocalDate.parse(cmd.args.get("date"));
    List<Event> list = current.getService().eventsOn(d);
    if (list.isEmpty()) {
      return "(no events)";
    }
    DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
    return list.stream()
        .map(e -> {
          String loc = e.getLocation().orElse("");
          String at = loc.isBlank() ? "" : " @ " + loc;
          return e.getSubject() + " " + e.getStart().toLocalTime().format(tf)
              + "-" + e.getEnd().toLocalTime().format(tf) + at;
        })
        .collect(Collectors.joining(System.lineSeparator()));
  }
}
