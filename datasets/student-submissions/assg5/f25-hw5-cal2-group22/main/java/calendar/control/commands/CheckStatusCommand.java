package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command: show busy/available status at a specific date-time.
 * Syntax: show status on 2025-05-05T10:30
 */
public class CheckStatusCommand extends AbstractCommand {
  /**
   * Constructor of this class.
   *
   * @param multipleCalendar - multiple calendars
   * @param input            - String input
   */
  public CheckStatusCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  @Override
  public CommandResult execute() {
    try {
      if (!multipleCalendar.hasCalendars()) {
        return CommandResult.error(
            "No calendars exist. Create a calendar first using:\n"
                + "  create calendar --name <name> --timezone <timezone>");
      }

      Optional<Imodel> currentCal = multipleCalendar.getCurrent();
      if (currentCal.isEmpty()) {
        return CommandResult.error(
            "No calendar is currently selected. Use:\n"
                + "  use calendar --name <name>\n\n"
                + "Available calendars: " + getAvailableCalendarNames());
      }

      Imodel model = currentCal.get();

      Pattern p = Pattern.compile("show\\s+status\\s+on\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(input);
      if (!m.find()) {
        return CommandResult.error("Invalid status command syntax. Use: show status on <datetime>");

      }
      LocalDateTime dateTime = LocalDateTime.parse(m.group(1));
      boolean busy = model.isBusy(dateTime);
      String calendarName = multipleCalendar.getCurrentCalendarName();
      String status = busy ? "Busy" : "Available";
      return CommandResult.success(status + " (Calendar: " + calendarName + ")");

    } catch (Exception e) {
      return CommandResult.error("Check status failed: " + e.getMessage());
    }
  }
}