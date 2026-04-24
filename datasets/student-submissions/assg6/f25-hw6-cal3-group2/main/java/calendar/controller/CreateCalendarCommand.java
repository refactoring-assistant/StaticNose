package calendar.controller;

import calendar.model.MultiCalendarManager;
import calendar.model.MultiCalendarManagerInterface;
import calendar.view.CalendarView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to create a new calendar.
 */
public class CreateCalendarCommand extends CommandFactory {
  private static final Pattern CREATE_CALENDAR = Pattern.compile(
      "^create calendar --name (\\S+) --timezone (.+)$");

  private final MultiCalendarManagerInterface manager;
  private final String commandLine;
  private final CalendarView view;

  /**
   * Constructor for CreateCalendarCommand.
   *
   * @param commandLine the input command string
   * @param manager the calendar manager
   */
  public CreateCalendarCommand(String commandLine, MultiCalendarManagerInterface manager,
                               CalendarView view) {
    this.commandLine = commandLine;
    this.manager = manager;
    this.view = view;
  }

  @Override
  protected void parseCommand(String commandLine) {
    Matcher matcher = CREATE_CALENDAR.matcher(commandLine);
    if (matcher.matches()) {
      String name = matcher.group(1).trim();
      String timezone = matcher.group(2).trim();
      try {
        manager.createCalendar(name, timezone);
        view.displaySuccess(String.format("Successfully created calendar '%s'", name));
      } catch (IllegalArgumentException e) {
        view.displayError("Print failed." + e.getMessage());
      }
    } else {
      view.displayError("Invalid command");
    }
  }

  @Override
  protected void execute() {
    parseCommand(commandLine);
  }
}
