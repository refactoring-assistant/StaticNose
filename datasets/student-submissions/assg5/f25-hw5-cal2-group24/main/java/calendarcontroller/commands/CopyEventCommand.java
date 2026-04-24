package calendarcontroller.commands;

import calendarcontroller.TextCalendarController;
import calendarmodel.Event;
import calendarmodel.exceptions.EventNotFoundException;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.MultiCalendarModel;
import multicalendarmodel.ZonedCalendarModel;

/**
 * Command to copy a single event.
 * Expects input like:
 * copy event "Subject" on "YYYY-MM-DDTHH:MM" --target "TargetCal" to "NewTime"
 */
public class CopyEventCommand extends AbstractAppCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "copy event (.*?) on (.*?) --target (.*?) to (.*)", REGEX_FLAGS);

  @Override
  public boolean execute(String inputLine, MultiCalendarModel appModel, CalendarView view,
                         TextCalendarController controller) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }

    ZonedCalendarModel activeCal = controller.getActiveCalendar();
    if (activeCal == null) {
      view.displayError("Error: No calendar is in use. Cannot copy from a null calendar.");
      return true;
    }

    try {
      String subject = unquote(matcher.group(1));
      LocalDateTime findTime = parseDateTime(unquote(matcher.group(2)));
      String targetCalName = unquote(matcher.group(3));
      LocalDateTime targetTime = parseDateTime(unquote(matcher.group(4)));
      String sourceCalName = controller.getActiveCalendarName();
      List<Event> events = activeCal.getEventsFrom(findTime, findTime.plusNanos(1));
      Event eventToCopy = events.stream()
          .filter(e -> e.getSubject().equals(subject))
          .findFirst()
          .orElseThrow(() -> new EventNotFoundException(
              "No event found with subject '" + subject + "' at " + findTime));
      LocalDateTime findEndTime = eventToCopy.getEndTime();
      appModel.copyEvent(sourceCalName, subject, findTime, findEndTime,
          targetCalName, targetTime);
      view.displayMessage("Event copied successfully.");
    } catch (Exception e) {
      view.displayError("Error copying event: " + e.getMessage());
    }
    return true;
  }
}