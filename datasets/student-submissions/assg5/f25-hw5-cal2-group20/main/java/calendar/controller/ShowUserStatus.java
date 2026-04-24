package calendar.controller;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Show Status command that shows user status, provided a string date time.
 */
public class ShowUserStatus implements Command {
  private final CalendarModel model;
  private final CalendarView view;

  /**
   * Constructs a command show user status on a specific date and time, given the view.
   *
   * @param view the view of the calendar.
   */
  public ShowUserStatus(CalendarModel model, CalendarView view) {
    if (model == null) {
      throw new IllegalStateException("No Calendar in use to execute show status");
    } else {
      this.model = model;
    }
    this.view = Objects.requireNonNull(view);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    Pattern p = Pattern.compile("^status\\s+on\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$",
        Pattern.CASE_INSENSITIVE);
    Matcher match = p.matcher(command);
    if (match.matches()) {
      String dateTimeString = match.group(1); // getting the datetimestring
      String eventString = model.getUserStatus(dateTimeString);
      view.showUserStatus(eventString);
    } else {
      throw new IllegalStateException(
        "Invalid command. Should be: show status on <dateStringTtimeString>");
    }
  }
}
