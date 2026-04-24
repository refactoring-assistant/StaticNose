package calendar.command.calendar;

import calendar.command.Command;
import calendar.model.CalendarManager;
import calendar.view.CalendarTextView;
import java.time.ZoneId;

/**
 * Command to create a new calendar.
 */
public class CreateCalendar implements Command {

  private final String name;
  private final String timezone;

  /**
   * Constructs a new CreateCalendar command.
   *
   * @param name     the name for the calendar
   * @param timezone the timezone for the calendar
   */
  public CreateCalendar(String name, String timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarManager manager, CalendarTextView view) {
    try {
      ZoneId zoneId = ZoneId.of(timezone);
      manager.createCalendar(name, zoneId);
      view.printMessage("Calendar '" + name + "' created successfully.");
    } catch (Exception e) {
      view.printMessage("Error: " + e.getMessage());
    }
  }
}
