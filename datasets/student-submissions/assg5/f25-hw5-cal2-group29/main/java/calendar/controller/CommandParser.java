package calendar.controller;

import calendar.controller.calendar.CreateCalendarCommand;
import calendar.controller.calendar.EditCalendarCommand;
import calendar.controller.calendar.ExportCalendarCommand;
import calendar.controller.calendar.UseCalendarCommand;
import calendar.controller.event.CopyEventCommand;
import calendar.controller.event.CopyEventsBetweenCommand;
import calendar.controller.event.CopyEventsOnDateCommand;
import calendar.controller.event.CreateEventCommand;
import calendar.controller.event.EditEventCommand;
import calendar.controller.event.QueryEventsCommand;
import calendar.model.utils.EditType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The central router for the controller layer. This class is responsible for
 * parsing a raw user input string into a concrete {@link Command} object.
 */
public class CommandParser {

  private static final Pattern CREATE_CALENDAR_PATTERN = Pattern.compile(
      "create calendar --name (\\S+) --timezone (\\S+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern EDIT_CALENDAR_PATTERN = Pattern.compile(
      "edit calendar --name (\\S+) --property (name|timezone) (?:\"([^\"]*)\"|(\\S+))",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern USE_CALENDAR_PATTERN = Pattern.compile(
      "use calendar --name (\\S+)", Pattern.CASE_INSENSITIVE);

  private static final Pattern COPY_EVENT_PATTERN = Pattern.compile(
      "copy event (?:\"([^\"]+)\"|(\\S+)) on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
          + "--target (\\S+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})", Pattern.CASE_INSENSITIVE);
  private static final Pattern COPY_EVENTS_ON_DATE_PATTERN = Pattern.compile(
      "copy events on (\\d{4}-\\d{2}-\\d{2}) --target (\\S+) to (\\d{4}-\\d{2}-\\d{2})",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern COPY_EVENTS_BETWEEN_PATTERN = Pattern.compile(
      "copy events between (\\d{4}-\\d{2}-\\d{2}) and (\\d{4}-\\d{2}-\\d{2}) "
          + "--target (\\S+) to (\\d{4}-\\d{2}-\\d{2})", Pattern.CASE_INSENSITIVE);
  private static final Pattern CREATE_EVENT_PATTERN = Pattern.compile(
      "create event (?:\"([^\"]+)\"|(\\S+)) (.*)", Pattern.CASE_INSENSITIVE);
  private static final Pattern EDIT_EVENT_PATTERN = Pattern.compile(
      "edit (event|events|series) (\\w+) (?:\"([^\"]+)\"|(\\S+)) (.*)", Pattern.CASE_INSENSITIVE);
  private static final Pattern QUERY_EVENTS_PATTERN = Pattern.compile(
      "print events (on|from) (.+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern STATUS_PATTERN = Pattern.compile(
      "show status on (.+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern EXPORT_PATTERN = Pattern.compile(
      "export cal (.+)", Pattern.CASE_INSENSITIVE);

  /**
   * Parses a user input string and returns the corresponding Command.
   *
   * @param input the user input string
   * @return the parsed Command object
   * @throws IllegalArgumentException if the input is invalid or unrecognized
   */
  public Command parse(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Empty command.");
    }

    String trimmed = input.trim();
    String lowerTrimmed = trimmed.toLowerCase();

    if ("exit".equalsIgnoreCase(trimmed)) {
      return new ExitCommand();
    }


    if (lowerTrimmed.startsWith("create calendar")) {
      return parseCreateCalendar(trimmed);
    }
    if (lowerTrimmed.startsWith("edit calendar")) {
      return parseEditCalendar(trimmed);
    }
    if (lowerTrimmed.startsWith("use calendar")) {
      return parseUseCalendar(trimmed);
    }

    if (lowerTrimmed.startsWith("copy events between")) {
      return parseCopyEventsBetween(trimmed);
    }
    if (lowerTrimmed.startsWith("copy events on")) {
      return parseCopyEventsOnDate(trimmed);
    }
    if (lowerTrimmed.startsWith("copy event")) {
      return parseCopyEvent(trimmed);
    }

    if (lowerTrimmed.startsWith("create event")) {
      return parseCreateEvent(trimmed);
    }
    if (lowerTrimmed.startsWith("edit ")) {
      return parseEditEvent(trimmed);
    }
    if (lowerTrimmed.startsWith("print events")) {
      return parseQueryEvents(trimmed);
    }
    if (lowerTrimmed.startsWith("show status")) {
      return parseStatus(trimmed);
    }
    if (lowerTrimmed.startsWith("export cal")) {
      return parseExport(trimmed);
    }

    throw new IllegalArgumentException("Unknown command: " + trimmed);
  }

  private Command parseCreateCalendar(String input) {
    Matcher matcher = CREATE_CALENDAR_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid command. Use: create calendar --name <name> --timezone <area/location>");
    }
    return new CreateCalendarCommand(matcher.group(1), matcher.group(2));
  }

