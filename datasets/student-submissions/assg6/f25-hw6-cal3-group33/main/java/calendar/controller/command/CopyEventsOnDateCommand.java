package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.calendar.ReadOnlyCalendarAdapter;
import calendar.model.copy.EventCopyInterface;
import calendar.model.copy.EventCopyService;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to copy all events on a specific date to another calendar.
 *
 * <p>Copies all events occurring on the source date from the current calendar
 * to the target calendar on the target date, handling any timezone conversions.
 *
 * <p>Note: Implementation pending - method calls will be added when
 * copy functionality is implemented in CalendarManager.
 */
public class CopyEventsOnDateCommand implements Command {

  private final String sourceDate;
  private final String targetCalendarName;
  private final String targetDate;

  /**
   * Creates a command to copy all events on a date.
   *
   * @param sourceDate         the date to copy events from (format: YYYY-MM-DD)
   * @param targetCalendarName the name of the target calendar
   * @param targetDate         the date to copy events to (format: YYYY-MM-DD)
   */
  public CopyEventsOnDateCommand(String sourceDate, String targetCalendarName,
                                 String targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    ReadOnlyCalendar sourceCalendar = new ReadOnlyCalendarAdapter(manager.getCurrentCalendar());
    CalendarInterface targetCalendar = manager.getCalendar(targetCalendarName);
    EventCopyInterface copyService = new EventCopyService();
    int copyCount = copyService.copyEvents(sourceCalendar, sourceDate, targetCalendar,
        targetDate);
    view.displayMessage(
        "Copied " + copyCount + " events from " + sourceCalendar.getCalendarName() + " to "
            + targetCalendar.getCalendarName());
  }
}