package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.util.EventCopyUtil;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Command to copy all events between two dates to another calendar.
 */
public class CopyEventsBetweenCommand implements Command {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final String targetCalendarName;
  private final LocalDate targetStartDate;

  /**
   * Constructs a CopyEventsBetweenCommand.
   *
   * @param startDate start of range (inclusive)
   * @param endDate end of range (inclusive)
   * @param targetCalendarName target calendar
   * @param targetStartDate start date in target
   */
  public CopyEventsBetweenCommand(LocalDate startDate,
                                  LocalDate endDate,
                                  String targetCalendarName,
                                  LocalDate targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
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

      EventCopyUtil.copyEventsBetween(sourceCalendar, targetCalendar,
          startDate, endDate, targetStartDate, sourceTimezone, targetTimezone);

      view.displayMessage("Events copied successfully.");

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}