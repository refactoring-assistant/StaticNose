package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command that creates a new calendar event.
 * Supports creating both timed and all-day events.
 */
public class CreateEventCommand extends AbstractCommand {

  /**
   * Creates a new CreateEventCommand.
   *
   * @param multipleCalendar all the calendars used to store events
   * @param input            raw command string entered by the user
   */
  public CreateEventCommand(IcalendarDatabase multipleCalendar, String input) {
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

      if (input.toLowerCase().contains(" from ") && input.toLowerCase().contains(" to ")) {
        return createTimedEvent(model);
      } else if (input.toLowerCase().contains(" on ")) {
        return createAllDayEvent(model);
      }
      return CommandResult.error("Invalid create event syntax.");
    } catch (Exception e) {
      return CommandResult.error("Create event failed: " + e.getMessage());
    }
  }

  private CommandResult createTimedEvent(Imodel model) {
    Pattern pattern = Pattern.compile(
        "create event\\s+\"?([^\"]+)\"?\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      String subject = StringUtils.removeQuotes(matcher.group(1));
      LocalDateTime start = LocalDateTime.parse(matcher.group(2));
      LocalDateTime end = LocalDateTime.parse(matcher.group(3));
      boolean ok = model.createEvent(subject, start, end);

      String calendarName = multipleCalendar.getCurrentCalendarName();
      if (ok) {
        return CommandResult.success(
            "Event created in calendar '" + calendarName + "' with subject " + subject);
      } else {
        return CommandResult.error("Failed to create event (conflict may exist).");
      }
    }

    return CommandResult.error("Invalid timed event syntax.");
  }

  private CommandResult createAllDayEvent(Imodel model) {
    Pattern pattern = Pattern.compile(
        "create event\\s+\"?([^\"]+)\"?\\s+on\\s+(\\S+)",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      String subject = StringUtils.removeQuotes(matcher.group(1));
      LocalDate date = LocalDate.parse(matcher.group(2));
      boolean ok = model.createAllDayEvent(subject, date);
      String calendarName = multipleCalendar.getCurrentCalendarName();
      if (ok) {
        return CommandResult.success(
            "All-day event created in calendar '" + calendarName + "' with subject " + subject);
      } else {
        return CommandResult.error("Failed to create event (conflict may exist).");
      }
    }

    return CommandResult.error("Invalid all-day event syntax.");
  }
}

