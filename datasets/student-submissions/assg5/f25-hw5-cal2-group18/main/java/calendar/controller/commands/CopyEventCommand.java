package calendar.controller.commands;

import calendar.model.CalendarSystem;
import java.time.LocalDateTime;

/**
 * Command to copy a single event to another calendar.
 */
public class CopyEventCommand implements SystemCommand {
  private final String eventName;
  private final LocalDateTime sourceStart;
  private final String targetCalendarName;
  private final LocalDateTime targetStart;

  /**
   * Copies the event from source to the target cal.
   *
   * @param eventName name of event
   * @param sourceStart start time of the source event
   * @param targetCalendarName name of target cal
   * @param targetStart start time at the target
   */
  public CopyEventCommand(String eventName, LocalDateTime sourceStart, String targetCalendarName,
                          LocalDateTime targetStart) {
    this.eventName = eventName;
    this.sourceStart = sourceStart;
    this.targetCalendarName = targetCalendarName;
    this.targetStart = targetStart;
  }

  @Override
  public String execute(CalendarSystem system) {
    try {
      system.copyEvent(eventName, sourceStart, targetCalendarName, targetStart);
      return "Event(s) '" + eventName + "' copied to calendar '" + targetCalendarName + "'";
    } catch (IllegalArgumentException | IllegalStateException e) {
      return "Error: " + e.getMessage();
    }
  }
}