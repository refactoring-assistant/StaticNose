package calendar.controller.calendar;

import calendar.controller.Command;
import calendar.model.CalendarApplication;
import calendar.view.CalendarView;
import java.time.DateTimeException;

/**
 * Command class for editing calendar.
 * Implements the Command interface and calls the
 * corresponding method on the CalendarApplication model.
 */
public class EditCalendarCommand implements Command {

  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs a command to edit an existing calendar.
   *
   * @param calendarName The name of the calendar to edit.
   * @param property The property to change ("name" or "timezone").
   * @param newValue The new value for that property.
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      model.editCalendar(calendarName, property, newValue);
      view.displaySuccess("Calendar '" + calendarName + "' updated successfully.");
    } catch (IllegalArgumentException | DateTimeException e) {
      view.displayError(e.getMessage());
    }
  }
}