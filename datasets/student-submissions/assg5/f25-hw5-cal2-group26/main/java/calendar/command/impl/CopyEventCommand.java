package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.time.ZonedDateTime;

/**
 * Command to copy a single event from the active calendar to a target calendar.
 * Example:
 * copy event "Meeting" on 2024-09-05T14:00 --target Work to 2024-09-12T14:00
 */
public class CopyEventCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final String eventName;
  private final ZonedDateTime sourceStart;
  private final String targetCalendarName;
  private final ZonedDateTime targetStart;

  /**
   * Constructs a command to copy a single event.
   *
   * @param controller is the controller that the method is in
   * @param eventName name of the event to copy
   * @param sourceStart start time of the event in the source calendar
   * @param targetCalendarName name of the target calendar
   * @param targetStart start time in the target calendar
   */
  public CopyEventCommand(CalendarManagerControllerInterface controller, String eventName,
                          ZonedDateTime sourceStart, String targetCalendarName,
                          ZonedDateTime targetStart) {
    this.controller = controller;
    this.eventName = eventName;
    this.sourceStart = sourceStart;
    this.targetCalendarName = targetCalendarName;
    this.targetStart = targetStart;
  }

  @Override
  public String execute() {
    try {
      controller.copyEvent(eventName, sourceStart, targetCalendarName, targetStart);
      return "Event '" + eventName + "' copied to calendar '" + targetCalendarName + "'";
    } catch (RuntimeException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Copy event '" + eventName + "' from " + sourceStart
        + " to '" + targetCalendarName + "' at " + targetStart;
  }
}