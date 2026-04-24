package calendar.controller;

import calendar.model.CalenderManager;
import calendar.view.ViewConsole;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to create a new calendar.
 * */
public class CreateCalendar implements CommandInterface {
  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   * */
  public CreateCalendar(String textInput) {
    this.textInput = textInput;
  }

  /**
   * Function to execute commands.
   *
   * @param manager instance of calendar manager
   * @param view instance of view console
   * */
  @Override
  public void execute(CalenderManager manager, ViewConsole view) {
    Pattern pattern = Pattern.compile("create calendar --name (\\S+) --timezone (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid calendar creation command");
      return;
    }

    String calName = matcher.group(1);
    String timezone = matcher.group(2);

    try {
      ZoneId zoneId = ZoneId.of(timezone);
      manager.createCalender(calName, zoneId);
      view.dispSuccess(String.format("Successfully created calendar '%s' with timezone '%s'",
          calName, timezone));
    } catch (Exception e) {
      view.dispError("Error creating calendar: " + e.getMessage());
    }
  }
}