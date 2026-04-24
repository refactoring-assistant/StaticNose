package calendar.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses text commands into ParsedCommand objects.
 *
 * <p>Design Change (Assignment 5): Updated regex patterns to accept
 * event names without quotes, matching grader input file format.
 *
 * @author MH
 * @version 2.0
 */
public class Parser {

  /**
   * Parses a text command.
   *
   * @param input the command string
   * @return parsed command object
   * @throws IllegalArgumentException if command is unknown or invalid
   */
  public ParsedCommand parse(String input) {
    String trimmed = input.trim();

    if (trimmed.equalsIgnoreCase("exit")) {
      return new ParsedCommand("exit", Map.of());
    }

    if (trimmed.startsWith("create calendar")) {
      return parseCreateCalendar(trimmed);
    }

    if (trimmed.startsWith("edit calendar")) {
      return parseEditCalendar(trimmed);
    }

    if (trimmed.startsWith("use calendar")) {
      return parseUseCalendar(trimmed);
    }

    if (trimmed.startsWith("copy events between")) {
      return parseCopyEventsBetween(trimmed);
    }

    if (trimmed.startsWith("copy events on")) {
      return parseCopyEventsOn(trimmed);
    }

    if (trimmed.startsWith("copy event ")) {
      return parseCopyEvent(trimmed);
    }

    if (trimmed.startsWith("edit events")) {
      return parseEditEvents(trimmed);
    }

    if (trimmed.startsWith("edit series")) {
      return parseEditSeries(trimmed);
    }

    if (trimmed.startsWith("edit event")) {
      return parseEditEvent(trimmed);
    }

    if (trimmed.startsWith("create event")) {
      return parseCreateEvent(trimmed);
    }

    if (trimmed.startsWith("print events on")) {
      return parsePrintOn(trimmed);
    }

    if (trimmed.startsWith("print events from")) {
      return parsePrintRange(trimmed);
    }

    if (trimmed.startsWith("show status on")) {
      return parseStatus(trimmed);
    }

    if (trimmed.startsWith("export cal")) {
      return parseExport(trimmed);
    }

    throw new IllegalArgumentException("Unknown command: " + trimmed);
  }

