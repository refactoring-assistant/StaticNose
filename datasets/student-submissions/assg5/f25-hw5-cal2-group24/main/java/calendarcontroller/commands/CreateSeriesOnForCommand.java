package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to create a recurring series of "all-day" events (8am to 5pm),
 * defined by a total number of occurrences.
 *
 * <p>This class parses input matching the pattern
 * {@code create event "Subject" on YYYY-MM-DD repeats [WEEKDAYS] for [N] times}.</p>
 */
public class CreateSeriesOnForCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "create event (.*?) on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times",
      REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to create an all-day recurring series based on a count.</p>
   */
  @Override
  public boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    try {
      String subject = unquote(matcher.group(1));
      LocalDate date = parseDate(matcher.group(2));
      LocalDateTime startTime = date.atTime(8, 0);
      LocalDateTime endTime = date.atTime(17, 0);
      List<DayOfWeek> weekdays = parseWeekdays(matcher.group(3));
      int occurrences = Integer.parseInt(matcher.group(4));
      Event prototype = Event.newBuilder(subject, startTime, endTime).build();
      model.createEventSeries(prototype, weekdays, occurrences);
      view.displayMessage("All day event series have been created successfully.");
    } catch (Exception e) {
      view.displayError("Exception occurred while creating series: " + e.getMessage());
    }
    return true;
  }
}
