package calendar.controller;

import calendar.model.CalendarDatabaseModel;
import calendar.model.CalendarProperty;
import java.io.StringReader;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a new edit command with calendar database model. Edits the calendar properties or
 * delegates editing events responsibility to the old edit command.
 */
public class EditWithDatabaseCommand implements Command {
  private final CalendarDatabaseModel database;

  /**
   * Constructs the new edit command given the calendar database model.
   *
   * @param database the database of calendars.
   * @throws NullPointerException if the database is null.
   */
  public EditWithDatabaseCommand(CalendarDatabaseModel database) {
    this.database = Objects.requireNonNull(database);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    Matcher editCalendarMatch = editCalendarMatcher(command);
    if (editCalendarMatch.matches()) {
      editCalendar(editCalendarMatch);
    } else {
      Scanner sc =  new Scanner(new StringReader(command));
      new EditCommand(this.database.getCurrCalendarModel()).execute(sc);
    }
  }

  private Matcher editCalendarMatcher(String command) {
    Pattern editCalendar = Pattern.compile("^calendar\\s+--name\\s+(?:\"([^\"]+)\"|(\\S+))"
        + "\\s+--property\\s+(\\S+)\\s+(?:\"([^\"]+)\"|(\\S+))\\s*$");
    return editCalendar.matcher(command);
  }

  private void editCalendar(Matcher editCalendarMatcher) {
    String calName = calNameHelper(editCalendarMatcher.group(1), editCalendarMatcher.group(2));
    CalendarProperty property = createCalendarProperty(editCalendarMatcher.group(3));
    String newValue = calNameHelper(editCalendarMatcher.group(4), editCalendarMatcher.group(5));
    this.database.editCalendar(calName, property, newValue);
  }

  private CalendarProperty createCalendarProperty(String property) {
    try {
      return CalendarProperty.valueOf(property.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Invalid calendar property");
    }
  }

  private String calNameHelper(String moreWords, String oneWord) {
    String subject;
    if (moreWords != null) {
      subject = moreWords;
    } else {
      subject = oneWord;
    }
    return subject;
  }
}
