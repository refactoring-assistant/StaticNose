package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;

/**
 * Command to create a new calendar with name and timezone.
 */
public class CreateCalendarCommand implements Command {
  private final String calendarName;
  private final String timezone;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param calendarName the unique name for the calendar
   * @param timezone the IANA timezone string
   */
  public CreateCalendarCommand(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    // This command operates on the system level, not individual calendar
    throw new UnsupportedOperationException(
        "Use executeOnSystem() for calendar management commands");
  }

  /**
   * Executes command on the calendar system.
   *
   * @param context the calendar context
   * @param view the view
   */
  public void executeOnSystem(CalendarContext context, CalendarView view) {
    try {
      context.getSystem().createCalendar(calendarName, timezone);
      view.displayMessage("Calendar '" + calendarName + "' created successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}