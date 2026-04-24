package calendar.controller;

import calendar.model.CalendarModel;
import calendar.model.EventObject;
import calendar.view.CalendarView;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Print Events command that shows a bullet point lists with events planned on specific
 * datetime or time interval.
 */
public class PrintEventsCommand implements Command {
  private final CalendarModel model;
  private final CalendarView view;

  /**
   * Constructs a print events command, given the current calendar model and the view.
   *
   * @param model the calendar model to print.
   * @param view the calendar view to print to.
   */
  public PrintEventsCommand(CalendarModel model, CalendarView view) {
    if (model == null) {
      throw new IllegalStateException("No Calendar in use to execute print");
    } else {
      this.model = model;
    }
    this.view = Objects.requireNonNull(view);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    Pattern on =
        Pattern.compile("^events\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})$", Pattern.CASE_INSENSITIVE);
    Matcher onMatcher = on.matcher(command);
    if (onMatcher.matches()) {
      this.onCommand(onMatcher, model);
      return;
    }
    Pattern from = Pattern.compile(
        "^events\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+to\\s+(\\d{4}-\\d{2}-"
            + "\\d{2}T\\d{2}:\\d{2})$", Pattern.CASE_INSENSITIVE);
    Matcher fromMatcher = from.matcher(command);
    if (fromMatcher.matches()) {
      this.fromCommand(fromMatcher, model);
      return;
    }
    throw new IllegalStateException("Invalid command. Should be a print command");
  }

  private void onCommand(Matcher onMatcher, CalendarModel model) {
    String dateString = onMatcher.group(1); // getting the datestring
    List<EventObject> events = model.getEventsBetween(dateString, "");
    String eventString = model.eventsForList(events);
    view.printEvents(eventString);
  }

  private void fromCommand(Matcher fromMatcher, CalendarModel model) {
    String startString = fromMatcher.group(1); // getting the start datestring
    String endString = fromMatcher.group(2); // getting the end datestring
    List<EventObject> events = model.getEventsBetween(startString, endString);
    String eventString = model.eventsForList(events);
    view.printEvents(eventString);
  }
}
