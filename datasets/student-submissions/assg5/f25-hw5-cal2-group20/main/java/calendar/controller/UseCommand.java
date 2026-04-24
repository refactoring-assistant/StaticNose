package calendar.controller;

import calendar.model.CalendarDatabaseModel;
import calendar.view.CalendarView;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a command class for use calendar command. Users can use this command to set the
 * calendar context.
 */
public class UseCommand implements Command {
  private final CalendarView view;
  private final CalendarDatabaseModel database;

  /**
   * Constructs a use calendar command given the calendar view and calendar database model.
   *
   * @param view the calendar view.
   * @param database the database model.
   * @throws NullPointerException if any of args is null.
   */
  public UseCommand(CalendarView view, CalendarDatabaseModel database) {
    this.view = Objects.requireNonNull(view);
    this.database = Objects.requireNonNull(database);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    Pattern p = Pattern.compile("^calendar\\s+--name\\s+(?:\"([^\"]+)\"|(\\S+))$",
        Pattern.CASE_INSENSITIVE);
    Matcher match = p.matcher(command);
    if (match.matches()) {
      String calName = match.group(1) != null ? match.group(1) : match.group(2);
      try {
        this.database.useCalendar(calName);
        view.renderMessage("Set active calendar to " + useDoubleQuotes(calName));
      } catch (IllegalArgumentException e) {
        view.renderMessage(e.getMessage());
      }
    } else {
      throw new IllegalStateException(
        "Invalid command. Should be: use calendar --name <name-of-calendar>");
    }
  }

  private String useDoubleQuotes(String calName) {
    return calName.split("\\s+").length > 1 ? "\"" + calName + "\"" : calName;
  }
}
