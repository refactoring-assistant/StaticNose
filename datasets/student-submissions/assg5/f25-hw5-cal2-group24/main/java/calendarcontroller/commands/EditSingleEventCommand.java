package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to edit a single, specific event instance.
 *
 * <p>This class parses input matching the pattern
 * {@code edit event <property> "Subject" from YYYY-MM-DDTHH:MM to YYYY-MM-DDTHH:MM
 * with <new_value>}.</p>
 */
public class EditSingleEventCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "edit event (\\S+) (.*?) from (\\S+) to (\\S+) with (.*)", REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to edit a single event.</p>
   */
  @Override
  public boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }

    try {
      String property = matcher.group(1);
      String subject = unquote(matcher.group(2));
      LocalDateTime startTime = parseDateTime(matcher.group(3));
      LocalDateTime endTime = parseDateTime(matcher.group(4));
      Object newValue = parseNewValue(property, matcher.group(5));
      model.editSingleEvent(subject, startTime, endTime, property, newValue);
      view.displayMessage("Event is updated successfully.");
    } catch (Exception e) {
      view.displayError("Exception occurred while editing event: " + e.getMessage());
    }
    return true;
  }
}
