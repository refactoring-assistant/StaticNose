package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.util.EventCopyUtil;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Command to copy all events on a specific date to another calendar.
 */
public class CopyEventsOnDateCommand implements Command {
  private final LocalDate sourceDate;
  private final String targetCalendarName;
  private final LocalDate targetDate;

  /**
   * Constructs a CopyEventsOnDateCommand.
   *
   * @param sourceDate the date to copy events from
   * @param targetCalendarName the target calendar
   * @param targetDate the date to copy events to
   */
  public CopyEventsOnDateCommand(LocalDate sourceDate,
                                 String targetCalendarName,
                                 LocalDate targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    throw new UnsupportedOperationException(
        "Use executeOnSystem() for copy commands");
  }

  /**
   * Executes copy on the calendar system.
   *
   * @param context the calendar context
   * @param view the view
   */
  public void executeOnSystem(CalendarContext context, CalendarView view) {
    try {
      CalendarModel sourceCalendar = context.getCurrentCalendar();
      String sourceCalendarName = context.getCurrentCalendarName();
      CalendarModel targetCalendar = context.getSystem().getCalendar(targetCalendarName);

      ZoneId sourceTimezone = context.getSystem().getCalendarTimezone(sourceCalendarName);
      ZoneId targetTimezone = context.getSystem().getCalendarTimezone(targetCalendarName);

      EventCopyUtil.copyEventsOnDate(sourceCalendar, targetCalendar,
          sourceDate, targetDate, sourceTimezone, targetTimezone);

      view.displayMessage("Events copied successfully.");

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}