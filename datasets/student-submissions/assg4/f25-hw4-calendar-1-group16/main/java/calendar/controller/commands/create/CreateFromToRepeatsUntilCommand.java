package calendar.controller.commands.create;

import calendar.controller.commands.CommandUtils;
import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * Command: create event {@code subject} from {@code start} to {@code end} repeats {@code weekdays}
 * until {@code date}.
 */
public class CreateFromToRepeatsUntilCommand implements Icommands {

  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for {@code createFromToRepeatUntil}.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   */
  public CreateFromToRepeatsUntilCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    String subject = CommandUtils.extractSubject(matcher);
    LocalDateTime start = LocalDateTime.parse(matcher.group(3));
    LocalDateTime end = LocalDateTime.parse(matcher.group(4));
    String weekdays = matcher.group(5).toUpperCase();
    LocalDate until = LocalDate.parse(matcher.group(6));

    model.createFromToRepeatsUntil(subject, start, end, weekdays, until);
    view.displayMessage("Event series created: " + subject + " (repeats until " + until + ")");
  }
}
