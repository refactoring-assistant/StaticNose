package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;

/**
 * Command to edit calendar properties (name or timezone).
 */
public class EditCalendarCommand implements Command {
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param calendarName the calendar to edit
   * @param property the property to change
   * @param newValue the new value
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
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
      context.getSystem().editCalendar(calendarName, property, newValue);
      view.displayMessage("Calendar property '" + property + "' updated successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}