package calendar.controller;

import calendar.calendarentity.CalendarEntityInterface;
import calendar.model.MultiCalendarManagerInterface;
import calendar.view.CalendarView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the process of using a calendar. Use regex to match use command and
 * set up the context of the calendar.
 */
public class UseCalendarCommand extends CommandFactory {
  private static final Pattern USE_CALENDAR = Pattern.compile(
      "^use calendar --name (?:(\\S+)|\"([^\"]*)\")$");

  private final MultiCalendarManagerInterface manager;
  private final CalendarView view;
  private final String commandLine;

  /**
   * Constructor for UseCalendarCommand Class.
   *
   * @param commandLine the input line of a command
   * @param manager     the calendar manager
   * @param view        the calendar view part
   */
  public UseCalendarCommand(String commandLine, MultiCalendarManagerInterface manager,
                            CalendarView view) {
    this.commandLine = commandLine;
    this.manager = manager;
    this.view = view;
  }

  @Override
  protected void parseCommand(String commandLine) {
    Matcher matcher = USE_CALENDAR.matcher(commandLine);
    if (matcher.matches()) {
      String calendarName =
          ((matcher.group(1) != null) ? matcher.group(1).trim() : matcher.group(2)).trim();

      CalendarEntityInterface entity = manager.getCalendarEntity(calendarName);
      manager.useThisCalendarEntity(entity);
      return;
    }
    view.displayError("Copy calendar failure. Wrong format: " + commandLine);
  }

  @Override
  protected void execute() {
    parseCommand(commandLine);
  }
}
