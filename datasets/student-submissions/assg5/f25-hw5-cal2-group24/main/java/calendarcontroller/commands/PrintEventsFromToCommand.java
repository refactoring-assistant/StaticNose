package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to print all events within a specific date and time range.
 *
 * <p>This class parses input matching the pattern
 * {@code print events from YYYY-MM-DDTHH:MM to YYYY-MM-DDTHH:MM}.</p>
 */
public class PrintEventsFromToCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "print events from (\\S+) to (\\S+)", REGEX_FLAGS);

  private static final DateTimeFormatter printDateFormat =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter printTimeFormatter =
      DateTimeFormatter.ofPattern("hh:mm a");

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to print events within the specified range.</p>
   */
  @Override
  public boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    try {
      LocalDateTime startTime = parseDateTime(matcher.group(1));
      LocalDateTime endTime = parseDateTime(matcher.group(2));
      List<Event> events = model.getEventsFrom(startTime, endTime);
      List<String> formattedEvents = new ArrayList<>();
      for (Event event : events) {
        String location = event.getLocation() != null ? " | Location: " + event.getLocation() : "";
        String formattedLine = String.format(
            "- %s starting on %s at %s, ending on %s at %s%s",
            event.getSubject(),
            event.getStartTime().format(printDateFormat),
            event.getStartTime().format(printTimeFormatter),
            event.getEndTime().format(printDateFormat),
            event.getEndTime().format(printTimeFormatter),
            location
        );
        formattedEvents.add(formattedLine);
      }
      view.displayEventsFromTo(formattedEvents);
    } catch (Exception e) {
      view.displayError("Exception occurred while printing event: " + e.getMessage());
    }
    return true;
  }
}