  private Command parseEditCalendar(String input) {
    Matcher matcher = EDIT_CALENDAR_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid command. Use: edit calendar --name <name> --property <prop> <value>");
    }
    String name = matcher.group(1);
    String property = matcher.group(2);
    String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
    return new EditCalendarCommand(name, property, value);
  }

  private Command parseUseCalendar(String input) {
    Matcher matcher = USE_CALENDAR_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid command. Use: use calendar --name <name>");
    }
    return new UseCalendarCommand(matcher.group(1));
  }


  private Command parseCopyEvent(String input) {
    Matcher matcher = COPY_EVENT_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid command. Use: copy event <subject> on <datetime> --target <cal> to <datetime>");
    }
    String subject = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    String startDateTime = matcher.group(3);
    String targetCal = matcher.group(4);
    String targetStart = matcher.group(5);
    return new CopyEventCommand(subject, startDateTime, targetCal, targetStart);
  }

  private Command parseCopyEventsOnDate(String input) {
    Matcher matcher = COPY_EVENTS_ON_DATE_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid command. Use: copy events on <date> --target <cal> to <date>");
    }
    return new CopyEventsOnDateCommand(matcher.group(1), matcher.group(2), matcher.group(3));
  }

  private Command parseCopyEventsBetween(String input) {
    Matcher matcher = COPY_EVENTS_BETWEEN_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid command. Use: copy events between <date> and <date> --target <cal> to <date>");
    }
    return new CopyEventsBetweenCommand(matcher.group(1), matcher.group(2), matcher.group(3),
        matcher.group(4));
  }

  private Command parseCreateEvent(String input) {
    Matcher matcher = CREATE_EVENT_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid create event command format.");
    }

    String subject = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    String rest = matcher.group(3);

    if (rest.matches(
        "on \\d{4}-\\d{2}-\\d{2}"
            + "(?: repeats [MTWRFSU]+ (?:for \\d+ times|until \\d{4}-\\d{2}-\\d{2}))?")) {
      return parseCreateOnDate(subject, rest);
    } else if (rest.matches(
        "from \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2} to \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}"
            + "(?: repeats [MTWRFSU]+ (?:for \\d+ times|until \\d{4}-\\d{2}-\\d{2}))?")) {
      return parseCreateFromTo(subject, rest);
    } else {
      throw new IllegalArgumentException("Invalid create event parameters.");
    }
  }

  private Command parseCreateOnDate(String subject, String rest) {
    String[] parts = rest.split("\\s+");
    String dateStr = parts[1];

    if (parts.length == 2) {
      return new CreateEventCommand(subject, null, null, dateStr, null, null, null);
    } else if (parts.length >= 5 && "repeats".equals(parts[2])) {
      String weekdaysStr = parts[3];
      if ("for".equals(parts[4])) {
        String occurrencesStr = parts[5];
        return new CreateEventCommand(subject, null, null, dateStr, weekdaysStr, occurrencesStr,
            null);
      } else if ("until".equals(parts[4])) {
        String untilDateStr = parts[5];
        return new CreateEventCommand(subject, null, null, dateStr, weekdaysStr, null,
            untilDateStr);
      }
    }

    throw new IllegalArgumentException("Invalid 'create event on' command format.");
  }

  private Command parseCreateFromTo(String subject, String rest) {
    Pattern pattern = Pattern.compile(
        "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");
    Matcher matcher = pattern.matcher(rest);

    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid 'create event from-to' command format.");
    }

    String startDateTimeStr = matcher.group(1);
    String endDateTimeStr = matcher.group(2);
    String remaining = rest.substring(matcher.end()).trim();

    if (remaining.isEmpty()) {
      return new CreateEventCommand(subject, startDateTimeStr, endDateTimeStr, null, null, null,
          null);
    } else if (remaining.matches(
        "repeats [MTWRFSU]+ (?:for \\d+ times|until \\d{4}-\\d{2}-\\d{2})")) {
      String[] parts = remaining.split("\\s+");
      String weekdaysStr = parts[1];

      if ("for".equals(parts[2])) {
        String occurrencesStr = parts[3];
        return new CreateEventCommand(subject, startDateTimeStr, endDateTimeStr, null, weekdaysStr,
            occurrencesStr, null);
      } else if ("until".equals(parts[2])) {
        String untilDateStr = parts[3];
        return new CreateEventCommand(subject, startDateTimeStr, endDateTimeStr, null, weekdaysStr,
            null, untilDateStr);
      }
    }

    throw new IllegalArgumentException("Invalid 'create event from-to' command format.");
  }

  private Command parseEditEvent(String input) {
    Matcher matcher = EDIT_EVENT_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid edit command format.");
    }

    String editTypeStr = matcher.group(1).toLowerCase();
    String propertyName = matcher.group(2).toLowerCase();
    String subject = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
    String rest = matcher.group(5).trim();

    EditType editType;
    switch (editTypeStr) {
      case "event":
        editType = EditType.SINGLE_EVENT;
        break;
      case "events":
        editType = EditType.THIS_AND_FUTURE;
        break;
      case "series":
        editType = EditType.FULL_SERIES;
        break;
      default:
        throw new IllegalArgumentException("Unknown edit type: " + editTypeStr);
    }

    if (editType == EditType.SINGLE_EVENT) {
      Pattern singlePattern = Pattern.compile(
          "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
              + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) with (?:\"([^\"]*)\"|(\\S+))$",
          Pattern.CASE_INSENSITIVE);
      Matcher singleMatcher = singlePattern.matcher(rest);
      if (!singleMatcher.find()) {
        throw new IllegalArgumentException(
            "Invalid 'edit event' format. "
                + "Use: ... with \"New Value\" or ... with NewValue (no spaces)");
      }
      String startDateTimeStr = singleMatcher.group(1);
      String endDateTimeStr = singleMatcher.group(2);
      String newValueStr = singleMatcher.group(3) != null
          ? singleMatcher.group(3) : singleMatcher.group(4);
      return new EditEventCommand(editType, propertyName, subject, startDateTimeStr, endDateTimeStr,
          newValueStr);
    } else {
      Pattern pattern = Pattern.compile(
          "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) with (?:\"([^\"]*)\"|(\\S+))$",
          Pattern.CASE_INSENSITIVE);
      Matcher patternMatcher = pattern.matcher(rest);
      if (!patternMatcher.find()) {
        throw new IllegalArgumentException(
            "Invalid 'edit " + editTypeStr + "' format. Use: ... with \"New Value\" or ... with"
                + " NewValue (no spaces)");
      }
      String startDateTimeStr = patternMatcher.group(1);
      String newValueStr = patternMatcher.group(2) != null
          ? patternMatcher.group(2) : patternMatcher.group(3);
      return new EditEventCommand(editType, propertyName, subject, startDateTimeStr, null,
          newValueStr);
    }
  }

  private Command parseQueryEvents(String input) {
    Matcher matcher = QUERY_EVENTS_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid query command format.");
    }

    String queryType = matcher.group(1).toLowerCase();
    String parameters = matcher.group(2);

    if ("on".equals(queryType)) {
      return new QueryEventsCommand(QueryEventsCommand.QueryType.EVENTS_ON_DATE, parameters.trim());
    } else if ("from".equals(queryType)) {
      Pattern rangePattern = Pattern.compile(
          "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");
      Matcher rangeMatcher = rangePattern.matcher(parameters);
      if (!rangeMatcher.find()) {
        throw new IllegalArgumentException(
            "Invalid range format. Use: print events from DATETIME to DATETIME");
      }
      String startDateTimeStr = rangeMatcher.group(1);
      String endDateTimeStr = rangeMatcher.group(2);
      return new QueryEventsCommand(QueryEventsCommand.QueryType.EVENTS_IN_RANGE, startDateTimeStr,
          endDateTimeStr);
    } else {
      throw new IllegalArgumentException("Unknown query type: " + queryType);
    }
  }

  private Command parseStatus(String input) {
    Matcher matcher = STATUS_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid status command format. Use: show status on DATETIME");
    }
    String dateTimeStr = matcher.group(1).trim();
    return new QueryEventsCommand(QueryEventsCommand.QueryType.STATUS_AT_TIME, dateTimeStr);
  }

  private Command parseExport(String input) {
    Matcher matcher = EXPORT_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          "Invalid export command format. Use: export cal FILENAME.csv");
    }
    String filePath = matcher.group(1).trim();
    return new ExportCalendarCommand(filePath);
  }
}