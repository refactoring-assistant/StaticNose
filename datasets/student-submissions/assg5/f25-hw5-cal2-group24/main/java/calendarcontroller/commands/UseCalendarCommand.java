package calendarcontroller.commands;

import calendarcontroller.TextCalendarController;
import calendarview.CalendarView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.CalendarNameException;
import multicalendarmodel.MultiCalendarModel;

/**
 * Command to set the active calendar context.
 * Expects input like: use calendar --name "Work"
 */
public class UseCalendarCommand extends AbstractAppCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "use calendar --name (.*)", REGEX_FLAGS);

  @Override
  public boolean execute(String inputLine, MultiCalendarModel appModel, CalendarView view,
                         TextCalendarController controller) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }

    String calendarName = unquote(matcher.group(1));
    try {
      controller.setActiveCalendar(calendarName);
    } catch (CalendarNameException e) {
      view.displayError("Error: " + e.getMessage());
    }
    return true;
  }
}