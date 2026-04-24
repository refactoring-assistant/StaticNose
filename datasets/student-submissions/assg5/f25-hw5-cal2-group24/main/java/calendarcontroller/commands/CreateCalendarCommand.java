package calendarcontroller.commands;

import calendarcontroller.TextCalendarController;
import calendarview.CalendarView;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.MultiCalendarModel;

/**
 * Command to handle the creation of a new calendar.
 * Expects input like: create calendar --name "Work" --timezone "America/New_York"
 */
public class CreateCalendarCommand extends AbstractAppCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "create calendar --name (.*?) --timezone (.*)", REGEX_FLAGS);

  @Override
  public boolean execute(String inputLine, MultiCalendarModel appModel, CalendarView view,
                         TextCalendarController controller) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    try {
      String name = unquote(matcher.group(1));
      String zoneStr = unquote(matcher.group(2));
      ZoneId zone = ZoneId.of(zoneStr);

      appModel.createCalendar(name, zone);
      view.displayMessage("Calendar '" + name + "' created successfully.");
    } catch (Exception e) {
      view.displayError("Error creating calendar: " + e.getMessage());
    }
    return true;
  }
}