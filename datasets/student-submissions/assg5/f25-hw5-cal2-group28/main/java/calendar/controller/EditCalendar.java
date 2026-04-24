package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.view.ViewConsole;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for edit calendar.
 */
public class EditCalendar implements CommandInterface {

  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   */
  public EditCalendar(String textInput) {
    this.textInput = textInput;
  }

  /**
   * Function to execute commands.
   *
   * @param manager instance of calendar manager
   * @param view    instance of view console
   */
  @Override
  public void execute(CalenderManager manager, ViewConsole view) {
    Pattern pattern = Pattern.compile("edit calendar --name (\\S+) --property (\\S+) (.*)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid command to edit calendar");
      return;
    }

    String property = matcher.group(2).toLowerCase();
    String newProperty = matcher.group(3).trim();
    if (newProperty.startsWith("'") && newProperty.endsWith("'") && newProperty.length() > 1) {
      newProperty = newProperty.substring(1, newProperty.length() - 1);
    }

    try {
      Calendar calendar = manager.fetchCal(matcher.group(1));
      if (property.equals("timezone")) {
        ZoneId zoneId = ZoneId.of(newProperty);
        calendar.setTimeZone(zoneId);
        view.dispSuccess(String.format("Updated timezone to %s", zoneId));
      } else if (property.equals("name")) {
        calendar.setName(newProperty);
        view.dispSuccess("Updated calendar name: " + newProperty);
      } else {
        view.dispError("Invalid property to edit calendar. Must be name or timezone");
      }
    } catch (Exception e) {
      view.dispError("Error editing calendar: " + e.getMessage());
    }
  }
}