package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.view.ViewConsole;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to control show if user is available or busy.
 * */
public class ShowStatus implements CommandInterface {
  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   * */
  public ShowStatus(String textInput) {
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
    Calendar calendar = manager.getCurrentCalender();
    if (calendar == null) {
      view.dispError("No calender in use. Use 'use' command first");
      return;
    }

    Pattern pattern = Pattern.compile("show status on (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid command to show status");
      return;
    }

    try {
      LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1));
      String status = calendar.isBusy(dateTime) ? "busy" : "available";
      view.showStatus(status);
    } catch (Exception e) {
      view.dispError("Error while checking status: " + e.getMessage());
    }
  }
}
