package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.controller.Token;
import calendar.model.Date;
import calendar.model.Day;
import calendar.model.IntCalendar;
import calendar.model.IntCalendarManager;
import calendar.model.Time;
import calendar.view.IntView;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the "create" command for creating events and event series.
 */
public class CreateCommand implements IntCommand {
  private final IntView out;
  private final IntCalendarManager calendarManager;

  /**
   * Constructs a Create handler.
   *
   * @param out             the output view
   * @param calendarManager the calendarManager to use
   * @throws IllegalArgumentException if out or calendar is null
   */
  public CreateCommand(IntView out, IntCalendarManager calendarManager) {
    this.out = Objects.requireNonNull(out);
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Processes the create command.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  public void go(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord) {
      case "event":
        if (calendar == null) {
          out.writeln("Must set a calendar first");
          return;
        }
        createEvent(input.substring(firstSpace + 1), calendar);
        break;
      case "calendar":
        createCalendar(input.substring(firstSpace + 1));
        break;
      default:
        throw new UnsupportedOperationException("create " + currentWord + " is not supported");
    }
  }

  /**
   * Parses through input given the last token read was "event" to determine what the eventSubject
   * is and what the next step is.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void createEvent(String input, IntCalendar calendar) {
    Parser.EventSubjectPair pair = Parser.extractEventSubject(input);
    String eventSubject = pair.eventSubject;
    input = input.substring(pair.stringLength + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord) {
      case "from":
        createEventFromTo(input.substring(firstSpace + 1), eventSubject, calendar);
        break;
      case "on":
        createEventOn(input.substring(firstSpace + 1), eventSubject, calendar);
        break;
      default:
        throw new UnsupportedOperationException("create event " + eventSubject + " " + currentWord
            + " is not supported");
    }
  }

  /**
   * Parses through input given the last token read was "from" to determine what the fromDate,
   * fromTime, toDate, and toTime are and then if the input is empty after the
   * dateStringTtimeStrings are extracted, ask the calendar to create the event. If it isn't empty,
   * it must be a repeating event so passs the information forward to that method.
   *
   * @param input        the command input
   * @param calendar     the calendar to operate on
   * @param eventSubject the subject for the event
   */
  private void createEventFromTo(String input, String eventSubject, IntCalendar calendar) {
    final Date fromDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    final Time fromTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    Date toDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    Time toTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH);

