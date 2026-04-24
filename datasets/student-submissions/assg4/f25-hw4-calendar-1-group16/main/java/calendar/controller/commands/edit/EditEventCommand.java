package calendar.controller.commands.edit;

import calendar.controller.commands.CommandUtils;
import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * Command: edit event {@code property} {@code subject} from {@code start} to {@code end} with
 * {@code value}.
 */
public class EditEventCommand implements Icommands {
  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for {@code editEvent}.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   */
  public EditEventCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    String property = matcher.group(1);
    String subject = CommandUtils.extractSubject(matcher, 2);
    LocalDateTime start = LocalDateTime.parse(matcher.group(4));
    LocalDateTime end = LocalDateTime.parse(matcher.group(5));
    String value = matcher.group(6);
    model.editEventFromToWith(property, subject, start, end, value);
    view.displayMessage("Event updated: " + subject);
  }
}
