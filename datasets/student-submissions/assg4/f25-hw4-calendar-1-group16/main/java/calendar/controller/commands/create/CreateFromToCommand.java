package calendar.controller.commands.create;

import calendar.controller.commands.CommandUtils;
import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * Command: create event {@code subject} from {@code date} to {@code end}.
 */
public class CreateFromToCommand implements Icommands {
  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for {@code createFromTo}.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups
   */
  public CreateFromToCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    String subject = CommandUtils.extractSubject(matcher);
    LocalDateTime start = LocalDateTime.parse(matcher.group(3));
    LocalDateTime end = LocalDateTime.parse(matcher.group(4));

    model.createFromTo(subject, start, end);
    view.displayMessage("Event created: " + subject);
  }

}
