package calendar.controller;

import calendar.model.CalendarInterface;
import calendar.model.MultiCalendarManagerInterface;
import calendar.view.CalendarView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the process of event(s) copy. Use regex to match copy pattern and
 * copy event or events with given property values.
 */
public class CopyCommand extends CommandFactory {

  private static final Pattern COPY_EVENTS_BETWEEN = Pattern.compile(
      "^copy events between (\\S+) and (\\S+) --target (?:(\\S+)|\"([^\"]*)\") to (\\S+)$");

  private static final Pattern COPY_EVENTS_ON = Pattern.compile(
      "^copy events on (\\S+) --target (?:(\\S+)|\"([^\"]*)\") to (\\S+)$");

  private static final Pattern COPY_EVENT_ON = Pattern.compile(
      "^copy event (?:(\\S+)|\"([^\"]*)\") on (\\S+) "
          + "--target (?:(\\S+)|\"([^\"]*)\") to (\\S+)$");


  private final String commandLine;
  private final MultiCalendarManagerInterface manager;
  private final CalendarView view;


  /**
   * Constructor for CopyCommand Class.
   *
   * @param commandLine the input line of a command
   * @param manager     the calendar manager
   * @param view        the view for output
   */
  public CopyCommand(String commandLine, MultiCalendarManagerInterface manager,
                     CalendarView view) {
    this.commandLine = commandLine;
    this.manager = manager;
    this.view = view;
  }

  @Override
  protected void execute() throws IllegalArgumentException {
    parseCommand(this.commandLine);
  }

  @Override
  protected void parseCommand(String commandLine) throws IllegalArgumentException {
    Matcher matcher;

    matcher = COPY_EVENTS_BETWEEN.matcher(commandLine);
    if (matcher.matches()) {
      String startDate = matcher.group(1).trim();
      String endDate = matcher.group(2).trim();
      String targetCalendarName =
          ((matcher.group(3) != null) ? matcher.group(3).trim() : matcher.group(4)).trim();
      String targetDay = matcher.group(5);
      manager.copyEventsBetweenDays(startDate, endDate, targetCalendarName, targetDay);
      return;
    }

    matcher = COPY_EVENTS_ON.matcher(commandLine);
    if (matcher.matches()) {
      String specificDate = matcher.group(1).trim();
      String targetCalendarName =
          ((matcher.group(2) != null) ? matcher.group(2).trim() : matcher.group(3)).trim();
      String targetDay = matcher.group(4).trim();
      manager.copyEventsOnThatDay(specificDate, targetCalendarName, targetDay);
      return;
    }

    matcher = COPY_EVENT_ON.matcher(commandLine);
    if (matcher.matches()) {
      String subject =
          (matcher.group(1) != null) ? matcher.group(1).trim() : matcher.group(2).trim();
      String startDateTime = matcher.group(3).trim();
      String targetCalendarName =
          ((matcher.group(4) != null) ? matcher.group(4).trim() : matcher.group(5)).trim();
      String targetDateTime = matcher.group(6).trim();
      manager.copyEvent(subject, startDateTime, targetCalendarName, targetDateTime);
      return;
    }
    view.displayError("Copy event(s) failure. Wrong format: " + commandLine);

  }
}
