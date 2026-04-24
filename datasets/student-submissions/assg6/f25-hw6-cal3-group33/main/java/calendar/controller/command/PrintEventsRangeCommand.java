package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.calendar.ReadOnlyCalendarAdapter;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to print all events within a date range.
 *
 * <p>Displays all events that occur between the start and end datetimes (inclusive).
 * Events that partially or fully overlap the range are included.
 */
public class PrintEventsRangeCommand implements Command {

  private final String startDateTime;
  private final String endDateTime;

  /**
   * Creates a command to print events in a date range.
   *
   * @param startDateTime the start datetime (format: YYYY-MM-DDThh:mm) - inclusive
   * @param endDateTime the end datetime (format: YYYY-MM-DDThh:mm) - inclusive
   */
  public PrintEventsRangeCommand(String startDateTime, String endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();
    ReadOnlyCalendar readOnlyCalendar = new ReadOnlyCalendarAdapter(calendar);
    view.displayEventsInRange(readOnlyCalendar, startDateTime, endDateTime);
  }
}