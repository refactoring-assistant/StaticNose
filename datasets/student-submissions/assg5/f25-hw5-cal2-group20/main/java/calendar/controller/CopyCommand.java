package calendar.controller;

import calendar.model.CalendarDatabaseModel;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Copy command that implements Command interface. Copy specific event/events with
 * params, such as target calendar, start at the specified date/time, and other.
 */
public class CopyCommand implements Command {
  private final CalendarDatabaseModel database;

  /**
   * Constructs the copy command given the current calendar database.
   *
   * @param database the current calendar database.
   * @throws NullPointerException if the database is null.
   */
  public CopyCommand(CalendarDatabaseModel database) {
    this.database = Objects.requireNonNull(database);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    Matcher copyEventMatcher = this.copyEventMatcher(command);
    Matcher copyEventsOnMatcher = this.copyEventsOnMatcher(command);
    Matcher copyEventsBetweenMatcher = this.copyEventsBetweenMatcher(command);

    if (copyEventMatcher.matches()) {
      this.copyEvent(copyEventMatcher);
    } else if (copyEventsOnMatcher.matches()) {
      this.copyEventsOn(copyEventsOnMatcher);
    } else if (copyEventsBetweenMatcher.matches()) {
      this.copyEventsBetween(copyEventsBetweenMatcher);
    } else {
      throw new IllegalStateException("Invalid command. Should be one of copy commands.");
    }
  }

  private Matcher copyEventMatcher(String command) {
    Pattern copyEvent = Pattern.compile(
        "^event\\s+(?:\"([^\"]+)\"|(\\S+))\\s+on\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + "\\s+--target\\s+(?:\"([^\"]+)\"|(\\S+))\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:"
          + "\\d{2})$", Pattern.CASE_INSENSITIVE);
    return copyEvent.matcher(command);
  }

  private Matcher copyEventsOnMatcher(String command) {
    Pattern copyEventsOn = Pattern.compile(
        "^events\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+--target\\s+(?:\"([^\"]+)\"|(\\S+))\\s+"
          + "to\\s+(\\d{4}-\\d{2}-\\d{2})$", Pattern.CASE_INSENSITIVE);
    return copyEventsOn.matcher(command);
  }

  private Matcher copyEventsBetweenMatcher(String command) {
    Pattern copyEventsBetween = Pattern.compile(
        "^events\\s+between\\s+(\\d{4}-\\d{2}-\\d{2})\\s+and\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
          + "--target\\s+(?:\"([^\"]+)\"|(\\S+))\\s+to\\s+(\\d{4}-\\d{2}-\\d{2})$",
        Pattern.CASE_INSENSITIVE);
    return copyEventsBetween.matcher(command);
  }

  private void copyEvent(Matcher copyEventMatcher) {
    String eventName = nameHelper(copyEventMatcher.group(1), copyEventMatcher.group(2));
    String onDateTime = copyEventMatcher.group(3);
    String calendarName = nameHelper(copyEventMatcher.group(4), copyEventMatcher.group(5));
    String toDateTime = copyEventMatcher.group(6);
    this.database.copyEvent(eventName, calendarName, onDateTime, toDateTime);
  }

  private void copyEventsOn(Matcher copyEventsOnMatcher) {
    String onDateTime = copyEventsOnMatcher.group(1);
    String calendarName = nameHelper(copyEventsOnMatcher.group(2), copyEventsOnMatcher.group(3));
    String toDateTime = copyEventsOnMatcher.group(4);
    this.database.copyEvents(onDateTime, calendarName, toDateTime);
  }

  private void copyEventsBetween(Matcher copyEventsBetweenMatcher) {
    String startDate = copyEventsBetweenMatcher.group(1);
    String endDate = copyEventsBetweenMatcher.group(2);
    String calendarName = nameHelper(copyEventsBetweenMatcher.group(3),
        copyEventsBetweenMatcher.group(4));
    String toDate = copyEventsBetweenMatcher.group(5);
    this.database.copyEventsInterval(startDate, endDate, calendarName, toDate);
  }

  private String nameHelper(String moreWords, String oneWord) {
    String name;
    if (moreWords != null) {
      name = moreWords;
    } else {
      name = oneWord;
    }
    return name;
  }
}
