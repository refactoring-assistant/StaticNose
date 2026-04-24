package calendar.controller.command;

import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.MultiCalendarModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to handle editing events and event series.
 */
class EditCommand implements Command {

  private final MultiCalendarModel multiModel;
  private static final DateTimeFormatter ISO_LOCAL_MIN =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates an EditCommand with the given multi-calendar model.
   *
   * @param multiModel the multi-calendar model to use
   */
  public EditCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    // Pattern for: edit event <subject> on <datetime> --property <property> <value>
    Pattern peventOn = Pattern.compile(
        "^edit\\s+event\\s+"
            + "(?:\"(.+?)\"|([^\"].*?))\\s+"
            + "on\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "--property\\s+(subject|start|end|description|location|status|duration|name)\\s+"
            + "(.+)$",
        Pattern.CASE_INSENSITIVE);

    // Pattern for: edit event <property> <subject> from <datetime> to <datetime> with <value>
    Pattern pevent = Pattern.compile(
        "^edit\\s+event\\s+"
            + "(subject|start|end|description|location|status)\\s+"
            + "(?:\"(.+?)\"|([^\"].*?))\\s+"
            + "from\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "to\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "with\\s+(.+)$",
        Pattern.CASE_INSENSITIVE);

    // Pattern for: edit events <property> <subject> from <datetime> with <value>
    Pattern pevents = Pattern.compile(
        "^edit\\s+events\\s+"
            + "(subject|start|end|description|location|status)\\s+"
            + "(?:\"([^\\\"]+)\"|([^\"\\s].*?))\\s+"
            + "from\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "with\\s+(.+)$",
        Pattern.CASE_INSENSITIVE);

    CalendarModel model = multiModel.getCurrentModel();
    
    Matcher m0 = peventOn.matcher(command);
    if (m0.matches()) {
      String subject = pickSubject(m0.group(1), m0.group(2));
      LocalDateTime start = LocalDateTime.parse(m0.group(3), ISO_LOCAL_MIN);
      String prop = m0.group(4).toLowerCase();
      String newVal = normalizeValue(m0.group(5), prop);

      // Find event to get end time
      List<Event> events = model.getEventsOn(start.toLocalDate());
      LocalDateTime originalEnd = null;
      for (Event e : events) {
        if (e.subject().trim().equalsIgnoreCase(subject.trim()) && e.startDate().equals(start)) {
          originalEnd = e.endDate();
          break;
        }
      }
      if (originalEnd == null) {
        throw new IllegalArgumentException("Event '" + subject + "' not found at specified time.");
      }

      // Handle special properties
      if ("duration".equals(prop)) {
        int minutes = Integer.parseInt(newVal.trim());
        LocalDateTime newEnd = start.plusMinutes(minutes);
        model.editEvent(subject, start, originalEnd, "end", newEnd.format(ISO_LOCAL_MIN));
        return "OK: event updated.";
      } else if ("name".equals(prop)) {
        model.editEvent(subject, start, originalEnd, "subject", newVal);
        return "OK: event updated.";
      } else {
        model.editEvent(subject, start, originalEnd, prop, newVal);
        return "OK: event updated.";
      }
    }
    
    Matcher m1 = pevent.matcher(command);
    if (m1.matches()) {
      String prop = m1.group(1).toLowerCase();
      String subject = pickSubject(m1.group(2), m1.group(3));
      LocalDateTime start = LocalDateTime.parse(m1.group(4), ISO_LOCAL_MIN);
      LocalDateTime end = LocalDateTime.parse(m1.group(5), ISO_LOCAL_MIN);
      String newVal = normalizeValue(m1.group(6), prop);

      model.editEvent(subject, start, end, prop, newVal);
      return "OK: event updated.";
    }

    Matcher m2 = pevents.matcher(command);
    if (m2.matches()) {
      String prop = m2.group(1).toLowerCase();
      String subject = pickSubject(m2.group(2), m2.group(3));
      LocalDateTime start = LocalDateTime.parse(m2.group(4), ISO_LOCAL_MIN);
      String newVal = normalizeValue(m2.group(5), prop);

      int count = model.editEventsFrom(subject, start, prop, newVal);
      return "OK: events updated (" + count + ").";
    }

    // Pattern for: edit series <property> <subject> from <datetime> with <value>
    Pattern pseries = Pattern.compile(
        "^edit\\s+series\\s+"
            + "(subject|start|end|description|location|status)\\s+"
            + "(?:\"(.+?)\"|([^\"].*?))\\s+"
            + "from\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "with\\s+(.+)$",
        Pattern.CASE_INSENSITIVE);
    Matcher m3 = pseries.matcher(command);
    if (m3.matches()) {
      String prop = m3.group(1).toLowerCase();
      String subject = pickSubject(m3.group(2), m3.group(3));
      LocalDateTime start = LocalDateTime.parse(m3.group(4), ISO_LOCAL_MIN);
      String newVal = normalizeValue(m3.group(5), prop);

      int count = model.editSeries(subject, start, prop, newVal);
      return "OK: series updated (" + count + ").";
    }

    throw new IllegalArgumentException("Invalid edit command.");
  }

  private static String pickSubject(String quoted, String bare) {
    String s = (quoted != null && !quoted.trim().isEmpty()) ? quoted.trim() : bare.trim();
    if (s == null) {
      s = "";
    }
    if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
      s = s.substring(1, s.length() - 1);
    }
    if (s.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty.");
    }
    return s;
  }

  /**
   * Removes quotes from string values.
   */
  private static String normalizeValue(String value, String property) {
    if (value == null) {
      return "";
    }
    String trimmed = value.trim();
    if ("subject".equals(property) || "description".equals(property)
        || "location".equals(property) || "name".equals(property)) {
      if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
        return trimmed.substring(1, trimmed.length() - 1);
      }
    }
    return trimmed;
  }
}

