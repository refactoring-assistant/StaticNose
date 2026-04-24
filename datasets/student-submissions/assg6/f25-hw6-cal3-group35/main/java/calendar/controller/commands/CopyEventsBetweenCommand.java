package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Copies all events in a date range to target calendar.
 */
public class CopyEventsBetweenCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CopyEventsBetweenCommand(CalendarManager manager, ParsedCommand cmd) {
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

    LocalDate startDate = LocalDate.parse(cmd.args.get("start"));
    LocalDate endDate = LocalDate.parse(cmd.args.get("end"));
    String targetCalName = cmd.args.get("target");
    LocalDate targetStartDate = LocalDate.parse(cmd.args.get("to"));

    Calendar targetCal = manager.getCalendar(targetCalName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Target calendar '" + targetCalName + "' not found"));

    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);
    List<Event> sourceEvents = current.getService().eventsOverlapping(
        rangeStart, rangeEnd);

    long dayOffset = ChronoUnit.DAYS.between(startDate, targetStartDate);

    for (Event source : sourceEvents) {
      LocalDateTime newStart = source.getStart().plusDays(dayOffset);
      LocalDateTime newEnd = source.getEnd().plusDays(dayOffset);

      LocalDateTime adjustedStart = convertTime(newStart,
          current.getTimezone(), targetCal.getTimezone());
      LocalDateTime adjustedEnd = convertTime(newEnd,
          current.getTimezone(), targetCal.getTimezone());

      Event copy = new Event(
          source.getSubject(),
          adjustedStart,
          adjustedEnd,
          source.getDescription(),
          source.getLocation(),
          source.getStatus(),
          source.getSeriesId()
      );

      try {
        targetCal.getService().createSingleEvent(copy);
      } catch (IllegalStateException e) {
        // ok
      }
    }

    return "ok";
  }

  private LocalDateTime convertTime(LocalDateTime time,
                                    java.time.ZoneId sourceZone, java.time.ZoneId targetZone) {
    ZonedDateTime sourceZoned = time.atZone(sourceZone);
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);
    return targetZoned.toLocalDateTime();
  }
}