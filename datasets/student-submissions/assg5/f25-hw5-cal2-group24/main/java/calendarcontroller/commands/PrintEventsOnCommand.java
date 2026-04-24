package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to print all events overlapping with a specific date.
 *
 * <p>This class parses input matching the pattern
 * {@code print events on YYYY-MM-DD}.</p>
 */
public class PrintEventsOnCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "print events on (\\S+)", REGEX_FLAGS);

  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to print events on the specified date.</p>
   */
  @Override
  public boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    try {
      LocalDate date = parseDate(matcher.group(1));
      List<Event> events = model.getEventsOn(date);
      List<String> formattedEvents = new ArrayList<>();
      for (Event event : events) {
        String location = event.getLocation() != null ? " | Location: " + event.getLocation() : "";
        String formattedLine = String.format("  - %s from %s to %s%s",
            event.getSubject(),
            event.getStartTime().format(timeFormatter),
            event.getEndTime().format(timeFormatter),
            location);
        formattedEvents.add(formattedLine);
      }
      view.displayEventsOn(date, formattedEvents);
    } catch (Exception e) {
      view.displayError("Exception Occurred while printing events: " + e.getMessage());
    }
    return true;
  }
}