    if (input.isEmpty()) {
      try {
        calendar.createEvent(eventSubject, fromDate, fromTime, toDate, toTime);
        out.writeln("Created event");
      } catch (IllegalArgumentException e) {
        out.writeln(e.getMessage());
      }

    } else {
      input = input.substring(1);
      input = input.substring(Parser.getFirstSpaceIndex(input) + 1);
      createEventFromToRepeats(input, eventSubject, fromDate, fromTime, toDate, toTime, calendar);
    }
  }

  /**
   * Parses through input given the last token read was "repeats" to capture the repeat days and
   * directs control flow given the next token.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param fromDate     the start date
   * @param fromTime     the start time
   * @param toDate       the end date
   * @param toTime       the end time
   * @param calendar     the calendar to operate on
   */
  private void createEventFromToRepeats(String input, String eventSubject,
                                        Date fromDate, Time fromTime, Date toDate, Time toTime,
                                        IntCalendar calendar) {
    Parser.RepeatDaysPair pair = Parser.extractRepeatDays(input);
    input = input.substring(pair.stringLength + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord) {
      case "for":
        createEventFromToRepeatsFor(input.substring(firstSpace + 1), eventSubject,
            fromDate, fromTime, toDate, toTime, pair.repeatDays, calendar);
        break;
      case "until":
        createEventFromToRepeatsUntil(input.substring(firstSpace + 1), eventSubject,
            fromDate, fromTime, toDate, toTime, pair.repeatDays, calendar);
        break;
      default:
        throw new UnsupportedOperationException("create eventFromToRepeats " + currentWord
            + " is not supported");
    }
  }

  /**
   * Parses through input given the last token read was "for" to capture the number of repetitions.
   * Calls the calendar to create the event series.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param fromDate     the start date
   * @param fromTime     the start time
   * @param toDate       the end date
   * @param toTime       the end time
   * @param repeatDays   the days to repeat the event on
   * @param calendar     the calendar to operate on
   */
  private void createEventFromToRepeatsFor(String input, String eventSubject,
                                           Date fromDate, Time fromTime, Date toDate, Time toTime,
                                           Set<Day> repeatDays, IntCalendar calendar) {
    int repetitions = Integer.parseInt(input.substring(0, Parser.getFirstSpaceIndex(input)));

    try {
      calendar.createEventSeries(eventSubject, fromDate, fromTime, toTime, repeatDays,
          repetitions);
      out.writeln("Created event series");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses through input given the last token read was "until" to capture the date to repeat until.
   * Calls the calendar to create the event series.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param fromDate     the start date
   * @param fromTime     the start time
   * @param toDate       the end date
   * @param toTime       the end time
   * @param repeatDays   the days to repeat the event on
   * @param calendar     the calendar to operate on
   */
  private void createEventFromToRepeatsUntil(String input, String eventSubject,
                                             Date fromDate, Time fromTime, Date toDate, Time toTime,
                                             Set<Day> repeatDays, IntCalendar calendar) {
    Date until = Parser.extractDate(input);

    try {
      calendar.createEventSeries(eventSubject, fromDate, fromTime, toTime, repeatDays, until);
      out.writeln("Created event series");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses through input given the last token read was "on" to determine the on date. If
   * the input is empty after the date, calls the calendar to create an all day event. Otherwise,
   * the event must repeat so that call is forwarded.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param calendar     the calendar to operate on
   */
  private void createEventOn(String input, String eventSubject, IntCalendar calendar) {
    Date on = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH);

    if (input.isEmpty()) {
      try {
        calendar.createAllDayEvent(eventSubject, on);
        out.writeln("Created event");
      } catch (IllegalArgumentException e) {
        out.writeln(e.getMessage());
      }
    } else {
      input = input.substring(1);
      createEventOnRepeats(input.substring(Parser.getFirstSpaceIndex(input) + 1),
          eventSubject, on, calendar);
    }
  }

  /**
   * Parses through the input given the last token read was "repeats" to determine the days to
   * repeat on. Determines what the next token after repeatDays is and forwards the information to
   * the appropriate method.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param on           the date the event is on
   * @param calendar     the calendar to operate on
   */
  private void createEventOnRepeats(String input, String eventSubject, Date on,
                                    IntCalendar calendar) {
    Parser.RepeatDaysPair pair = Parser.extractRepeatDays(input);
    input = input.substring(pair.stringLength + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord) {
      case "for":
        createEventOnRepeatsFor(input.substring(firstSpace + 1), eventSubject, on,
            pair.repeatDays, calendar);
        break;
      case "until":
        createEventOnRepeatsUntil(input.substring(firstSpace + 1), eventSubject, on,
            pair.repeatDays, calendar);
        break;
      default:
        throw new UnsupportedOperationException("create eventFromToRepeats " + currentWord
            + " is not supported");
    }
  }

  /**
   * Parses through the input given the last token read was "for" to determine the number of
   * repetitions. Calls the calendar to create the event series.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param on           the date the event is on
   * @param repeatDays   the days to repeat the event on
   * @param calendar     the calendar to operate on
   */
  private void createEventOnRepeatsFor(String input, String eventSubject, Date on,
                                       Set<Day> repeatDays, IntCalendar calendar) {
    int repetitions = Integer.parseInt(input.substring(0, Parser.getFirstSpaceIndex(input)));

    try {
      calendar.createEventSeries(eventSubject, on, Time.ALL_DAY_EVENT_START, Time.ALL_DAY_EVENT_END,
          repeatDays, repetitions);
      out.writeln("Created event series");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  /**
   * Parses through the input given the last token read was "until" to determine until date for
   * repetitions. Calls the calendar to create the event series.
   *
   * @param input        the command input
   * @param eventSubject the subject for the event
   * @param on           the date the event is on
   * @param repeatDays   the days to repeat the event on
   * @param calendar     the calendar to operate on
   */
  private void createEventOnRepeatsUntil(String input, String eventSubject, Date on,
                                         Set<Day> repeatDays, IntCalendar calendar) {
    Date until = Parser.extractDate(input);

    try {
      calendar.createEventSeries(eventSubject, on, Time.ALL_DAY_EVENT_START, Time.ALL_DAY_EVENT_END,
          repeatDays, until);
      out.writeln("Created event series");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }

  private void createCalendar(String input) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--name")) {
      throw new IllegalArgumentException("Expected token \"--name\" after tokens"
          + " \"create calendar\" but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);
    Parser.TokenPair namePair = Parser.extractToken(input);
    input = input.substring(namePair.length + 1);

    firstSpace = Parser.getFirstSpaceIndex(input);
    currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--timezone")) {
      throw new IllegalArgumentException("Expected token \"--timezone\" after tokens "
          + "\"create calendar " + namePair.token + "\"" + " but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    String timezoneString = input.trim();
    ZoneId zoneId;

    try {
      zoneId = ZoneId.of(timezoneString);
    } catch (Exception e) {
      out.writeln("Invalid timezone: " + timezoneString
          + ". Please use a valid timezone ID (e.g., America/New_York, Europe/London, UTC)");
      return;
    }

    try {
      calendarManager.createCalendar(namePair.token, zoneId);
      out.writeln("Calendar created");
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }
}
