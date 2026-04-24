package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.controller.Token;
import calendar.model.Date;
import calendar.model.IntCalendar;
import calendar.model.IntCalendarManager;
import calendar.model.Time;
import calendar.view.IntView;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Handles the "edit" command for modifying events and event series.
 */
public class EditCommand implements IntCommand {
  private final IntView out;
  private final IntCalendarManager calendarManager;

  /**
   * Constructs an Edit handler.
   *
   * @param out             the output view
   * @param calendarManager the calendarManager to use
   * @throws IllegalArgumentException if out or calendar is null
   */
  public EditCommand(IntView out, IntCalendarManager calendarManager) {
    this.out = Objects.requireNonNull(out);
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Processes the edit command.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  public void go(String input, IntCalendar calendar) {
    if (calendar == null) {
      out.writeln("Must set a calendar first");
      return;
    }

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord) {
      case "event":
        editEvent(input.substring(firstSpace + 1), calendar);
        break;
      case "events":
        editEvents(input.substring(firstSpace + 1), calendar);
        break;
      case "series":
        editSeries(input.substring(firstSpace + 1), calendar);
        break;
      case "calendar":
        editCalendar(input.substring(firstSpace + 1));
        break;
      default:
        throw new UnsupportedOperationException("edit " + currentWord + " is not supported");
    }
  }

  /**
   * Parses the input given the last token read was "event" to determine the property to edit,
   * the event subject, the fromDate, the fromTime, the toDate, the toTime, and the new
   * property value to call the calendar to edit the described event.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void editEvent(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    final String property = input.substring(0, firstSpace);
    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    Parser.EventSubjectPair esPair = Parser.extractEventSubject(input);
    input = input.substring(esPair.stringLength + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    final Date fromDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    final Time fromTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    final Date toDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    Time toTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    String newPropertyValue = input.trim().replaceAll("\"", "");

    try {
      calendar.editEvent(esPair.eventSubject, fromDate, fromTime, toDate, toTime,
          property, newPropertyValue);
      out.writeln("Event edited");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses the input given the last token read was "events" to determine the property to edit,
   * the event subject, the fromDate, the fromTime, and the new
   * property value to call the calendar to edit the described events.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void editEvents(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    final String property = input.substring(0, firstSpace);
    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    Parser.EventSubjectPair esPair = Parser.extractEventSubject(input);
    input = input.substring(esPair.stringLength + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    final Date fromDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    Time fromTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    String newPropertyValue = input.trim().replaceAll("\"", "");

    try {
      calendar.editEventsFromDate(esPair.eventSubject, fromDate, fromTime,
          property, newPropertyValue);
      out.writeln("Events edited");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses the input given the last token read was "series" to determine the property to edit,
   * the event subject, the fromDate, the fromTime, and the new
   * property value to call the calendar to edit the described event series.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void editSeries(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    final String property = input.substring(0, firstSpace);
    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    Parser.EventSubjectPair esPair = Parser.extractEventSubject(input);
    input = input.substring(esPair.stringLength + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    final Date fromDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    Time fromTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    String newPropertyValue = input.trim().replaceAll("\"", "");

    try {
      calendar.editSeries(esPair.eventSubject, fromDate, fromTime,
          property, newPropertyValue);
      out.writeln("Series edited");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  private void editCalendar(String input) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--name")) {
      throw new IllegalArgumentException("Expected token \"--name\" after tokens \"edit calendar\""
          + " but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);
    Parser.TokenPair namePair = Parser.extractToken(input);
    input = input.substring(namePair.length + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--property")) {
      throw new IllegalArgumentException("Expected token \"--property\" after tokens "
          + "edit calendar " + namePair.token + " but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);
    Parser.TokenPair propertyNamePair = Parser.extractToken(input);
    input = input.substring(propertyNamePair.length + 1);

    String propertyValue = Parser.extractToken(input).token;

    switch (propertyNamePair.token.toLowerCase()) {
      case "name":
        editCalendarName(namePair.token, propertyValue);
        break;
      case "timezone":
        editCalendarTimezone(namePair.token, propertyValue);
        break;
      default:
        out.writeln(propertyNamePair.token + " is not a supported property on calendar "
            + namePair.token);
    }
  }

  private void editCalendarName(String oldName, String newName) {
    try {
      calendarManager.editCalendarName(oldName, newName);
      out.writeln("Calendar name edited to: " + newName);
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  private void editCalendarTimezone(String calendarName, String zone) {
    ZoneId zoneId;

    try {
      zoneId = ZoneId.of(zone);
    } catch (Exception e) {
      out.writeln("Invalid timezone: " + zone
          + ". Please use a valid timezone ID (e.g., America/New_York, Europe/London, UTC)");
      return;
    }

    try {
      calendarManager.editCalendarTimezone(calendarName, zoneId);
      out.writeln("Calendar timezone edited to: " + zoneId);
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }
}
