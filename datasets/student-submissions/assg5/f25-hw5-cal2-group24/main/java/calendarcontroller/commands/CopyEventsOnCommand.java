package calendarcontroller.commands;

import calendarcontroller.TextCalendarController;
import calendarview.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.MultiCalendarModel;
import multicalendarmodel.ZonedCalendarModel;

/**
 * Command to copy all events on a specific date.
 * Expects input like:
 * copy events on "2025-11-10" --target "TargetCal" to "2025-12-01"
 */
public class CopyEventsOnCommand extends AbstractAppCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "copy events on (.*?) --target (.*?) to (.*)", REGEX_FLAGS);

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
      LocalDate sourceDate = parseDate(unquote(matcher.group(1)));
      String targetCalName = unquote(matcher.group(2));
      LocalDate targetDate = parseDate(unquote(matcher.group(3)));

      String sourceCalName = controller.getActiveCalendarName();
      LocalDateTime sourceStart = sourceDate.atStartOfDay();
      LocalDateTime sourceEnd = sourceDate.plusDays(1).atStartOfDay();
      LocalDateTime targetStart = targetDate.atStartOfDay();

      appModel.copyEventInterval(sourceCalName, sourceStart, sourceEnd,
          targetCalName, targetStart);
      view.displayMessage("Events copied successfully.");
    } catch (Exception e) {
      view.displayError("Error copying events: " + e.getMessage());
    }
    return true;
  }
}