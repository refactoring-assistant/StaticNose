package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to create a single event with a specific start and end time.
 *
 * <p>This class parses input matching the pattern
 * {@code create event "Subject" from YYYY-MM-DDTHH:MM to YYYY-MM-DDTHH:MM}.</p>
 */
public class CreateSingleFromCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "create event (.*?) from (\\S+) to (\\S+)",
      REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to create a single event with a specific time range.</p>
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
      Event newEvent = Event.newBuilder(subject, startTime, endTime).build();
      model.createSingleEvent(newEvent);
      view.displayMessage("Event is created successfully.");
    } catch (Exception e) {
      view.displayError("Exception occurred while creating event: " + e.getMessage());
    }
    return true;
  }
}
