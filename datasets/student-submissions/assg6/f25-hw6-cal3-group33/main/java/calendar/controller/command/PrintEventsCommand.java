package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.calendar.ReadOnlyCalendarAdapter;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to print all events scheduled on a specific date.
 *
 * <p>Displays all events that occur on the given date, including:
 * - Events that start on that date
 * - Multi-day events that span across that date
 */
public class PrintEventsCommand implements Command {

  private final String date;

  /**
   * Creates a command to print events on a specific date.
   *
   * @param date the date to query (format: YYYY-MM-DD)
   */
  public PrintEventsCommand(String date) {
    this.date = date;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();
    ReadOnlyCalendar readOnlyCalendar = new ReadOnlyCalendarAdapter(calendar);
    view.displayEvents(readOnlyCalendar, date);
  }
}