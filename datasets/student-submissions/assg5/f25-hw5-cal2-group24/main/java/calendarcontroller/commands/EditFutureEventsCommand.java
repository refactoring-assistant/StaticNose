package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.enums.EditMode;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to edit an event and all future events in its recurring series.
 *
 * <p>This class parses input matching the pattern
 * {@code edit events <property> "Subject" from YYYY-MM-DDTHH:MM with <new_value>}.</p>
 */
public class EditFutureEventsCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "edit events (\\S+) (.*?) from (\\S+) with (.*)",
      REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to edit this and all future events in a series.</p>
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
      Object newValue = parseNewValue(property, matcher.group(4));
      model.editEventSeries(subject, startTime, EditMode.THIS_AND_FUTURE, property, newValue);
      view.displayMessage("Event series for this and future events has been updated successfully");
    } catch (Exception e) {
      view.displayError("Exception occurred while editing events: " + e.getMessage());
    }
    return true;
  }
}
