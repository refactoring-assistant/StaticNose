package calendar.controller;

import calendar.enums.UserStatus;
import calendar.model.CalendarInterface;
import calendar.view.CalendarView;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the process of handling print command.
 */
public class ShowCommand extends CommandFactory {

  private static final Pattern SHOW_STATUS =  Pattern.compile("^show status on (\\S+)$");

  private final CalendarInterface calendar;
  private final CalendarView view;
  private final String commandLine;

  /**
   * Constructs an ShowCommand instance.
   *
   * @param commandLine the input command string
   * @param calendar the calendar object
   * @param view the calendar view to display
   */
  public ShowCommand(String commandLine, CalendarInterface calendar, CalendarView view) {
    this.calendar = calendar;
    this.view = view;
    this.commandLine = commandLine;
  }

  @Override
  protected void parseCommand(String commandLine) {
    Matcher matcher = SHOW_STATUS.matcher(commandLine);
    try {
      if (matcher.matches()) {
        String queryDateTime = matcher.group(1).trim();
        LocalDateTime queryTime = LocalDateTime.parse(queryDateTime);
        UserStatus status = calendar.getUserStatus(queryTime);
        view.displayUserStatus(status);
        return;
      }
      view.displayError("Show status failed. Invalid command: " + commandLine);
    } catch (Exception e) {
      view.displayError("Show status failed. " + e.getMessage());
    }
  }

  @Override
  protected void execute() {
    parseCommand(commandLine);
  }
}
