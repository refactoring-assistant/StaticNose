package calendar.controller.command;

import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to create a new calendar with a specified name and timezone.
 *
 * <p>Creates an empty calendar that can be used to store events.
 * The calendar will use the specified IANA timezone (e.g., "America/New_York").
 */
public class CreateCalendarCommand implements Command {

  private final String calendarName;
  private final String timezone;

  /**
   * Creates a command to create a new calendar.
   *
   * @param calendarName the name for the new calendar
   * @param timezone the IANA timezone identifier (e.g., "America/New_York")
   */
  public CreateCalendarCommand(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    manager.createCalendar(calendarName, timezone);
    view.displayMessage("Created calendar: " + calendarName + " (" + timezone + ")");
  }
}