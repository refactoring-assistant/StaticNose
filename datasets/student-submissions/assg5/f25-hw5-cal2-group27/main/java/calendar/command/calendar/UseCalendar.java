package calendar.command.calendar;

import calendar.command.Command;
import calendar.model.CalendarManager;
import calendar.view.CalendarTextView;

/**
 * Command to switch the active calendar.
 */
public class UseCalendar implements Command {

  private final String name;

  /**
   * Constructs a new UseCalendar command.
   *
   * @param name the name of the calendar
   */
  public UseCalendar(String name) {
    this.name = name;
  }

  @Override
  public void execute(CalendarManager manager, CalendarTextView view) {
    try {
      manager.useCalendar(name);
      view.printMessage("Now using calendar '" + name + "'.");
    } catch (IllegalArgumentException e) {
      view.printMessage("Error: " + e.getMessage());
    }
  }
}
