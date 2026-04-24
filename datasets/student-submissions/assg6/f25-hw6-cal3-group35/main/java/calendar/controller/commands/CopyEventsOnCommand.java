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
 * Copies all events on a specific date to target calendar.
 *
 * <p>Design Change (Assignment 5): Timezone conversion now happens during
 * the copy operation, not during export. Times are converted to maintain
 * the same absolute time.
 *
 * @author MH
 * @version 2.0
 */
public class CopyEventsOnCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CopyEventsOnCommand(CalendarManager manager, ParsedCommand cmd) {
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

    LocalDate sourceDate = LocalDate.parse(cmd.args.get("date"));
    String targetCalName = cmd.args.get("target");
    LocalDate targetDate = LocalDate.parse(cmd.args.get("to"));

    Calendar targetCal = manager.getCalendar(targetCalName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Target calendar '" + targetCalName + "' not found"));

    List<Event> sourceEvents = current.getService().eventsOn(sourceDate);

    for (Event source : sourceEvents) {
      LocalDateTime sourceStart = source.getStart();
      LocalDateTime sourceEnd = source.getEnd();

      long duration = ChronoUnit.MINUTES.between(sourceStart, sourceEnd);

      LocalDateTime targetStart = targetDate.atTime(sourceStart.toLocalTime());

      LocalDateTime adjustedStart = convertTime(targetStart,
          current.getTimezone(), targetCal.getTimezone());
      LocalDateTime adjustedEnd = adjustedStart.plusMinutes(duration);

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
        // Duplicate event - skip and continue
      }
    }

    return "ok";
  }

  /**
   * Converts a time from source timezone to target timezone.
   *
   * @param time the local time in the source timezone
   * @param sourceZone the source timezone
   * @param targetZone the target timezone
   * @return the equivalent local time in the target timezone
   */
  private LocalDateTime convertTime(LocalDateTime time,
                                    java.time.ZoneId sourceZone,
                                    java.time.ZoneId targetZone) {
    ZonedDateTime sourceZoned = time.atZone(sourceZone);
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);
    return targetZoned.toLocalDateTime();
  }
}