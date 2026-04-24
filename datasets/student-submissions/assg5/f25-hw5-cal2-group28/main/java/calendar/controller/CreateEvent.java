package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.model.Events;
import calendar.view.ViewConsole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to control the creation of a single event (both all-day and timed).
 * */
public class CreateEvent implements CommandInterface {

  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   * */
  public CreateEvent(String textInput) {
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
      if (textInput.contains(" on ") && !textInput.contains(" from ")) {
        createAllDayEvent(calendar, view);
      } else if (textInput.contains(" from ")) {
        createTimedEvent(calendar, view);
      } else {
        view.dispError("invalid command to create event");
      }
    } catch (Exception e) {
      view.dispError("Error while creating command" + e.getMessage());
    }
  }

  /**
   * Function to create all day events.
   *
   * @param calendar instance of calendar class
   * @param view instance of view console
   * */
  private void createAllDayEvent(Calendar calendar, ViewConsole view) {
    Pattern pattern = Pattern.compile("create event (.+?) on (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (matcher.find()) {
      String subject = matcher.group(1);
      Events event = new Events(subject, LocalDate.parse(matcher.group(2)));
      calendar.addEvent(event);
      view.dispSuccess(String.format("Successfully created event: %s", subject));
    } else {
      view.dispError("invalid all-day event format");
    }
  }

  /**
   * Function to create timed event.
   *
   * @param calendar instance of calendar class
   * @param view instance of view console
   * */
  private void createTimedEvent(Calendar calendar, ViewConsole view) {
    Pattern pattern = Pattern.compile("create event (.+?) from (\\S+) to (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (matcher.find()) {
      String subject = matcher.group(1);
      LocalDateTime start = LocalDateTime.parse(matcher.group(2));
      LocalDateTime end = LocalDateTime.parse(matcher.group(3));
      Events event = new Events(subject, start, end);
      calendar.addEvent(event);
      view.dispSuccess(String.format("Successfully created event: %s", subject));
    } else {
      view.dispError("invalid timed event event format");
    }
  }
}