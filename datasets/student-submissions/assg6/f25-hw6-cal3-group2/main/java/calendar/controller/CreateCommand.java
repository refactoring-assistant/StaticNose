package calendar.controller;

import calendar.event.EventContext;
import calendar.model.CalendarInterface;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the process of event creation. Use regex to match creation pattern and
 * create event or event series with given property values.
 */
public class CreateCommand extends CommandFactory {

  private static final Pattern C_FROM_TO_REPEATS_N = Pattern.compile(
      "^create event (?:(\\S+)|\"([^\"]*)\") from (\\S+) to (\\S+) "
          + "repeats (\\S+) for (\\d+) times$");

  private static final Pattern C_FROM_TO_REPEATS_UNTIL = Pattern.compile(
      "^create event (?:(\\S+)|\"([^\"]*)\") from (\\S+) to (\\S+) "
          + "repeats (\\S+) until (\\S+)$");

  private static final Pattern C_FROM_TO = Pattern.compile(
      "^create event (?:(\\S+)|\"([^\"]*)\") from (\\S+) to (\\S+)$");

  private static final Pattern C_ON_REPEATS_N = Pattern.compile(
      "^create event (?:(\\S+)|\"([^\"]*)\") on (\\S+) repeats (\\S+) for (\\d+) times$");

  private static final Pattern C_ON_REPEATS_UNTIL = Pattern.compile(
      "^create event (?:(\\S+)|\"([^\"]*)\") on (\\S+) repeats (\\S+) until (\\S+)$");

  private static final Pattern C_ON = Pattern.compile(
      "^create event (?:(\\S+)|\"([^\"]*)\") on (\\S+)$");

  private final CalendarInterface calendar;
  private final String commandLine;

  /**
   * Constructor for CreateCommand Class.
   *
   * @param commandLine the input line of a command
   * @param calendar    the calendar object
   */
  public CreateCommand(String commandLine, CalendarInterface calendar) {
    this.calendar = calendar;
    this.commandLine = commandLine;
  }

  @Override
  public void execute() throws IllegalArgumentException {
    parseCommand(this.commandLine);
  }

  @Override
  public void parseCommand(String commandLine) throws IllegalArgumentException {
    Matcher matcher;

    matcher = C_FROM_TO_REPEATS_N.matcher(commandLine);
    if (matcher.matches()) {
      subject = ((matcher.group(1) != null) ? matcher.group(1) : matcher.group(2)).trim();
      startDateTime = matcher.group(3).trim();
      endDateTime = matcher.group(4).trim();
      weekdays = matcher.group(5).trim();
      repeatTimes = Integer.parseInt(matcher.group(6));

      EventContext context = new EventContext(subject, startDateTime, endDateTime,
          description, location, eventStatus);
      this.calendar.createEventSeries(context, weekdays, repeatTimes, null);
      return;
    }

    matcher = C_FROM_TO_REPEATS_UNTIL.matcher(commandLine);
    if (matcher.matches()) {
      subject = ((matcher.group(1) != null) ? matcher.group(1) : matcher.group(2)).trim();
      startDateTime = matcher.group(3).trim();
      endDateTime = matcher.group(4).trim();
      weekdays = matcher.group(5).trim();
      seriesEndDateTime = matcher.group(6).trim();

      EventContext context = new EventContext(subject, startDateTime, endDateTime,
          description, location, eventStatus);
      this.calendar.createEventSeries(context, weekdays, 0, seriesEndDateTime);
      return;
    }

    matcher = C_FROM_TO.matcher(commandLine);
    if (matcher.matches()) {
      subject = ((matcher.group(1) != null) ? matcher.group(1) : matcher.group(2)).trim();
      startDateTime = matcher.group(3).trim();
      endDateTime = matcher.group(4).trim();

      EventContext context = new EventContext(subject, startDateTime, endDateTime,
          description, location, eventStatus);
      this.calendar.createSingleEvent(context, null);
      return;
    }

    matcher = C_ON_REPEATS_N.matcher(commandLine);
    if (matcher.matches()) {
      subject = ((matcher.group(1) != null) ? matcher.group(1) : matcher.group(2)).trim();
      startDateTime = matcher.group(3).trim();
      weekdays = matcher.group(4).trim();
      repeatTimes = Integer.parseInt(matcher.group(5));

      EventContext context = new EventContext(subject, startDateTime, null,
          description, location, eventStatus);
      this.calendar.createEventSeries(context, weekdays, repeatTimes, null);
      return;
    }

    matcher = C_ON_REPEATS_UNTIL.matcher(commandLine);
    if (matcher.matches()) {
      subject = ((matcher.group(1) != null) ? matcher.group(1) : matcher.group(2)).trim();
      startDateTime = matcher.group(3).trim();
      weekdays = matcher.group(4).trim();
      seriesEndDateTime = matcher.group(5).trim();

      EventContext context = new EventContext(subject, startDateTime, null,
          description, location, eventStatus);
      this.calendar.createEventSeries(context, weekdays, 0, seriesEndDateTime);
      return;
    }

    matcher = C_ON.matcher(commandLine);
    if (matcher.matches()) {
      subject = ((matcher.group(1) != null) ? matcher.group(1) : matcher.group(2)).trim();
      startDateTime = matcher.group(3).trim();
      EventContext context = new EventContext(subject, startDateTime, null,
          description, location, eventStatus);
      this.calendar.createSingleEvent(context, null);
      return;
    }
    throw new IllegalArgumentException("Create event failure. Wrong format: " + commandLine);
  }

}
