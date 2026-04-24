package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Copies a single event from current calendar to target calendar.
 */
public class CopyEventCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public CopyEventCommand(CalendarManager manager, ParsedCommand cmd) {
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

    String eventName = cmd.args.get("subject");
    LocalDateTime sourceStart = LocalDateTime.parse(cmd.args.get("start"));
    String targetCalName = cmd.args.get("target");
    LocalDateTime targetStart = LocalDateTime.parse(cmd.args.get("to"));

    Calendar targetCal = manager.getCalendar(targetCalName)
        .orElseThrow(() -> new IllegalArgumentException(
            "Target calendar '" + targetCalName + "' not found"));

    Event source = current.getService().findBySubjectAndStart(eventName,
            sourceStart)
        .orElseThrow(() -> new IllegalStateException("Event not found"));

    long duration = ChronoUnit.MINUTES.between(source.getStart(),
        source.getEnd());

    LocalDateTime adjustedEnd = targetStart.plusMinutes(duration);

    Event copy = new Event(
        source.getSubject(),
        targetStart,
        adjustedEnd,
        source.getDescription(),
        source.getLocation(),
        source.getStatus(),
        Optional.empty()
    );

    targetCal.getService().createSingleEvent(copy);
    return "ok";
  }
}