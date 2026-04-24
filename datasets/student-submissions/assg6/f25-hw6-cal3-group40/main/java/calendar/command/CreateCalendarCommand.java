package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;

/**
 * Command to create a new calendar with a name and timezone.
 */
public class CreateCalendarCommand implements CalendarCommand {
  private final String name;
  private final String timezone;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param name     The calendar name.
   * @param timezone The timezone ID.
   */
  public CreateCalendarCommand(String name, String timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) {
    try {
      service.createCalendar(name, timezone);
      view.showMessage("Calendar '" + name + "' created successfully with timezone " + timezone);
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }
}