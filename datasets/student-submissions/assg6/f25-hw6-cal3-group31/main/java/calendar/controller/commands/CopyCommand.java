package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.controller.Token;
import calendar.model.Date;
import calendar.model.IntCalendar;
import calendar.model.IntCalendarManager;
import calendar.model.Time;
import calendar.view.IntView;
import java.util.Objects;

/**
 * Handles the "copy" command for creating events and event series.
 */
public class CopyCommand implements IntCommand {
  private final IntView out;
  private final IntCalendarManager calendarManager;

  /**
   * Constructs a Copy handler.
   *
   * @param out             the output view
   * @param calendarManager the calendarManager to use
   */
  public CopyCommand(IntView out, IntCalendarManager calendarManager) {
    this.out = Objects.requireNonNull(out);
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Processes the copy command.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  public void go(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord.toLowerCase()) {
      case "event":
        if (calendar == null) {
          out.writeln("Must set a calendar first");
          return;
        }
        copyEvent(input.substring(firstSpace + 1), calendar);
        break;
      case "events":
        if (calendar == null) {
          out.writeln("Must set a calendar first");
          return;
        }
        copyEvents(input.substring(firstSpace + 1), calendar);
        break;
      default:
        throw new UnsupportedOperationException("copy " + currentWord + " is not supported");
    }
  }

  /**
   * Parses through input given the last token read was "event" to copy an event from the current
   * calendar to a target calendar.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void copyEvent(String input, IntCalendar calendar) {
    Parser.EventSubjectPair pair = Parser.extractEventSubject(input);
    String eventSubject = pair.eventSubject;
    input = input.substring(pair.stringLength + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("on")) {
      throw new IllegalArgumentException("Expected token \"on\" after tokens \"copy event\""
          + " but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    final Date date = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    final Time time = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--target")) {
      throw new IllegalArgumentException("Expected token \"--target\" after tokens \"copy event on "
          + date + time + "\" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    Parser.TokenPair targetCalendarNamePair = Parser.extractToken(input);
    input = input.substring(targetCalendarNamePair.length + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("to")) {
      throw new IllegalArgumentException("Expected token \"to\" after tokens \"copy event on "
          + date + time + targetCalendarNamePair.token
          + "\" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    final Date newDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    final Time newTime = Parser.extractTime(input);

    try {
      IntCalendar targetCalendar = calendarManager.getCalendar(targetCalendarNamePair.token);
      try {
        calendar.copyEventTo(eventSubject, date, time, targetCalendar, newDate, newTime);
        out.writeln("Copied event to " + targetCalendar.getName());
      } catch (IllegalArgumentException e) {
        out.writeln(e.getMessage());
      }
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses through input given the last token read was "events" to copy all events scheduled
   * on a particular day from the current calendar to a target calendar.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void copyEvents(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord.toLowerCase()) {
      case "on":
        copyEventsOn(input.substring(firstSpace + 1), calendar);
        break;
      case "between":
        copyEventsBetween(input.substring(firstSpace + 1), calendar);
        break;
      default:
        throw new UnsupportedOperationException("copy events " + currentWord + " is not supported");
    }
  }

  /**
   * Parses through input given the last token read was "on".
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void copyEventsOn(String input, IntCalendar calendar) {
    final Date date = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--target")) {
      throw new IllegalArgumentException("Expected token \"--target\" after "
          + "tokens \"copy events on " + date + "\" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    Parser.TokenPair targetCalendarNamePair = Parser.extractToken(input);
    input = input.substring(targetCalendarNamePair.length + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("to")) {
      throw new IllegalArgumentException("Expected token \"to\" after tokens \"copy events on "
          + date + targetCalendarNamePair.token
          + "\" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    final Date newDate = Parser.extractDate(input);

    try {
      IntCalendar targetCalendar = calendarManager.getCalendar(targetCalendarNamePair.token);
      try {
        calendar.copyEventsOnDateTo(date, targetCalendar, newDate);
        out.writeln("Copied events on " + date + " to " + targetCalendar.getName());
      } catch (IllegalArgumentException e) {
        out.writeln(e.getMessage());
      }
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses through input given the last token read was "between".
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void copyEventsBetween(String input, IntCalendar calendar) {
    final Date startDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("and")) {
      throw new IllegalArgumentException("Expected token \"and\" after "
          + "tokens \"copy events between " + startDate + "\" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    final Date endDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--target")) {
      throw new IllegalArgumentException("Expected token \"--target\" after "
          + "tokens \"copy events between " + startDate + " and " + endDate + " \" but was: "
          + currentWord);
    }
    input = input.substring(firstSpace + 1);

    Parser.TokenPair targetCalendarNamePair = Parser.extractToken(input);
    input = input.substring(targetCalendarNamePair.length + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("to")) {
      throw new IllegalArgumentException("Expected token \"to\" after "
          + "tokens \"copy events between " + startDate + " and " + endDate + " --target "
          + targetCalendarNamePair.token + " \" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    final Date newStartDate = Parser.extractDate(input);

    try {
      IntCalendar targetCalendar = calendarManager.getCalendar(targetCalendarNamePair.token);
      try {
        calendar.copyEventsInRangeTo(startDate, endDate, targetCalendar, newStartDate);
        out.writeln("Copied events between " + startDate + " and " + endDate + " to "
            + targetCalendar.getName() + " on " + newStartDate);
      } catch (IllegalArgumentException e) {
        out.writeln(e.getMessage());
      }
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }
}
