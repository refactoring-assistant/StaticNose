package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.RecurrenceRule;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Creates an all-day repeating event series until a date.
 */
public class CreateAllDaySeriesUntilCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CreateAllDaySeriesUntilCommand(CalendarManager manager,
                                        ParsedCommand cmd) {
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

    Event base = Event.allDay(
        cmd.args.get("subject"),
        LocalDate.parse(cmd.args.get("date")),
        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    RecurrenceRule r = new RecurrenceRule(
        parseDays(cmd.args.get("days")),
        Optional.empty(),
        Optional.of(LocalDate.parse(cmd.args.get("until"))));

    current.getService().createEventSeries(base, r);
    return "ok";
  }

  private EnumSet<DayOfWeek> parseDays(String s) {
    EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
    for (char ch : s.toUpperCase().toCharArray()) {
      switch (ch) {
        case 'M':
          set.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          set.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          set.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          set.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          set.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          set.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          set.add(DayOfWeek.SUNDAY);
          break;
        default:
          break;
      }
    }
    return set;
  }
}