package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to use the given command.
 */
public class UseCalendarCommand extends AbstractCommand {

  /**
   * Constructor of the Use calendar command.
   *
   * @param multipleCalendar - multiple calendar
   * @param input            - input command
   */
  public UseCalendarCommand(IcalendarDatabase multipleCalendar, String input) {
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

      Pattern pattern = Pattern.compile(
          "use calendar\\s+--name\\s+\"?([^\"]+)\"?",
          Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(input);

      if (matcher.find()) {
        String name = StringUtils.removeQuotes(matcher.group(1));
        multipleCalendar.useCalendar(name);
        return CommandResult.success("Now using calendar: " + name);
      } else {
        return CommandResult.error("Invalid use calendar syntax. Use: use calendar --name <name>");
      }

    } catch (Exception e) {
      return CommandResult.error("UseCalendarCommand error: " + e.getMessage());
    }
  }
}
