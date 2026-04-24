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
 * Command to copy all events within a date range.
 * Expects input like:
 * copy events between "2025-11-10" and "2025-11-12" --target "TargetCal" to "2025-12-01"
 */
public class CopyEventsBetweenCommand extends AbstractAppCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "copy events between (.*?) and (.*?) "
          + "--target (.*?) to (.*)", REGEX_FLAGS);

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
      LocalDate sourceDateStart = parseDate(unquote(matcher.group(1)));
      LocalDate sourceDateEnd = parseDate(unquote(matcher.group(2)));
      String targetCalName = unquote(matcher.group(3));
      LocalDate targetDateStart = parseDate(unquote(matcher.group(4)));

      String sourceCalName = controller.getActiveCalendarName();
      LocalDateTime sourceStart = sourceDateStart.atStartOfDay();
      LocalDateTime sourceEnd = sourceDateEnd.plusDays(1).atStartOfDay();
      LocalDateTime targetStart = targetDateStart.atStartOfDay();

      appModel.copyEventInterval(sourceCalName, sourceStart, sourceEnd,
          targetCalName, targetStart);
      view.displayMessage("Events copied successfully.");
    } catch (Exception e) {
      view.displayError("Error copying events: " + e.getMessage());
    }
    return true;
  }
}