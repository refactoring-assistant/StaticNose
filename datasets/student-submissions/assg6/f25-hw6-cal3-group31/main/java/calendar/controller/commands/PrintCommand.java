package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.controller.Token;
import calendar.model.Date;
import calendar.model.IntCalendar;
import calendar.model.IntEvent;
import calendar.model.Time;
import calendar.view.IntView;
import java.util.List;
import java.util.Objects;

/**
 * Handles the "print" command for displaying events.
 */
public class PrintCommand implements IntCommand {
  private final IntView out;

  /**
   * Constructs a Print handler.
   *
   * @param out the output view
   * @throws IllegalArgumentException if out or calendar is null
   */
  public PrintCommand(IntView out) {
    this.out = Objects.requireNonNull(out);
  }

  /**
   * Processes the print command.
   *
   * @param input    the command
   * @param calendar the calendar to operate on
   *
   */
  public void go(String input, IntCalendar calendar) {
    if (calendar == null) {
      out.writeln("Must set a calendar first");
      return;
    }

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    switch (currentWord) {
      case "on":
        printEventsOn(input.substring(firstSpace + 1), calendar);
        break;
      case "from":
        printEventsFromTo(input.substring(firstSpace + 1), calendar);
        break;
      default:
        throw new UnsupportedOperationException("print events " + currentWord
            + " is not supported");
    }
  }

  /**
   * Parses the input given the last token read was "on" to determine the on date. Calls out to
   * print the event details.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void printEventsOn(String input, IntCalendar calendar) {
    Date date = Parser.extractDate(input);

    List<IntEvent> events = calendar.getEventsOnDate(date);
    if (events.isEmpty()) {
      out.writeln("No events found");
    } else {
      out.writeln("Events on " + date + ":");
      for (IntEvent event : events) {
        out.writeln("* " + event.getSubject() + ", " + event.getStartTime() + ", "
            + event.getEndTime() + ", " + event.getLocation());
      }
    }
  }

  /**
   * Parses the input given the last token read was "from" to determine the fromDate, fromTime,
   * toDate, and toTime. Calls out to print the event details.
   *
   * @param input    the command input
   * @param calendar the calendar to operate on
   */
  private void printEventsFromTo(String input, IntCalendar calendar) {
    final Date fromDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    final Time fromTime = Parser.extractTime(input);
    input = input.substring(Token.TIME_LENGTH + 1);

    input = input.substring(Parser.getFirstSpaceIndex(input) + 1);

    final Date toDate = Parser.extractDate(input);
    input = input.substring(Token.DATE_LENGTH + 1);
    final Time toTime = Parser.extractTime(input);

    List<IntEvent> events = calendar.getEventsInRange(fromDate, fromTime, toDate, toTime);
    if (events.isEmpty()) {
      out.writeln("No events found");
    } else {
      out.writeln("Events from " + fromDate + " " + fromTime
          + " to " + toDate + " " + toTime + ": ");
      for (IntEvent event : events) {
        out.writeln("* " + event.getSubject() + ", " + event.getStartTime() + ", "
            + event.getEndTime() + ", " + event.getLocation());
      }
    }
  }
}
