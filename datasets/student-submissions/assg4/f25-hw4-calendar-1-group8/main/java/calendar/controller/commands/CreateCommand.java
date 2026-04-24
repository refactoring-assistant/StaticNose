package calendar.controller.commands;

import calendar.controller.utils.CommandParserUtils;
import calendar.model.CalendarEvent;
import calendar.model.InterfaceCalendarModel;
import calendar.model.RecurrenceRule;
import calendar.model.SeriesMaster;
import calendar.view.InterfaceCalendarView;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Command to handle creating new events.
 */
public class CreateCommand implements CommandInterface {

  private final CommandParserUtils utils;

  /**
   * Constructs a CreateCommand with the given utility parser.
   *
   * @param utils the command parser utilities
   */
  public CreateCommand(CommandParserUtils utils) {
    this.utils = utils;
  }

  /**
   * Executes the create command to add a new event or event series to the calendar.
   *
   * @param model the calendar model
   * @param view  the calendar view
   * @param args  command arguments after "create"
   * @throws Exception if the command is malformed or invalid
   */
  @Override
  public void execute(InterfaceCalendarModel model, InterfaceCalendarView view, List<String> args)
      throws Exception {
    if (args.size() < 4 || !args.get(0).equals("event")) {
      throw new Exception("Invalid 'create' command structure. Command is too short.");
    }

    String subject = utils.stripQuotes(args.get(1));
    String type = args.get(2).toLowerCase();
    List<String> subArgs = args.subList(3, args.size());

    switch (type) {
      case "on":
        parseOnEvent(model, subject, subArgs);
        break;
      case "from":
        parseFromEvent(model, subject, subArgs);
        break;
      default:
        throw new Exception(
            "Invalid 'create' command. Must be '... on <date>' or '... from <start>'.");
    }

    view.displayMessage("Event created successfully.");
  }

  /**
   * Parses an all-day event command: {@code ... on <date> [repeats ...]}.
   *
   * @param model   the calendar model
   * @param subject the event subject
   * @param args    arguments after "on"
   * @throws Exception if parsing fails
   */
  private void parseOnEvent(InterfaceCalendarModel model, String subject, List<String> args)
      throws Exception {
    if (args.isEmpty()) {
      throw new Exception("Missing date. Usage: ... on <dateString>");
    }

    ZonedDateTime start = utils.parseDateToZonedDateTime(args.get(0));

    if (args.size() == 1) {
      CalendarEvent event = new CalendarEvent.CalendarEventBuilder(subject, start).build();
      model.addEvent(event);
      return;
    }

    if (!args.get(1).equals("repeats")) {
      throw new Exception("Invalid command structure. Expected 'repeats'.");
    }
    if (args.size() < 5) {
      throw new Exception("Invalid 'repeats' command. Command is too short.");
    }

    Set<DayOfWeek> weekdays = utils.parseWeekdays(args.get(2));
    String repeatType = args.get(3).toLowerCase();
    String repeatValue = args.get(4);
    RecurrenceRule.Builder ruleBuilder = new RecurrenceRule.Builder(weekdays);

    if (repeatType.equals("for")) {
      if (args.size() != 6 || !args.get(5).equalsIgnoreCase("times")) {
        throw new Exception(
            "Invalid command. Usage: ... repeats <weekdays> for <N> times");
      }
      ruleBuilder.repeatsFor(utils.parseOccurrences(repeatValue));
    } else if (repeatType.equals("until")) {
      if (args.size() != 5) {
        throw new Exception(
            "Invalid command. Usage: ... repeats <weekdays> until <dateString>");
      }
      ruleBuilder.repeatsUntil(utils.parseDate(repeatValue));
    } else {
      throw new Exception("Invalid repeat type: " + repeatType + ". Must be 'for' or 'until'.");
    }

    CalendarEvent templateEvent = new CalendarEvent.CalendarEventBuilder(subject, start).build();
    RecurrenceRule rule = ruleBuilder.build();
    SeriesMaster master = new SeriesMaster(templateEvent, rule);
    model.addEventSeries(master);
  }

  /**
   * Parses a timed event command: {@code ... from <start> to <end> [repeats ...]}.
   *
   * @param model   the calendar model
   * @param subject the event subject
   * @param args    arguments after "from"
   * @throws Exception if parsing fails
   */
  private void parseFromEvent(InterfaceCalendarModel model, String subject, List<String> args)
      throws Exception {
    if (args.size() < 3) {
      throw new Exception("Invalid 'create from' command. Usage: ... from <start> to <end>");
    }

    ZonedDateTime start = utils.parseDateTimeToZonedDateTime(args.get(0));
    if (!args.get(1).equals("to")) {
      throw new Exception("Invalid command. Expected 'to' keyword after start time.");
    }
    ZonedDateTime end = utils.parseDateTimeToZonedDateTime(args.get(2));

    if (args.size() == 3) {
      CalendarEvent event = new CalendarEvent.CalendarEventBuilder(subject, start)
          .withEnd(end)
          .build();
      model.addEvent(event);
      return;
    }

    if (!args.get(3).equals("repeats")) {
      throw new Exception("Invalid command structure. Expected 'repeats'.");
    }
    if (args.size() < 7) {
      throw new Exception("Invalid 'repeats' command. Command is too short.");
    }

    Set<DayOfWeek> weekdays = utils.parseWeekdays(args.get(4));
    String repeatType = args.get(5).toLowerCase();
    String repeatValue = args.get(6);
    RecurrenceRule.Builder ruleBuilder = new RecurrenceRule.Builder(weekdays);

    if (repeatType.equals("for")) {
      if (args.size() != 8 || !args.get(7).equalsIgnoreCase("times")) {
        throw new Exception(
            "Invalid command. Usage: ... repeats <weekdays> for <N> times");
      }
      ruleBuilder.repeatsFor(utils.parseOccurrences(repeatValue));
    } else if (repeatType.equals("until")) {
      if (args.size() != 7) {
        throw new Exception(
            "Invalid command. Usage: ... repeats <weekdays> until <dateString>");
      }
      ruleBuilder.repeatsUntil(utils.parseDate(repeatValue));
    } else {
      throw new Exception("Invalid repeat type: " + repeatType + ". Must be 'for' or 'until'.");
    }

    CalendarEvent templateEvent = new CalendarEvent.CalendarEventBuilder(subject, start)
        .withEnd(end)
        .build();
    RecurrenceRule rule = ruleBuilder.build();
    SeriesMaster master = new SeriesMaster(templateEvent, rule);
    model.addEventSeries(master);
  }
}