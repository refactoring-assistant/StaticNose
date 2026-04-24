package calendar.controller.commands.utils;

import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * Command: show status at {@code datetime}.
 */
public class ShowStatusCommand implements Icommands {

  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for showing the status of the events.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   *
   */
  public ShowStatusCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  /**
   * Calls the model's method for this command and passes the proper data.
   *
   * @param model the calendar model to operate on
   * @throws IllegalArgumentException if the values inputted are invalid
   * @throws IOException if an IOException occurs
   */

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1));
    boolean isBusy = model.showStatus(dateTime);

    if (isBusy) {
      view.displayMessage("Busy at " + dateTime);
    } else {
      view.displayMessage("Available at " + dateTime);
    }
  }
}
