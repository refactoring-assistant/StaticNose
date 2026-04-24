package calendar.control.commands;

import calendar.control.editmodes.EditForward;
import calendar.control.editmodes.EditSeries;
import calendar.control.editmodes.EditSingleInstance;
import calendar.control.editmodes.IeditModes;
import calendar.control.results.CommandResult;
import calendar.model.AbstractEvent;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command that edits an event or series of events.
 * Supports three scopes: event, events, and series.
 * Example commands:
 * edit event subject "Meeting" from 2025-05-05T10:00 with "Review"
 * edit events location "Class" from 2025-05-05T09:00 with "Room 101"
 * edit series status "Project" from 2025-05-05T10:00 with "private"
 */
public class EditEventCommand extends AbstractCommand {

  /**
   * Constructs an edit event command.
   *
   * @param multipleCalendar calendar model instance
   * @param input            raw command text entered by the user
   */
  public EditEventCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  /**
   * Normalizes property names for the model, converting spaces to underscores
   * and handling common variations.
   *
   * @param p the raw property token from input
   * @return normalized property name used by the model
   */
  private static String normalizeProperty(String p) {

    String norm = p.trim().toLowerCase().replace(' ', '_');

    if (norm.equals("starttime")) {
      return "start_time";
    }
    if (norm.equals("startdate")) {
      return "start_date";
    }
    if (norm.equals("endtime")) {
      return "end_time";
    }
    if (norm.equals("enddate")) {
      return "end_date";
    }
    return norm;
  }

  @Override
  public CommandResult execute() {
    try {

      if (!multipleCalendar.hasCalendars()) {
        return CommandResult.error("Error: No calendars exist. Create a calendar first using:\n"
            + "  create calendar --name <name> --timezone <timezone>");
      }

      Pattern p = Pattern.compile(
          "edit\\s+(event|events|series)\\s+(\\w+)\\s+\"?([^\"]+)\"?\\s"
              + "+from\\s+(\\S+)(?:\\s+to\\s+(\\S+))?(?:\\s+with\\s+(.+))?",
          Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(input);

      if (!m.find()) {
        return CommandResult.error("Invalid edit command syntax.");
      }

      String scope = m.group(1).toLowerCase();
      String property = m.group(2);
      String subject = StringUtils.removeQuotes(m.group(3));
      LocalDateTime start = LocalDateTime.parse(m.group(4));
      String endString = m.group(5);
      String newValue = StringUtils.removeQuotes(m.group(6));

      Optional<Imodel> currentCal = multipleCalendar.getCurrent();
      if (currentCal.isEmpty()) {
        return CommandResult.error("Error: No calendar is currently selected. Use:\n"
            + "  use calendar --name <name>\n\n"
            + "Available calendars: " + getAvailableCalendarNames());
      }
      Imodel model = currentCal.get();

      LocalDateTime end;
      if (endString != null && !endString.isEmpty()) {
        end = LocalDateTime.parse(endString);
      } else {
        AbstractEvent foundEvent = findEvent(model, subject, start);
        if (foundEvent == null) {
          return CommandResult.error("Error: Could not find event '"
              + subject + "' starting at " + start);
        }
        end = foundEvent.getEnd();
      }

      String prop = normalizeProperty(property);

      IeditModes modes =
          scope.equals("event") ? new EditSingleInstance() :
              scope.equals("events") ? new EditForward() :
                  new EditSeries();

      boolean ok = modes.edit(model, subject, start, end, prop, newValue);
      if (ok) {
        return CommandResult.success("Event edited successfully.");
      } else {
        return CommandResult.error("Failed to edit event.");
      }
    } catch (Exception e) {
      return CommandResult.error("EditEventCommand error: " + e.getMessage() + "\n");
    }
  }

  /**
   * Helper method to find an event by subject and start time.
   */
  private AbstractEvent findEvent(Imodel model, String subject, LocalDateTime start) {
    return model.getAllEvents().stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(subject)
            && e.getStart().equals(start))
        .findFirst()
        .orElse(null);
  }

}
