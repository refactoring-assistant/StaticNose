package calendar.controller.command;

import calendar.exceptions.InvalidDateTimeException;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to edit a calendar's properties (name or timezone).
 *
 * <p>Supports editing two properties:
 * - "name": Renames the calendar
 * - "timezone": Changes the calendar's timezone
 */
public class EditCalendarCommand implements Command {

  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Creates a command to edit a calendar property.
   *
   * @param calendarName the name of the calendar to edit
   * @param property the property to edit ("name" or "timezone")
   * @param newValue the new value for the property
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    if (property.equals("name")) {
      manager.editCalendarName(calendarName, newValue);
      view.displayMessage("Renamed calendar from '" + calendarName + "' to '" + newValue + "'");
    } else if (property.equals("timezone")) {
      try {
        manager.editCalendarTimezone(calendarName, newValue);
        view.displayMessage("Updated timezone for calendar '" + calendarName + "' to " + newValue);
      } catch (InvalidDateTimeException e) {
        view.displayError(e.getMessage());
      }
    } else {
      throw new IllegalArgumentException("Invalid property: " + property
          + ". Must be 'name' or 'timezone'");
    }
  }
}