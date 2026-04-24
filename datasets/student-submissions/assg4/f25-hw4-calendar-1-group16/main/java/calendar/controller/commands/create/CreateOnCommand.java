package calendar.controller.commands.create;

import calendar.controller.commands.CommandUtils;
import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;

/**
 * Command: create event {@code subject} on {@code date}.
 */
public class CreateOnCommand implements Icommands {

  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for {@code createOn}.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups
   */
  public CreateOnCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IOException {
    String subject = CommandUtils.extractSubject(matcher);
    LocalDate date = LocalDate.parse(matcher.group(3));

    model.createOn(subject, date);
    view.displayMessage("All-Day event created: " + subject);
  }
}
