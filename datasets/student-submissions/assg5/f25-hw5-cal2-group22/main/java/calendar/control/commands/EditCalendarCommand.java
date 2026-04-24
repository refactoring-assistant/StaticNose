package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Command to edit calendar.
 */
public class EditCalendarCommand extends AbstractCommand {

  /**
   * Constructor for Editing calendar.
   */
  public EditCalendarCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  @Override
  public CommandResult execute() {
    try {
      if (!multipleCalendar.hasCalendars()) {
        return CommandResult.error("Error: No calendars exist. Create a calendar first using: "
            + " create calendar --name <name> --timezone <timezone>");
      }

      Pattern pattern = Pattern.compile(
          "^\\s*edit\\s+calendar\\s+--name\\s+(\"[^\"]+\"|\\S+)\\s+"
              + "--property\\s+(name|timezone)\\s+(.*)$",
          Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(input);

      if (!matcher.find()) {
        return CommandResult.error("Invalid edit calendar syntax. Use:\n"
            + "  edit calendar --name <current-name> --property name <new-name>\n"
            + "  edit calendar --name <current-name> --property timezone <new-timezone>");
      }
      String calendarName = StringUtils.removeQuotes(matcher.group(1));
      String property = matcher.group(2).toLowerCase();
      String newValue = StringUtils.removeQuotes(matcher.group(3).trim());

      if (property.equals("name")) {
        return renameCalendar(calendarName, newValue);
      } else {
        return changeTimeZone(calendarName, newValue);
      }
    } catch (Exception e) {
      return CommandResult.error("EditCalendarCommand error: " + e.getMessage());
    }
  }

  /**
   * Renames a calendar.
   */
  private CommandResult renameCalendar(String oldName, String newName) {
    try {
      multipleCalendar.renameCalendar(oldName, newName);
      return CommandResult.success("Calendar renamed from '"
          + oldName + "' to '" + newName + "' successfully.");
    } catch (IllegalArgumentException e) {
      return CommandResult.error("Error: " + e.getMessage());
    }
  }

  /**
   * Changes the timezone of a calendar.
   */
  private CommandResult changeTimeZone(String calendarName, String timezoneStr) {
    try {
      ZoneId newTimezone = ZoneId.of(timezoneStr);
      multipleCalendar.changeTimeZone(calendarName, newTimezone);
      return CommandResult.success("Timezone for calendar '" + calendarName + "' changed to '"
          + timezoneStr + "' successfully.");
    } catch (java.time.DateTimeException e) {
      return CommandResult.error("Invalid timezone: " + timezoneStr + " Continent/City");
    } catch (IllegalArgumentException e) {
      return CommandResult.error("Error: " + e.getMessage());
    }
  }
}
