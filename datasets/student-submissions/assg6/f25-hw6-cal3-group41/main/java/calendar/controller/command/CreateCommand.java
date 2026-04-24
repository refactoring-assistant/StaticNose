package calendar.controller.command;

import calendar.model.CalendarModel;
import calendar.model.MultiCalendarModel;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to handle creating events and event series.
 */
class CreateCommand implements Command {

  private final MultiCalendarModel multiModel;
  private static final DateTimeFormatter ISO_LOCAL_MIN =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates a CreateCommand with the given multi-calendar model.
   *
   * @param multiModel the multi-calendar model to use
   */
  public CreateCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    // Pattern for: create event <subject> from <datetime> to <datetime>
    // [repeats <days> for <N> times|until <date>]
    Pattern psingle = Pattern.compile(
        "^create\\s+event\\s+"
            + "(?:\"(.+?)\"|([^\"].*?))\\s+"
            + "from\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "to\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})"
            + "(?:\\s+repeats\\s+([MTWRFSU]+)\\s+"
            + "(?:for\\s+(\\d+)\\s+times|until\\s+([0-9]{4}-[0-9]{2}-[0-9]{2})))?$",
        Pattern.CASE_INSENSITIVE);

    // Pattern for: create event <subject> from <datetime> (all-day variant)
    Pattern pfromonly = Pattern.compile(
        "^create\\s+event\\s+"
            + "(?:\"(.+?)\"|([^\"].*?))\\s+"
            + "from\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})$",
        Pattern.CASE_INSENSITIVE);

    // Pattern for: create event <subject> on <date>
    // [repeats <days> for <N> times|until <date>]
    Pattern pallday = Pattern.compile(
        "^create\\s+event\\s+"
            + "(?:\"(.+?)\"|([^\"].*?))\\s+"
            + "on\\s+([0-9]{4}-[0-9]{2}-[0-9]{2})"
            + "(?:\\s+repeats\\s+([MTWRFSU]+)\\s+"
            + "(?:for\\s+(\\d+)\\s+times|until\\s+([0-9]{4}-[0-9]{2}-[0-9]{2})))?$",
        Pattern.CASE_INSENSITIVE);

    Matcher m1 = psingle.matcher(command);
    if (m1.matches()) {
      String subject = pickSubject(m1.group(1), m1.group(2));
      LocalDateTime start = LocalDateTime.parse(m1.group(3), ISO_LOCAL_MIN);
      LocalDateTime end = LocalDateTime.parse(m1.group(4), ISO_LOCAL_MIN);
      String daysStr = m1.group(5);
      Integer times = (m1.group(6) == null) ? null : Integer.valueOf(m1.group(6));
      LocalDate until = (m1.group(7) == null) ? null : LocalDate.parse(m1.group(7));

      CalendarModel model = multiModel.getCurrentModel();
      if (daysStr == null) {
        model.createEvent(subject, start, end);
        return "OK: event created.";
      } else {
        Set<DayOfWeek> days = parseDays(daysStr);
        int count = model.createEventSeries(subject, start, end, days, times, until);
        return "OK: event series created (" + count + " instances).";
      }
    }

    Matcher m1b = pfromonly.matcher(command);
    if (m1b.matches()) {
      CalendarModel model = multiModel.getCurrentModel();
      String subject = pickSubject(m1b.group(1), m1b.group(2));
      LocalDateTime start = LocalDateTime.parse(m1b.group(3), ISO_LOCAL_MIN);
      LocalDate day = start.toLocalDate();
      model.createAllDayEvent(subject, day);
      return "OK: all-day event created (08:00–17:00).";
    }

    Matcher m2 = pallday.matcher(command);
    if (m2.matches()) {
      CalendarModel model = multiModel.getCurrentModel();
      String subject = pickSubject(m2.group(1), m2.group(2));
      LocalDate day = LocalDate.parse(m2.group(3));
      String daysStr = m2.group(4);
      Integer times = (m2.group(5) == null) ? null : Integer.valueOf(m2.group(5));
      LocalDate until = (m2.group(6) == null) ? null : LocalDate.parse(m2.group(6));

      if (daysStr == null) {
        model.createAllDayEvent(subject, day);
        return "OK: all-day event created.";
      } else {
        Set<DayOfWeek> days = parseDays(daysStr);
        int count = model.createAllDayEventSeries(subject, day, days, times, until);
        return "OK: event series created (" + count + " instances).";
      }
    }

    throw new IllegalArgumentException("Invalid create command.");
  }

  private static String pickSubject(String quoted, String bare) {
    String s;
    if (quoted != null && !quoted.trim().isEmpty()) {
      s = quoted.trim();
    } else if (bare != null) {
      s = bare.trim();
    } else {
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

  private static Set<DayOfWeek> parseDays(String s) {
    if (s == null || s.trim().isEmpty()) {
      throw new IllegalArgumentException("Repeat days cannot be empty.");
    }
    EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
    char[] arr = s.toUpperCase().toCharArray();
    for (char c : arr) {
      switch (c) {
        case 'M':
          set.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          set.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          set.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          set.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          set.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          set.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          set.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday code: " + c);
      }
    }
    return set;
  }
}

