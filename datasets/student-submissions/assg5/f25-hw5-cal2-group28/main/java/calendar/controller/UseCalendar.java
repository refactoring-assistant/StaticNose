package calendar.controller;

import calendar.model.CalenderManager;
import calendar.view.ViewConsole;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for use calendar command.
 * */
public class UseCalendar implements CommandInterface {

  private final String inputText;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   * */
  public UseCalendar(String textInput) {
    this.inputText = textInput;
  }

  /**
   * Function to execute the command.
   *
   * @param manager instance of calendar manager
   * @param view instance of view console
   * */
  @Override
  public void execute(CalenderManager manager, ViewConsole view) {
    Pattern pattern = Pattern.compile("use calendar --name (\\S+)");
    Matcher matcher = pattern.matcher(inputText);

    if (!matcher.find()) {
      view.dispError("Invalid command to use calendar");
      return;
    }

    try {
      String calName = matcher.group(1);
      manager.fetchCal(calName);
      view.dispSuccess(String.format("Using calender: '%s'", calName));
    } catch (Exception e) {
      view.dispError("Error using calendar: " + e.getMessage());
    }
  }
}
