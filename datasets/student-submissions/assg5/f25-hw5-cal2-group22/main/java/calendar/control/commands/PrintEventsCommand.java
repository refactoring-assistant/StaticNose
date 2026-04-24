package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command that prints calendar events.
 * Supports printing events on a specific date or within a date-time range.
 * Example commands:
 * print events on 2025-05-05
 * print events from 2025-05-05T09:00 to 2025-05-05T12:00
 */
public class PrintEventsCommand extends AbstractCommand {

  /**
   * Creates a PrintEventsCommand.
   *
   * @param multipleCalendar the calendar model
   * @param input            the raw user command
   */
  public PrintEventsCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);

  }

  /**
   * Executes the print command.
   * Determines whether to print events for a single date or a date-time range.
   *
   * @return the result to be printed in view
   */
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

      if ((input.toLowerCase().contains(" on ")) && !(input.toLowerCase().contains(" to "))) {
        return printEventsOnDate(model);
      } else if (input.toLowerCase().contains(" from ") && input.toLowerCase().contains(" to ")) {
        return printEventsBetween(model);
      }

      return CommandResult.error(
          "Invalid print events syntax.\n"
              + "Commands are: print events on YYYY-MM-DD and "
              + "print events from YYYY-MM-DDThh:mm to YYYY-MM-DDThh:mm");
    } catch (Exception e) {
      return CommandResult.error("Print events failed: " + e.getMessage());
    }
  }

  private CommandResult printEventsOnDate(Imodel model) {
    Pattern pattern = Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      LocalDate date = LocalDate.parse(matcher.group(1));
      List<String> events = model.getEventsOn(date);
      return formatEventsList(events);
    }

    return CommandResult.error("Invalid date format.");
  }

  private CommandResult printEventsBetween(Imodel model) {
    Pattern pattern = Pattern.compile("print events from (\\S+) to (\\S+)",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      LocalDateTime from = LocalDateTime.parse(matcher.group(1));
      LocalDateTime to = LocalDateTime.parse(matcher.group(2));
      List<String> events = model.getEventsBetween(from, to);
      return formatEventsList(events);
    }
    return CommandResult.error("Invalid date-time format.");
  }

  private CommandResult formatEventsList(List<String> events) {
    if (events.isEmpty()) {
      return CommandResult.success("No events found in range.");
    }

    String calendarName = multipleCalendar.getCurrentCalendarName();
    ZoneId currentTimeZone = multipleCalendar.getTimeZone(calendarName);

    StringBuilder result = new StringBuilder();
    result.append("CalendarName: ").append(calendarName).append("\n");
    result.append("TimeZone: ").append(currentTimeZone).append("\n");
    result.append("Subject | Day | Start to End | Location | Description | Status\n");
    result.append("--------------------------------------------------------------\n");

    for (String event : events) {
      result.append(event).append("\n");
    }

    return CommandResult.success(result.toString().trim());
  }
}
