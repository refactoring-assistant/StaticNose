package calendar.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parses text commands for the calendar application.
 * Supports commands for creating, editing, querying, and exporting events.
 */
public class CommandParser {

  /**
   * Represents a parsed command with its type and parameters.
   */
  public static class Command {
    /**
     * Enum for storing type of commands.
     */
    public enum Type {
      // Calendar management
      CREATE_CALENDAR,
      EDIT_CALENDAR,
      USE_CALENDAR,

      // Event copying
      COPY_EVENT,
      COPY_EVENTS_ON_DATE,
      COPY_EVENTS_IN_RANGE,

      // Existing event commands
      CREATE_EVENT,
      CREATE_EVENT_SERIES,
      EDIT_SINGLE,
      EDIT_FROM,
      EDIT_SERIES,
      PRINT_DATE,
      PRINT_RANGE,
      SHOW_STATUS,
      EXPORT,
      EXIT
    }

    public Type type;
    public Map<String, Object> params;

    /**
     * Constructor for Command parser to get commands.
     *
     * @param type type of commands as enum type
     */
    public Command(Type type) {
      this.type = type;
      this.params = new HashMap<>();
    }

    @Override
    public String toString() {
      return "Command{type=" + type + ", params=" + params + "}";
    }
  }

  /**
   * Parses a command string and returns a Command object.
   *
   * @param commandStr Command String as string
   * @return Command
   */
  public static Command parse(String commandStr) {
    if (commandStr == null || commandStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    String trimmed = commandStr.trim();

    // Exit command
    if (trimmed.equalsIgnoreCase("exit")) {
      return new Command(Command.Type.EXIT);
    }

    // Calendar management commands
    if (trimmed.startsWith("create calendar ")) {
      return parseCreateCalendar(trimmed.substring(16));
    }
    if (trimmed.startsWith("edit calendar ")) {
      return parseEditCalendar(trimmed.substring(14));
    }
    if (trimmed.startsWith("use calendar ")) {
      return parseUseCalendar(trimmed.substring(13));
    }

    // Copy commands
    if (trimmed.startsWith("copy event ")) {
      return parseCopyEvent(trimmed.substring(11));
    }
    if (trimmed.startsWith("copy events on ")) {
      return parseCopyEventsOnDate(trimmed.substring(15));
    }
    if (trimmed.startsWith("copy events between ")) {
      return parseCopyEventsInRange(trimmed.substring(20));
    }

    // Create event commands
    if (trimmed.startsWith("create event ")) {
      return parseCreateEvent(trimmed.substring(13));
    }

    // Edit commands
    if (trimmed.startsWith("edit event ")) {
      return parseEditSingle(trimmed.substring(11));
    }
    if (trimmed.startsWith("edit events ")) {
      return parseEditFrom(trimmed.substring(12));
    }
    if (trimmed.startsWith("edit series ")) {
      return parseEditSeries(trimmed.substring(12));
    }

    // Print commands
    if (trimmed.startsWith("print events on ")) {
      return parsePrintDate(trimmed.substring(16));
    }
    if (trimmed.startsWith("print events from ")) {
      return parsePrintRange(trimmed.substring(18));
    }

    // Show status command
    if (trimmed.startsWith("show status on ")) {
      return parseShowStatus(trimmed.substring(15));
    }

    // Export command
    if (trimmed.startsWith("export cal ")) {
      return parseExport(trimmed.substring(11));
    }

    throw new IllegalArgumentException("Unknown command: " + trimmed);
  }

  // ========== NEW CALENDAR MANAGEMENT PARSING METHODS ==========

  /**
   * Parses: create calendar --name calName --timezone area/location.
   */
  private static Command parseCreateCalendar(String args) {
    Command cmd = new Command(Command.Type.CREATE_CALENDAR);

    if (!args.contains("--name ")) {
      throw new IllegalArgumentException("Missing --name parameter");
    }
    if (!args.contains("--timezone ")) {
      throw new IllegalArgumentException("Missing --timezone parameter");
    }

    int nameIdx = args.indexOf("--name ") + 7;
    int timezoneIdx = args.indexOf("--timezone ");

    String name = args.substring(nameIdx, timezoneIdx).trim();
    String timezone = args.substring(timezoneIdx + 11).trim();

    cmd.params.put("name", name);
    cmd.params.put("timezone", timezone);

    return cmd;
  }

  /**
   * Parses: edit calendar --name name --property property value.
   */
  private static Command parseEditCalendar(String args) {

    if (!args.contains("--name ")) {
      throw new IllegalArgumentException("Missing --name parameter");
    }
    if (!args.contains("--property ")) {
      throw new IllegalArgumentException("Missing --property parameter");
    }

    int nameIdx = args.indexOf("--name ") + 7;
    int propertyIdx = args.indexOf("--property ");

    String name = args.substring(nameIdx, propertyIdx).trim();
    String propertyPart = args.substring(propertyIdx + 11).trim();

    // Split property and value
    int spaceIdx = propertyPart.indexOf(' ');
    if (spaceIdx == -1) {
      throw new IllegalArgumentException("Missing property value");
    }

    String property = propertyPart.substring(0, spaceIdx);
    String value = propertyPart.substring(spaceIdx + 1).trim();

    Command cmd = new Command(Command.Type.EDIT_CALENDAR);

    cmd.params.put("name", name);
    cmd.params.put("property", property);
    cmd.params.put("value", value);

    return cmd;
  }

  /**
   * Parses: use calendar --name name.
   */
  private static Command parseUseCalendar(String args) {

    if (!args.startsWith("--name ")) {
      throw new IllegalArgumentException("Missing --name parameter");
    }

    String name = args.substring(7).trim();
    Command cmd = new Command(Command.Type.USE_CALENDAR);

    cmd.params.put("name", name);

    return cmd;
  }

  // ========== EVENT COPYING PARSING METHODS ==========

  /**
   * Parses: copy event eventName on dateTime --target calendarName to dateTime.
   */
  private static Command parseCopyEvent(String args) {

    if (!args.contains(" on ")) {
      throw new IllegalArgumentException("Missing 'on' keyword");
    }
    if (!args.contains("--target ")) {
      throw new IllegalArgumentException("Missing --target parameter");
    }
    if (!args.contains(" to ")) {
      throw new IllegalArgumentException("Missing 'to' keyword");
    }

    int onIdx = args.indexOf(" on ");
    int targetIdx = args.indexOf("--target ");
    int toIdx = args.lastIndexOf(" to ");

    String subject = extractSubject(args.substring(0, onIdx));
    String startStr = args.substring(onIdx + 4, targetIdx).trim();
    String targetCalendar = args.substring(targetIdx + 9, toIdx).trim();
    String targetStartStr = args.substring(toIdx + 4).trim();

    Command cmd = new Command(Command.Type.COPY_EVENT);

    cmd.params.put("subject", subject);
    cmd.params.put("start", parseDateTime(startStr));
    cmd.params.put("targetCalendar", targetCalendar);
    cmd.params.put("targetStart", parseDateTime(targetStartStr));

    return cmd;
  }

  /**
   * Parses: copy events on date --target calendarName to date.
   */
  private static Command parseCopyEventsOnDate(String args) {

    if (!args.contains("--target ")) {
      throw new IllegalArgumentException("Missing --target parameter");
    }
    if (!args.contains(" to ")) {
      throw new IllegalArgumentException("Missing 'to' keyword");
    }

    int targetIdx = args.indexOf("--target ");
    int toIdx = args.lastIndexOf(" to ");

    String sourceDate = args.substring(0, targetIdx).trim();
    String targetCalendar = args.substring(targetIdx + 9, toIdx).trim();
    String targetDate = args.substring(toIdx + 4).trim();

    Command cmd = new Command(Command.Type.COPY_EVENTS_ON_DATE);

    cmd.params.put("sourceDate", parseDate(sourceDate));
    cmd.params.put("targetCalendar", targetCalendar);
    cmd.params.put("targetDate", parseDate(targetDate));

    return cmd;
  }

  /**
   * Parses: copy events between date and date --target calendarName to date.
   */
  private static Command parseCopyEventsInRange(String args) {

    if (!args.contains(" and ")) {
      throw new IllegalArgumentException("Missing 'and' keyword");
    }
    if (!args.contains("--target ")) {
      throw new IllegalArgumentException("Missing --target parameter");
    }
    if (!args.contains(" to ")) {
      throw new IllegalArgumentException("Missing 'to' keyword");
    }

    int andIdx = args.indexOf(" and ");
    int targetIdx = args.indexOf("--target ");
    int toIdx = args.lastIndexOf(" to ");

    String startDate = args.substring(0, andIdx).trim();
    String endDate = args.substring(andIdx + 5, targetIdx).trim();
    String targetCalendar = args.substring(targetIdx + 9, toIdx).trim();
    String targetStartDate = args.substring(toIdx + 4).trim();

    Command cmd = new Command(Command.Type.COPY_EVENTS_IN_RANGE);

    cmd.params.put("startDate", parseDate(startDate));
    cmd.params.put("endDate", parseDate(endDate));
    cmd.params.put("targetCalendar", targetCalendar);
    cmd.params.put("targetStartDate", parseDate(targetStartDate));

    return cmd;
  }

  // ========== EXISTING PARSING METHODS (unchanged) ==========

  private static Command parseCreateEvent(String args) {
    Command cmd = new Command(Command.Type.CREATE_EVENT);

    // Check if it's a series (contains "repeats")
    if (args.contains(" repeats ")) {
      cmd.type = Command.Type.CREATE_EVENT_SERIES;
      return parseCreateEventSeries(args);
    }

    // Parse single event
    // Format: <subject> from <dateTime> to <dateTime>
    // or: <subject> on <date>

    if (args.contains(" on ")) {
      // All-day event: <subject> on <date>
      int onIdx = args.lastIndexOf(" on ");
      String subject = extractSubject(args.substring(0, onIdx));
      String dateStr = args.substring(onIdx + 4).trim();

      cmd.params.put("subject", subject);
      cmd.params.put("date", parseDate(dateStr));
      cmd.params.put("allDay", true);

    } else if (args.contains(" from ") && args.contains(" to ")) {
      // Timed event
      int fromIdx = args.lastIndexOf(" from ");
      int toIdx = args.lastIndexOf(" to ");

      String subject = extractSubject(args.substring(0, fromIdx));
      String startStr = args.substring(fromIdx + 6, toIdx).trim();
      String endStr = args.substring(toIdx + 4).trim();

      cmd.params.put("subject", subject);
      cmd.params.put("startDateTime", parseDateTime(startStr));
      cmd.params.put("endDateTime", parseDateTime(endStr));
      cmd.params.put("allDay", false);
    } else {
      throw new IllegalArgumentException("Invalid create event syntax");
    }

    return cmd;
  }

  private static Command parseCreateEventSeries(String args) {
    Command cmd = new Command(Command.Type.CREATE_EVENT_SERIES);

    // Format: <subject> from <dateTime> to <dateTime> repeats <weekdays> for <N> times
    // or: <subject> from <dateTime> to <dateTime> repeats <weekdays> until <date>
    // or: <subject> on <date> repeats <weekdays> for <N> times
    // or: <subject> on <date> repeats <weekdays> until <date>

    int repeatsIdx = args.lastIndexOf(" repeats ");
    String beforeRepeats = args.substring(0, repeatsIdx);
    String afterRepeats = args.substring(repeatsIdx + 9);

    boolean isAllDay = beforeRepeats.contains(" on ");

    if (isAllDay) {
      // All-day series
      int onIdx = beforeRepeats.lastIndexOf(" on ");
      String subject = extractSubject(beforeRepeats.substring(0, onIdx));
      String dateStr = beforeRepeats.substring(onIdx + 4).trim();
      LocalDate date = parseDate(dateStr);

      cmd.params.put("subject", subject);
      cmd.params.put("startDateTime", LocalDateTime.of(date, java.time.LocalTime
          .of(8, 0)));
      cmd.params.put("endDateTime", LocalDateTime.of(date, java.time.LocalTime
          .of(17, 0)));
      cmd.params.put("allDay", true);

    } else {
      // Timed series
      int fromIdx = beforeRepeats.lastIndexOf(" from ");
      int toIdx = beforeRepeats.lastIndexOf(" to ");

      String subject = extractSubject(beforeRepeats.substring(0, fromIdx));
      String startStr = beforeRepeats.substring(fromIdx + 6, toIdx).trim();
      String endStr = beforeRepeats.substring(toIdx + 4).trim();

      cmd.params.put("subject", subject);
      cmd.params.put("startDateTime", parseDateTime(startStr));
      cmd.params.put("endDateTime", parseDateTime(endStr));
      cmd.params.put("allDay", false);
    }

    // Parse repeats clause
    if (afterRepeats.contains(" for ") && afterRepeats.contains(" times")) {
      // repeats <weekdays> for <N> times
      int forIdx = afterRepeats.lastIndexOf(" for ");
      int timesIdx = afterRepeats.lastIndexOf(" times");

      String weekdaysStr = afterRepeats.substring(0, forIdx).trim();
      String occurrencesStr = afterRepeats.substring(forIdx + 5, timesIdx).trim();

      cmd.params.put("weekdays", parseWeekdays(weekdaysStr));
      cmd.params.put("occurrences", Integer.parseInt(occurrencesStr));

    } else if (afterRepeats.contains(" until ")) {
      // repeats <weekdays> until <date>
      int untilIdx = afterRepeats.lastIndexOf(" until ");

      String weekdaysStr = afterRepeats.substring(0, untilIdx).trim();
      String untilDateStr = afterRepeats.substring(untilIdx + 7).trim();

      cmd.params.put("weekdays", parseWeekdays(weekdaysStr));
      cmd.params.put("untilDate", parseDate(untilDateStr));

    } else {
      throw new IllegalArgumentException("Invalid repeats clause");
    }

    return cmd;
  }

  private static Command parseEditSingle(String args) {
    return parseEdit(args, Command.Type.EDIT_SINGLE);
  }

  private static Command parseEditFrom(String args) {
    return parseEdit(args, Command.Type.EDIT_FROM);
  }

  private static Command parseEditSeries(String args) {
    return parseEdit(args, Command.Type.EDIT_SERIES);
  }

  private static Command parseEdit(String args, Command.Type type) {
    Command cmd = new Command(type);

    // Format: <property> <subject> from <dateTime> with <newValue>
    // or: <property> <subject> from <dateTime> to <dateTime> with <newValue> (for single)

    int withIdx = args.lastIndexOf(" with ");
    if (withIdx == -1) {
      throw new IllegalArgumentException("Edit command must contain 'with'");
    }

    String beforeWith = args.substring(0, withIdx);

    // Extract property (first word)
    int firstSpace = beforeWith.indexOf(' ');
    String property = beforeWith.substring(0, firstSpace);
    String rest = beforeWith.substring(firstSpace + 1);

    // Extract subject and datetime
    int fromIdx = rest.lastIndexOf(" from ");
    String subject = extractSubject(rest.substring(0, fromIdx));
    String fromPart = rest.substring(fromIdx + 6);

    String startDateTimeStr;
    if (type == Command.Type.EDIT_SINGLE && fromPart.contains(" to ")) {
      int toIdx = fromPart.lastIndexOf(" to ");
      startDateTimeStr = fromPart.substring(0, toIdx).trim();
    } else {
      startDateTimeStr = fromPart.trim();
    }

    String newValue = args.substring(withIdx + 6).trim();

    cmd.params.put("property", property);
    cmd.params.put("subject", subject);
    cmd.params.put("startDateTime", parseDateTime(startDateTimeStr));
    cmd.params.put("newValue", newValue);

    return cmd;
  }

  private static Command parsePrintDate(String args) {
    Command cmd = new Command(Command.Type.PRINT_DATE);
    cmd.params.put("date", parseDate(args.trim()));
    return cmd;
  }

  private static Command parsePrintRange(String args) {
    Command cmd = new Command(Command.Type.PRINT_RANGE);

    int toIdx = args.lastIndexOf(" to ");
    if (toIdx == -1) {
      throw new IllegalArgumentException("Print range must contain 'to'");
    }

    String startStr = args.substring(0, toIdx).trim();
    String endStr = args.substring(toIdx + 4).trim();

    cmd.params.put("startDateTime", parseDateTime(startStr));
    cmd.params.put("endDateTime", parseDateTime(endStr));

    return cmd;
  }

  private static Command parseShowStatus(String args) {
    Command cmd = new Command(Command.Type.SHOW_STATUS);
    cmd.params.put("dateTime", parseDateTime(args.trim()));
    return cmd;
  }

  private static Command parseExport(String args) {
    Command cmd = new Command(Command.Type.EXPORT);
    cmd.params.put("filename", args.trim());
    return cmd;
  }

  // ========== HELPER METHODS ==========

  private static String extractSubject(String text) {
    text = text.trim();
    if (text.startsWith("\"") && text.contains("\"")) {
      int endQuote = text.indexOf("\"", 1);
      return text.substring(1, endQuote);
    }
    return text;
  }

  private static LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr
          + ". Expected format: YYYY-MM-DD");
    }
  }

  private static LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date/time format: " + dateTimeStr
          + ". Expected format: YYYY-MM-DDThh:mm");
    }
  }

  private static Set<DayOfWeek> parseWeekdays(String weekdaysStr) {
    Set<DayOfWeek> weekdays = new HashSet<>();

    for (char c : weekdaysStr.toCharArray()) {
      switch (c) {
        case 'M':
          weekdays.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          weekdays.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          weekdays.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          weekdays.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          weekdays.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          weekdays.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          weekdays.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday code: " + c);
      }
    }

    return weekdays;
  }
}