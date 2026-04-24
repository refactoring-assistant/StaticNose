package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.enums.EditMode;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to edit all events in a recurring series.
 *
 * <p>This class parses input matching the pattern
 * {@code edit series <property> "Subject" from YYYY-MM-DDTHH:MM with <new_value>}.</p>
 */
public class EditAllSeriesCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "edit series (\\S+) (.*?) from (\\S+) with (.*)", REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to edit all events in a series.</p>
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
      model.editEventSeries(subject, startTime, EditMode.ALL_IN_SERIES, property, newValue);
      view.displayMessage("Event series for all have been updated successfully.");
    } catch (Exception e) {
      view.displayError("Exception occurred while editing series: "
          + e.getMessage());
    }
    return true;
  }
}
