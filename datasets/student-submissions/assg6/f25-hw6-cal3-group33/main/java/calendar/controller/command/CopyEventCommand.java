package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.calendar.ReadOnlyCalendarAdapter;
import calendar.model.copy.EventCopyInterface;
import calendar.model.copy.EventCopyService;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to copy a single event to another calendar at a different datetime.
 *
 * <p>Copies the specified event from the current calendar to the target calendar,
 * adjusting the datetime and handling any timezone conversions.
 *
 * <p>Note: Implementation pending - method calls will be added when
 * copy functionality is implemented in CalendarManager.
 */
public class CopyEventCommand implements Command {

  private final String subject;
  private final String sourceDateTime;
  private final String targetCalendarName;
  private final String targetDateTime;

  /**
   * Creates a command to copy a single event.
   *
   * @param subject            the subject of the event to copy
   * @param sourceDateTime     the datetime of the event in source calendar
   *                           (format: YYYY-MM-DDThh:mm)
   * @param targetCalendarName the name of the target calendar
   * @param targetDateTime     the datetime for the copied event (format: YYYY-MM-DDThh:mm)
   */
  public CopyEventCommand(String subject, String sourceDateTime,
                          String targetCalendarName, String targetDateTime) {
    this.subject = subject;
    this.sourceDateTime = sourceDateTime;
    this.targetCalendarName = targetCalendarName;
    this.targetDateTime = targetDateTime;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    ReadOnlyCalendar sourceCalendar = new ReadOnlyCalendarAdapter(manager.getCurrentCalendar());
    CalendarInterface targetCalendar = manager.getCalendar(targetCalendarName);
    EventCopyInterface copyService = new EventCopyService();
    int copyCount = copyService.copyEvents(sourceCalendar, subject, sourceDateTime, targetCalendar,
        targetDateTime);
    view.displayMessage(
        "Copied " + copyCount + " events from " + sourceCalendar.getCalendarName() + " to "
            + targetCalendar.getCalendarName());
  }
}