package calendar.controller.command;

import calendar.model.Calendar;
import calendar.model.MultiCalendarModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to copy events.
 */
class CopyEventCommand implements Command {
  private final MultiCalendarModel multiModel;

  private static final DateTimeFormatter ISO_LOCAL_MIN =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter ISO_LOCAL_DATE =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Constructor.
   *
   * @param multiModel the model
   */
  public CopyEventCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    // Pattern 1: copy event <eventName> on <datetime> --target <calendar> to <datetime>
    Pattern p1 = Pattern.compile(
        "^copy\\s+event\\s+(?:\"(.+?)\"|([^\"].*?))\\s+on\\s+"
        + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+--target\\s+(\\S+)\\s+to\\s+"
        + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$",
        Pattern.CASE_INSENSITIVE);
    Matcher m1 = p1.matcher(command);
    if (m1.matches()) {
      String eventName = pickEventName(m1.group(1), m1.group(2));
      LocalDateTime sourceStart = LocalDateTime.parse(m1.group(3), ISO_LOCAL_MIN);
      String targetCalendar = m1.group(4);
      LocalDateTime targetStart = LocalDateTime.parse(m1.group(5), ISO_LOCAL_MIN);

      Calendar currentCal = multiModel.getCurrentCalendar();
      if (currentCal == null) {
        throw new IllegalArgumentException("No calendar is currently in use.");
      }
      String sourceCalendar = currentCal.getName();

      multiModel.copyEvent(sourceCalendar, eventName, sourceStart, targetCalendar, targetStart);
      return "OK: event '" + eventName + "' copied to calendar '" + targetCalendar + "'.";
    }

    // Pattern 2: copy events on <date> --target <calendar> to <date>
    Pattern p2 = Pattern.compile(
        "^copy\\s+events\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+--target\\s+(\\S+)\\s+"
        + "to\\s+(\\d{4}-\\d{2}-\\d{2})$",
        Pattern.CASE_INSENSITIVE);
    Matcher m2 = p2.matcher(command);
    if (m2.matches()) {
      LocalDate sourceDate = LocalDate.parse(m2.group(1), ISO_LOCAL_DATE);
      String targetCalendar = m2.group(2);
      LocalDate targetDate = LocalDate.parse(m2.group(3), ISO_LOCAL_DATE);

      Calendar currentCal = multiModel.getCurrentCalendar();
      if (currentCal == null) {
        throw new IllegalArgumentException("No calendar is currently in use.");
      }
      String sourceCalendar = currentCal.getName();

      int count = multiModel.copyEventsOnDate(
          sourceCalendar, sourceDate, targetCalendar, targetDate);
      return "OK: " + count + " event(s) copied to calendar '" + targetCalendar + "'.";
    }

    // Pattern 3: copy events between <date> and <date> --target <calendar> to <date>
    Pattern p3 = Pattern.compile(
        "^copy\\s+events\\s+between\\s+(\\d{4}-\\d{2}-\\d{2})\\s+and\\s+"
        + "(\\d{4}-\\d{2}-\\d{2})\\s+--target\\s+(\\S+)\\s+to\\s+(\\d{4}-\\d{2}-\\d{2})$",
        Pattern.CASE_INSENSITIVE);
    Matcher m3 = p3.matcher(command);
    if (m3.matches()) {
      LocalDate sourceStart = LocalDate.parse(m3.group(1), ISO_LOCAL_DATE);
      LocalDate sourceEnd = LocalDate.parse(m3.group(2), ISO_LOCAL_DATE);
      String targetCalendar = m3.group(3);
      LocalDate targetStart = LocalDate.parse(m3.group(4), ISO_LOCAL_DATE);

      Calendar currentCal = multiModel.getCurrentCalendar();
      if (currentCal == null) {
        throw new IllegalArgumentException("No calendar is currently in use.");
      }
      String sourceCalendar = currentCal.getName();

      LocalDateTime targetStartDateTime = targetStart.atStartOfDay();
      int count = multiModel.copyEventsBetween(
          sourceCalendar, sourceStart, sourceEnd, targetCalendar, targetStartDateTime);
      return "OK: " + count + " event(s) copied to calendar '" + targetCalendar + "'.";
    }

    throw new IllegalArgumentException(
        "Invalid copy command. Use: copy event <name> on <datetime> "
        + "--target <calendar> to <datetime>, or copy events on <date> "
        + "--target <calendar> to <date>, or copy events between <date> and <date> "
        + "--target <calendar> to <date>");
  }

  /**
   * Gets event name from command.
   */
  private static String pickEventName(String quoted, String bare) {
    String s = (quoted != null && !quoted.trim().isEmpty()) ? quoted.trim() : bare.trim();
    if (s == null) {
      s = "";
    }
    if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
      s = s.substring(1, s.length() - 1);
    }
    if (s.isEmpty()) {
      throw new IllegalArgumentException("Event name cannot be empty.");
    }
    return s;
  }
}

