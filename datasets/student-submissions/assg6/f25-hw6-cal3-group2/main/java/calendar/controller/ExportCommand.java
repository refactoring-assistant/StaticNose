package calendar.controller;

import calendar.calendarentity.CalendarEntityInterface;
import calendar.model.CalendarInterface;
import calendar.view.CalendarView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the process of handling export command.
 */
public class ExportCommand extends CommandFactory {
  private static final Pattern EXPORT = Pattern.compile("^export cal (\\S+)$");
  private final CalendarInterface calendar;
  private final CalendarView view;
  private final String commandLine;
  private final CalendarEntityInterface calendarEntity;

  /**
   * Constructs an ExportCommand instance.
   *
   * @param commandLine the input command string
   * @param calendarEntity the calendarEntity object
   * @param view the calendar view to display
   */
  public ExportCommand(String commandLine, CalendarEntityInterface calendarEntity,
                       CalendarView view) {
    this.calendar = calendarEntity != null ? calendarEntity.getCalendar() : null;
    this.calendarEntity = calendarEntity;
    this.view = view;
    this.commandLine = commandLine;
  }

  @Override
  protected void parseCommand(String commandLine) {
    Matcher matcher = EXPORT.matcher(commandLine);
    try {
      if (matcher.matches()) {
        String fileName = matcher.group(1).trim();
        String finalFileName = fileName;
        String content;
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".csv")) {
          content = calendar.exportToCsv();
        } else if (lowerFileName.endsWith(".ical")) {
          finalFileName = finalFileName.replace(".ical", ".ics");
          content = calendar.exportToIcal(
              calendarEntity.getCalendarName(),
              calendarEntity.getTimeZone()
          );
        } else {
          view.displayError("Invalid file name.");
          return;
        }
        view.exportCalendar(content, finalFileName);
        return;
      }
      view.displayError("Export failed. Invalid command: " + commandLine);
    } catch (Exception e) {
      view.displayError("Export failed." + e.getMessage());
    }
  }

  @Override
  protected void execute() {
    parseCommand(commandLine);
  }
}
