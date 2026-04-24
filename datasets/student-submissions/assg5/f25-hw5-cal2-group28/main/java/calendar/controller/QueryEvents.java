package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.model.Events;
import calendar.view.ViewConsole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to print all events.
 * */
public class QueryEvents implements CommandInterface {

  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   * */
  public QueryEvents(String textInput) {
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
      view.dispError("No calendar in use. Use 'use calendar' command first");
      return;
    }

    try {
      List<Events> eventsList = new ArrayList<>();
      Pattern pattern;
      Matcher matcher;

      if (textInput.contains(" on ")) {
        pattern = Pattern.compile("print events on (\\S+)");
        matcher = pattern.matcher(textInput);

        if (matcher.find()) {
          eventsList = calendar.getEvents(LocalDate.parse(matcher.group(1)));
        }
      } else if (textInput.contains(" from ")) {
        pattern = Pattern.compile("print events from (\\S+) to (\\S+)");
        matcher = pattern.matcher(textInput);
        if (!matcher.find()) {
          view.dispError("No events found");
          return;
        }
        LocalDateTime from = LocalDateTime.parse(matcher.group(1));
        LocalDateTime to = LocalDateTime.parse(matcher.group(2));
        eventsList = calendar.getEventsBetween(from, to);
      } else {
        view.dispError("Invalid print command");
        return;
      }

      view.showEvents(eventsList);
    } catch (Exception e) {
      view.dispError("Error querying events: " + e.getMessage());
    }
  }
}
