package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Command to copy all events between two dates to a target calendar.
 * Example: copy events between 2024-09-01 and 2024-09-05 --target Work to 2024-09-10
 */
public class CopyEventsBetweenCommand implements CommandInterface {
  private CalendarManagerControllerInterface controller;
  private final ZonedDateTime sourceStart;
  private final ZonedDateTime sourceEnd;
  private final String targetCalendarName;
  private final ZonedDateTime targetStart;

  /**
   * Constructs a command to copy all events from a source time range in the
   * default calendar to a target calendar, starting at a specified date and time.
   *
   * @param sourceStart inclusive start of source range.
   * @param sourceEnd exclusive end of source range.
   * @param targetCalendarName name of destination calendar.
   * @param targetStart start time for first copied event.
   */
  public CopyEventsBetweenCommand(CalendarManagerControllerInterface controller,
                                  ZonedDateTime sourceStart, ZonedDateTime sourceEnd,
                                  String targetCalendarName, ZonedDateTime targetStart) {
    this.controller = controller;
    this.sourceStart = Objects.requireNonNull(sourceStart, "Source start date cannot be null");
    this.sourceEnd = Objects.requireNonNull(sourceEnd, "Source end date cannot be null");
    this.targetCalendarName = Objects.requireNonNull(targetCalendarName,
        "Target calendar cannot be null");
    this.targetStart = Objects.requireNonNull(targetStart, "Target start date cannot be null");
  }

  @Override
  public String execute() {
    try {
      controller.copyEventsBetween(sourceStart, sourceEnd, targetCalendarName, targetStart);
      return "Events from " + sourceStart.toLocalDate() + " to " + sourceEnd.toLocalDate()
          + " copied to calendar '" + targetCalendarName + "' starting "
          + targetStart.toLocalDate();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Copy events between " + sourceStart.toLocalDate() + " and " + sourceEnd.toLocalDate()
        + " to '" + targetCalendarName + "' starting " + targetStart.toLocalDate();
  }
}
