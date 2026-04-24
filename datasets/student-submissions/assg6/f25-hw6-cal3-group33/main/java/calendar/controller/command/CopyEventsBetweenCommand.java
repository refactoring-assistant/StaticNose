package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.calendar.ReadOnlyCalendarAdapter;
import calendar.model.copy.EventCopyInterface;
import calendar.model.copy.EventCopyService;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to copy all events in a date range to another calendar.
 *
 * <p>Copies all events occurring between the source start and end dates
 * from the current calendar to the target calendar starting at the target date,
 * handling any timezone conversions.
 *
 * <p>Note: Implementation pending - method calls will be added when
 * copy functionality is implemented in CalendarManager.
 */
public class CopyEventsBetweenCommand implements Command {

  private final String sourceStartDate;
  private final String sourceEndDate;
  private final String targetCalendarName;
  private final String targetStartDate;

  /**
   * Creates a command to copy events in a date range.
   *
   * @param sourceStartDate    the start date of the range (format: YYYY-MM-DD)
   * @param sourceEndDate      the end date of the range (format: YYYY-MM-DD)
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDate    the start date in target calendar (format: YYYY-MM-DD)
   */
  public CopyEventsBetweenCommand(String sourceStartDate, String sourceEndDate,
                                  String targetCalendarName, String targetStartDate) {
    this.sourceStartDate = sourceStartDate;
    this.sourceEndDate = sourceEndDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    ReadOnlyCalendar sourceCalendar = new ReadOnlyCalendarAdapter(manager.getCurrentCalendar());
    CalendarInterface targetCalendar = manager.getCalendar(targetCalendarName);
    EventCopyInterface copyService = new EventCopyService();
    int copyCount =
        copyService.copyEventsBetween(sourceCalendar, sourceStartDate, sourceEndDate,
            targetCalendar, targetStartDate);
    view.displayMessage(
        "Copied " + copyCount + " events from " + sourceCalendar.getCalendarName() + " to "
            + targetCalendar.getCalendarName());
  }
}