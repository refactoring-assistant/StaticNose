package calendar.command;

import calendar.model.CalendarInterface;
import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;
import java.util.List;

/**
 * Command to list all available calendars.
 */
public class ListCalendarsCommand implements CalendarCommand {

  @Override
  public void execute(CalendarService service, CalendarView view) {
    List<CalendarInterface> calendars = service.getAllCalendars();
    String activeCalendar = service.getCurrentCalendarName();

    if (calendars.isEmpty()) {
      view.showMessage("No calendars have been created yet.");
      return;
    }

    StringBuilder sb = new StringBuilder("Available Calendars:\n");
    for (CalendarInterface cal : calendars) {
      String prefix = "  ";
      if (cal.getName().equals(activeCalendar)) {
        prefix = "* ";
      }
      sb.append(String.format("%s%s (Timezone: %s)\n",
          prefix, cal.getName(), cal.getTimezone().getId()));
    }
    view.showMessage(sb.toString().trim());
  }
}