package calendar.controller;

import calendar.event.Event;
import calendar.model.CalendarInterface;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the process of handling print command.
 */
public class PrintCommand extends CommandFactory {

  private static final Pattern PRINT_ON = Pattern.compile(
      "^print events on (\\S+)$");

  private static final Pattern PRINT_FROM_TO = Pattern.compile(
      "^print events from (\\S+) to (\\S+)$");

  private final CalendarInterface calendar;
  private final CalendarView view;
  private final String commandLine;

  /**
   * Constructs an PrintCommand instance.
   *
   * @param commandLine the input command string
   * @param calendar the calendar object
   * @param view the calendar view to display
   */
  public PrintCommand(String commandLine, CalendarInterface calendar, CalendarView view) {
    this.calendar = calendar;
    this.view = view;
    this.commandLine = commandLine;
  }

  @Override
  protected void parseCommand(String commandLine) {
    Matcher matcher;
    try {
      matcher = PRINT_ON.matcher(commandLine);
      if (matcher.matches()) {
        String dataString = matcher.group(1).trim();
        LocalDate date = LocalDate.parse(dataString);
        List<Event> events = calendar.getEventsOnDate(date);
        view.displayEventsOnDate(events, date);
        return;
      }
      matcher = PRINT_FROM_TO.matcher(commandLine);
      if (matcher.matches()) {
        String startDateTime = matcher.group(1).trim();
        String endDateTime = matcher.group(2).trim();
        LocalDateTime start = LocalDateTime.parse(startDateTime);
        LocalDateTime end = LocalDateTime.parse(endDateTime);
        List<Event> events = calendar.getEventsBetween(start, end);
        view.displayEventsBetween(events, start, end);
        return;
      }
      view.displayError("Print event failed. Invalid command: " + commandLine);
    } catch (Exception e) {
      view.displayError("Print failed." + e.getMessage());
    }
  }

  @Override
  protected void execute() {
    parseCommand(this.commandLine);
  }
}
