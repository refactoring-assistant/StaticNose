package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalView;
import calendar.view.textbased.CalendarView;

/**
 * Command to switch to a different calendar.
 */
public class UseCalendarCommand implements CalendarCommand {
  private final String name;

  /**
   * Constructs a UseCalendarCommand.
   *
   * @param name The calendar name to switch to.
   */
  public UseCalendarCommand(String name) {
    this.name = name;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) {
    try {
      service.useCalendar(name);

      if (view instanceof CalView) {
        ((CalView) view).setCurrentCalendarContext(name);
      }

      view.showMessage("Now using calendar: " + name
          + " (Timezone: " + service.getCurrentCalendarTimezone() + ")");
    } catch (IllegalArgumentException e) {
      view.showError(e.getMessage());
    }
  }
}