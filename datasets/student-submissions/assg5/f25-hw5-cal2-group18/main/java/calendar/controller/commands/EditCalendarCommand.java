package calendar.controller.commands;

import calendar.model.CalendarSystem;

/**
 * Command to edit a calendar property.
 */
public class EditCalendarCommand implements SystemCommand {
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Edits the calendar property.
   *
   * @param calendarName name of the cal
   * @param property name of the property
   * @param newValue new value of the property
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public String execute(CalendarSystem system) {
    try {
      system.editCalendar(calendarName, property, newValue);
      return "Calendar '" + calendarName + "' " + property + " updated to '" + newValue + "'";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }
}