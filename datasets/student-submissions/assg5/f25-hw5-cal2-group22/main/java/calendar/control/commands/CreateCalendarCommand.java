package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The command to create a new calendar.
 */
public class CreateCalendarCommand extends AbstractCommand {
  /**
   * Constructor to create a calendar.
   *
   * @param multipleCalendar - multiple calendars
   * @param input            - input string
   */
  public CreateCalendarCommand(IcalendarDatabase multipleCalendar,
                               String input) {
    super(multipleCalendar, input);
  }

  @Override
  public CommandResult execute() {
    try {
      Pattern pattern = Pattern.compile(
          "create calendar\\s+--name\\s+\"?([^\"]+)\"?\\s+--timezone\\s+(\\S+)",
          Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(input);

      if (matcher.find()) {
        String name = StringUtils.removeQuotes(matcher.group(1));
        String timezone = matcher.group(2);

        ZoneId zoneId;
        try {
          zoneId = ZoneId.of(timezone);
        } catch (Exception ex) {
          return CommandResult.error("Invalid time zone: " + timezone);
        }

        multipleCalendar.createCalendar(name, zoneId);
        return CommandResult.success("Calendar '" + name + "' created successfully.");
      } else {
        return CommandResult.error(
            "Invalid syntax. Use: create calendar --name <name> "
                + "--timezone <timezone>");
      }
    } catch (Exception e) {
      return CommandResult.error("Create calendar failed: " + e.getMessage());
    }
  }
}
