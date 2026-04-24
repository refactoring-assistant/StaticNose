package calendarcontroller.commands;

import calendarcontroller.TextCalendarController;
import calendarview.CalendarView;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.MultiCalendarModel;

/**
 * Command to edit properties of an existing calendar.
 * Expects input like: edit calendar --name "Work" --property "name" "New Name"
 * Or: edit calendar --name "Work" --property "timezone" "Europe/London"
 */
public class EditCalendarCommand extends AbstractAppCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "edit calendar --name (.*?) --property (.*?) (.*)", REGEX_FLAGS);

  @Override
  public boolean execute(String inputLine, MultiCalendarModel appModel, CalendarView view,
                         TextCalendarController controller) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    try {
      String calName = unquote(matcher.group(1));
      String property = unquote(matcher.group(2)).toLowerCase();
      String value = unquote(matcher.group(3));

      switch (property) {
        case "name":
          appModel.renameCalendar(calName, value);
          view.displayMessage("Calendar '" + calName + "' renamed to '" + value + "'.");
          if (controller.getActiveCalendar() != null
              && controller.getActiveCalendarName().equals(calName)) {
            controller.setActiveCalendar(value);
          }
          break;
        case "timezone":
          ZoneId zone = ZoneId.of(value);
          appModel.changeCalendarZone(calName, zone);
          view.displayMessage("Calendar '" + calName + "' timezone set to '" + value + "'.");
          break;
        default:
          throw new IllegalArgumentException("Unknown property: " + property);
      }
    } catch (Exception e) {
      view.displayError("Error editing calendar: " + e.getMessage());
    }
    return true;
  }
}