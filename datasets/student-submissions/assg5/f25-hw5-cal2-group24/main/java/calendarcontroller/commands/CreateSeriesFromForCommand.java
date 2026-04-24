package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to create a recurring series of events with a specific start/end time,
 * defined by a total number of occurrences.
 *
 * <p>This class parses input matching the pattern
 * {@code create event "Subject" from YYYY-MM-DDTHH:MM to YYYY-MM-DDTHH:MM
 * repeats [WEEKDAYS] for [N] times}.</p>
 */
public class CreateSeriesFromForCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN =
      Pattern.compile(
          "create event (.*?) from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times",
          REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to create a recurring series based on a count.</p>
   */
  @Override
  public boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }

    try {
      String subject = unquote(matcher.group(1));
      LocalDateTime startTime = parseDateTime(matcher.group(2));
      LocalDateTime endTime = parseDateTime(matcher.group(3));
      List<DayOfWeek> weekdays = parseWeekdays(matcher.group(4));
      int occurrences = Integer.parseInt(matcher.group(5));
      Event prototype = Event.newBuilder(subject, startTime, endTime).build();
      model.createEventSeries(prototype, weekdays, occurrences);
      view.displayMessage("Event Series is created successfully.");
    } catch (Exception e) {
      view.displayError("Exception occurred while creating series: " + e.getMessage());
    }
    return true;
  }
}
