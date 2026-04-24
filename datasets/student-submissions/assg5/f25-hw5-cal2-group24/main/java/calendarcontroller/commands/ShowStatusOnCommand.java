package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarview.CalendarView;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to check if the user is busy at a specific moment in time.
 *
 * <p>This class parses input matching the pattern
 * {@code show status on YYYY-MM-DDTHH:MM}.</p>
 */
public class ShowStatusOnCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN =
      Pattern.compile("show status on (\\S+)", REGEX_FLAGS);

  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes the command to show the user's busy status at a specific time.</p>
   */
  @Override
  public boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    try {
      LocalDateTime dateTime = parseDateTime(matcher.group(1));
      boolean isBusy = model.isBusy(dateTime);
      view.displayBusyStatus(isBusy);
    } catch (Exception e) {
      view.displayError("Error showing status: " + e.getMessage());
    }
    return true;
  }
}
