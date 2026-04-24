package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;

/**
 * Command to edit a calendar's name or timezone.
 */
public class EditCalendarCommand implements CalendarCommand {
  private final String property;
  private final String calendarName;
  private final String newValue;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param property     The property to edit (name, timezone).
   * @param calendarName The name of the calendar to edit.
   * @param newValue     The new value.
   */
  public EditCalendarCommand(String property, String calendarName, String newValue) {
    this.property = property;
    this.calendarName = calendarName;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) throws IllegalArgumentException {
    try {
      switch (property.toLowerCase()) {
        case "name":
          service.editCalendarName(calendarName, newValue);
          view.showMessage("Calendar '" + calendarName + "' renamed to '" + newValue + "'.");
          break;
        case "timezone":
          service.editCalendarTimezone(calendarName, newValue);
          view.showMessage("Calendar '" + calendarName + "' timezone updated to " + newValue);
          break;
        default:
          throw new IllegalArgumentException("Unknown property: " + property
              + ". Can only edit 'name' or 'timezone'.");
      }
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }
}