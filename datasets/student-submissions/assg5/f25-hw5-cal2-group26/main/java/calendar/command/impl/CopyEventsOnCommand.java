package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Command to copy all events from one day to another calendar.
 * Example: copy events on 2024-09-05 --target Work to 2024-09-12
 */
public class CopyEventsOnCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final ZonedDateTime sourceDate;
  private final String targetCalendarName;
  private final ZonedDateTime targetDate;

  /**
   * Creates a command to copy all events from sourceDate in the default calendar
   * to targetCalendarName on targetDate.
   */
  public CopyEventsOnCommand(CalendarManagerControllerInterface controller,
                             ZonedDateTime sourceDate,
                             String targetCalendarName, ZonedDateTime targetDate) {
    this.controller = controller;
    this.sourceDate = Objects.requireNonNull(sourceDate, "Source date cannot be null");
    this.targetCalendarName = Objects.requireNonNull(targetCalendarName,
        "Target calendar cannot be null");
    this.targetDate = Objects.requireNonNull(targetDate, "Target date cannot be null");
  }

  @Override
  public String execute() {
    try {
      controller.copyEventsOn(sourceDate, targetCalendarName, targetDate);
      return "All events on " + sourceDate.toLocalDate() + " copied to calendar '"
          + targetCalendarName + "' on " + targetDate.toLocalDate();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Copy all events on " + sourceDate.toLocalDate()
        + " to '" + targetCalendarName + "' on " + targetDate.toLocalDate();
  }
}