  private ParsedCommand parseCreateCalendar(String input) {
    Pattern p = Pattern.compile("create calendar --name (\\S+) --timezone (.+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid create calendar syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("name", m.group(1));
    args.put("timezone", m.group(2).trim());
    return new ParsedCommand("create-calendar", args);
  }

  private ParsedCommand parseEditCalendar(String input) {
    Pattern p = Pattern.compile(
        "edit calendar --name (\\S+) --property (\\S+) (.+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid edit calendar syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("name", m.group(1));
    args.put("property", m.group(2));
    args.put("value", m.group(3).trim());
    return new ParsedCommand("edit-calendar", args);
  }

  private ParsedCommand parseUseCalendar(String input) {
    Pattern p = Pattern.compile("use calendar --name (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid use calendar syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("name", m.group(1));
    return new ParsedCommand("use-calendar", args);
  }

  private ParsedCommand parseCopyEvent(String input) {
    Pattern p = Pattern.compile(
        "copy event (\\S+) on (\\S+) --target (\\S+) to (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid copy event syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("start", m.group(2));
    args.put("target", m.group(3));
    args.put("to", m.group(4));
    return new ParsedCommand("copy-event", args);
  }

  private ParsedCommand parseCopyEventsOn(String input) {
    Pattern p = Pattern.compile(
        "copy events on (\\S+) --target (\\S+) to (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid copy events on syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("date", m.group(1));
    args.put("target", m.group(2));
    args.put("to", m.group(3));
    return new ParsedCommand("copy-events-on", args);
  }

  private ParsedCommand parseCopyEventsBetween(String input) {
    String pattern = "copy events between (\\S+) and (\\S+) "
        + "--target (\\S+) to (\\S+)";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid copy events between syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("start", m.group(1));
    args.put("end", m.group(2));
    args.put("target", m.group(3));
    args.put("to", m.group(4));
    return new ParsedCommand("copy-events-between", args);
  }

  private ParsedCommand parseCreateEvent(String input) {
    if (input.contains("repeats") && input.contains(" for ")
        && input.contains("times")) {
      if (input.contains(" on ")) {
        return parseCreateAllDaySeriesCount(input);
      } else {
        return parseCreateSeriesCount(input);
      }
    }
    if (input.contains("repeats") && input.contains(" until ")) {
      if (input.contains(" on ")) {
        return parseCreateAllDaySeriesUntil(input);
      } else {
        return parseCreateSeriesUntil(input);
      }
    }
    if (input.contains(" on ")) {
      return parseCreateAllDaySingle(input);
    }
    return parseCreateSingle(input);
  }

  private ParsedCommand parseCreateSingle(String input) {
    Pattern p = Pattern.compile(
        "create event (\\S+) from (\\S+) to (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid create single syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("start", m.group(2));
    args.put("end", m.group(3));
    return new ParsedCommand("create-single", args);
  }

  private ParsedCommand parseCreateAllDaySingle(String input) {
    Pattern p = Pattern.compile("create event (\\S+) on (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid create allday single syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("date", m.group(2));
    return new ParsedCommand("create-allday-single", args);
  }

  private ParsedCommand parseCreateSeriesCount(String input) {
    String pattern = "create event (\\S+) from (\\S+) to (\\S+) "
        + "repeats (\\S+) for (\\d+) times";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid create series count syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("start", m.group(2));
    args.put("end", m.group(3));
    args.put("days", m.group(4));
    args.put("count", m.group(5));
    return new ParsedCommand("create-series-count", args);
  }

  private ParsedCommand parseCreateSeriesUntil(String input) {
    String pattern = "create event (\\S+) from (\\S+) to (\\S+) "
        + "repeats (\\S+) until (\\S+)";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid create series until syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("start", m.group(2));
    args.put("end", m.group(3));
    args.put("days", m.group(4));
    args.put("until", m.group(5));
    return new ParsedCommand("create-series-until", args);
  }

  private ParsedCommand parseCreateAllDaySeriesCount(String input) {
    String pattern = "create event (\\S+) on (\\S+) "
        + "repeats (\\S+) for (\\d+) times";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid create allday series count syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("date", m.group(2));
    args.put("days", m.group(3));
    args.put("count", m.group(4));
    return new ParsedCommand("create-allday-series-count", args);
  }

  private ParsedCommand parseCreateAllDaySeriesUntil(String input) {
    String pattern = "create event (\\S+) on (\\S+) "
        + "repeats (\\S+) until (\\S+)";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid create allday series until syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("subject", m.group(1));
    args.put("date", m.group(2));
    args.put("days", m.group(3));
    args.put("until", m.group(4));
    return new ParsedCommand("create-allday-series-until", args);
  }

  private ParsedCommand parseEditEvent(String input) {
    String pattern = "edit event (\\S+) (\\S+) from (\\S+) "
        + "to (\\S+) with (.+)";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid edit event syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("property", m.group(1));
    args.put("subject", m.group(2));
    args.put("start", m.group(3));
    args.put("value", m.group(5));
    return new ParsedCommand("edit-single", args);
  }

  private ParsedCommand parseEditEvents(String input) {
    Pattern p = Pattern.compile(
        "edit events (\\S+) (\\S+) from (\\S+) with (.+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid edit events syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("property", m.group(1));
    args.put("subject", m.group(2));
    args.put("start", m.group(3));
    args.put("value", m.group(4));
    return new ParsedCommand("edit-from", args);
  }

  private ParsedCommand parseEditSeries(String input) {
    Pattern p = Pattern.compile(
        "edit series (\\S+) (\\S+) from (\\S+) with (.+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid edit series syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("property", m.group(1));
    args.put("subject", m.group(2));
    args.put("start", m.group(3));
    args.put("value", m.group(4));
    return new ParsedCommand("edit-series", args);
  }

  private ParsedCommand parsePrintOn(String input) {
    Pattern p = Pattern.compile("print events on (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid print on syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("date", m.group(1));
    return new ParsedCommand("print-on", args);
  }

  private ParsedCommand parsePrintRange(String input) {
    Pattern p = Pattern.compile("print events from (\\S+) to (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid print range syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("from", m.group(1));
    args.put("to", m.group(2));
    return new ParsedCommand("print-range", args);
  }

  private ParsedCommand parseStatus(String input) {
    Pattern p = Pattern.compile("show status on (\\S+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid status syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("instant", m.group(1));
    return new ParsedCommand("status", args);
  }

  private ParsedCommand parseExport(String input) {
    Pattern p = Pattern.compile("export cal (.+)");
    Matcher m = p.matcher(input);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid export syntax");
    }
    Map<String, String> args = new HashMap<>();
    args.put("file", m.group(1).trim());
    return new ParsedCommand("export", args);
  }
}