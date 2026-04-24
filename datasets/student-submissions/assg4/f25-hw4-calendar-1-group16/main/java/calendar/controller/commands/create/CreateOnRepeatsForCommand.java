package calendar.controller.commands.create;

import calendar.controller.commands.CommandUtils;
import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;

/**
 * Command: create event {@code subject} on {@code date} repeats {@code weekdays}
 * for {@code n times}.
 */
public class CreateOnRepeatsForCommand implements Icommands {

  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for {@code creatOnRepeatsFor}.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   */
  public CreateOnRepeatsForCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    String subject = CommandUtils.extractSubject(matcher);
    LocalDate date = LocalDate.parse(matcher.group(3));
    String weekdays = matcher.group(4).toUpperCase();
    int times = Integer.parseInt(matcher.group(5));
    model.createOnRepeatsFor(subject, date, weekdays, times);
    view.displayMessage("All-day series created: " + subject + " (" + times + " events)");
  }
}
