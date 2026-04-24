package calendar.controller.calendar;

import calendar.controller.Command;
import calendar.model.CalendarApplication;
import calendar.view.CalendarView;
import java.time.DateTimeException;

/**
 * Command class for creating calendar.
 * Implements the Command interface and calls the
 * corresponding method on the CalendarApplication model.
 */
public class CreateCalendarCommand implements Command {

  private final String calendarName;
  private final String timezone;

  /**
   * Constructs a command to create a new calendar.
   *
   * @param calendarName The unique name for the new calendar.
   * @param timezone     The IANA timezone string.
   */
  public CreateCalendarCommand(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      model.createCalendar(calendarName, timezone);
      view.displaySuccess("Calendar '" + calendarName + "' created successfully.");
    } catch (IllegalArgumentException | DateTimeException e) {
      view.displayError(e.getMessage());
    }
  }
}