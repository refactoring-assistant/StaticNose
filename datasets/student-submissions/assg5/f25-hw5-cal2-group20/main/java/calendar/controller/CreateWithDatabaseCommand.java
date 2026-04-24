package calendar.controller;

import calendar.model.CalendarDatabaseModel;
import java.io.StringReader;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a new create command that considers calendar database model. If it's a create event
 * command, then delegates responsibility to the old create command implementation.
 */
public class CreateWithDatabaseCommand implements Command {
  private final CalendarDatabaseModel database;

  /**
   * Constructs the new create command given the current calendar database.
   *
   * @param database the current calendar database.
   * @throws NullPointerException if the database is null.
   */
  public CreateWithDatabaseCommand(CalendarDatabaseModel database) {
    this.database = Objects.requireNonNull(database);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    String[] words = command.split("\\s+", 2);
    String secondKeyword = words[0]; // event or calendar
    if (secondKeyword.equalsIgnoreCase("calendar")) {
      this.calendarKeywordHelper(words[1]);
    } else if (secondKeyword.equalsIgnoreCase("event")) { // delegate to create event
      Scanner sc = new Scanner(new StringReader(command));
      new CreateCommand(this.database.getCurrCalendarModel()).execute(sc);
    } else {
      throw new IllegalStateException("Invalid command. Should be: create calendar "
          + "or create event");
    }
  }

  private void calendarKeywordHelper(String command) {
    Pattern createCalendarPattern = Pattern.compile(
        "^--name\\s+(?:\"([^\"]+)\"|(\\S+))\\s+--timezone\\s+([A-Za-z]+(?:[_-][A-Za-z]+)*"
            + "(?:/[A-Za-z]+(?:[_-][A-Za-z]+)*)+)$", Pattern.CASE_INSENSITIVE);
    Matcher groups =  createCalendarPattern.matcher(command);
    if (groups.matches()) {
      String calName = groups.group(1) != null ? groups.group(1) : groups.group(2);
      String timezone = groups.group(3);
      this.database.createCalendar(calName, timezone);
    } else {
      throw new IllegalStateException("Invalid command. Should be: create calendar --name "
          + "<calName> --timezone area/location");
    }
  }
}
