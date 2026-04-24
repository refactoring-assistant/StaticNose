package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to create a single "all-day" event (8am to 5pm) on a specific date.
 *
 * <p>This class parses input matching the pattern
 * {@code create event "Subject" on YYYY-MM-DD}.</p>
 */
public class CreateSingleOnCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "create event (.*?) on (\\S+)", REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to create a single all-day event.</p>
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
      Event newEvent = Event.newBuilder(subject, startTime, endTime).build();
      model.createSingleEvent(newEvent);
      view.displayMessage("All Day Event is created successfully.");
    } catch (Exception e) {
      view.displayError("Exception occurred while creating event: " + e.getMessage());
    }
    return true;
  }
}
