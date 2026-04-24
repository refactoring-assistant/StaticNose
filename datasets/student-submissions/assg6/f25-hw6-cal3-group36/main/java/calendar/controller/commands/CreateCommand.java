package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.model.RecurrenceRule;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Creating single or recurring events.
 */
public class CreateCommand implements Command {

  private final String raw;

  /**
   * Constructor. Creating start.
   *
   * @param input cmd.
   */
  public CreateCommand(String input) {
    this.raw = input.trim();
    if (!raw.toLowerCase().startsWith("create event ")) {
      throw new IllegalArgumentException("Invalid create syntax");
    }
  }

  @Override
  public String execute(CalendarSystemModel model) {
    String rest = raw.substring("create event ".length()).trim();
    String subject;
    String afterSubject;
    if (rest.startsWith("\"")) {
      int endQuote = findClosingQuote(rest);
      if (endQuote <= 1) {
        throw new IllegalArgumentException("Invalid subject");
      }
      subject = rest.substring(1, endQuote);
      afterSubject = rest.substring(endQuote + 1).trim();
    } else {
      int split = indexOfKeyword(rest, new String[] {" from ", " on "});
      if (split < 0) {
        throw new IllegalArgumentException("Invalid create syntax");
      }
      subject = rest.substring(0, split).trim();
      afterSubject = rest.substring(split).trim();
    }
    if (afterSubject.toLowerCase().startsWith("on ")) {
      return handleAllDay(model, subject, afterSubject.substring(3).trim());
    }
    if (!afterSubject.toLowerCase().startsWith("from ")) {
      throw new IllegalArgumentException("Invalid create syntax");
    }
    String afterFrom = afterSubject.substring(5).trim();
    int toIdx = indexOfKeyword(afterFrom, new String[] {" to "});
    if (toIdx < 0) {
      throw new IllegalArgumentException("Invalid create syntax: missing 'to'");
    }
    String startStr = afterFrom.substring(0, toIdx).trim();
    String afterTo = afterFrom.substring(toIdx + 4).trim();
    String endStr;
    String repeatsPart = null;
    int repIdx = indexOfKeyword(afterTo.toLowerCase(), new String[] {" repeats "});
    if (repIdx >= 0) {
      endStr = afterTo.substring(0, repIdx).trim();
      repeatsPart = afterTo.substring(repIdx + " repeats ".length()).trim();
    } else {
      endStr = afterTo.trim();
    }
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    Instant start = parseLocalDateTimeToInstant(startStr, zone);
    Instant end = parseLocalDateTimeToInstant(endStr, zone);
    Event base = new EventBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("")
        .location("")
        .isPublic(true)
        .zone(zone)
        .build();
    if (repeatsPart == null) {
      model.createEvent(base);
      return "Event created.";
    } else {
      RecurrenceRule rule = parseRecurrence(repeatsPart);
      model.createRecurringEvent(base, rule);
      return "Recurring events created.";
    }
  }

  private String handleAllDay(CalendarSystemModel model, String subject, String remainder) {
    String dateStr;
    String repeatsPart = null;
    int repIdx = indexOfKeyword(remainder.toLowerCase(), new String[] {" repeats "});
    if (repIdx >= 0) {
      dateStr = remainder.substring(0, repIdx).trim();
      repeatsPart = remainder.substring(repIdx + " repeats ".length()).trim();
    } else {
      dateStr = remainder.trim();
    }
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    LocalDate day = parseLocalDate(dateStr);
    Instant start = day.atTime(8, 0).atZone(zone).toInstant();
    Instant end = day.atTime(17, 0).atZone(zone).toInstant();
    Event base = new EventBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .description("")
        .location("")
        .isPublic(true)
        .zone(zone)
        .build();
    if (repeatsPart == null) {
      model.createEvent(base);
      return "All-day event created.";
    } else {
      RecurrenceRule rule = parseRecurrence(repeatsPart);
      model.createRecurringEvent(base, rule);
      return "Recurring all-day events created.";
    }
  }

  private RecurrenceRule parseRecurrence(String repeatsPart) {
    String lower = repeatsPart.toLowerCase();
    int space = lower.indexOf(' ');
    if (space < 0) {
      throw new IllegalArgumentException("Invalid repeats syntax");
    }
    String weekdaysToken = repeatsPart.substring(0, space).trim();
    String tail = repeatsPart.substring(space).trim();
    List<DayOfWeek> days = parseWeekdays(weekdaysToken);
    if (tail.startsWith("for ")) {
      String afterFor = tail.substring(4).trim();
      int timesIdx = indexOfKeyword(afterFor, new String[] {" times"});
      if (timesIdx < 0) {
        throw new IllegalArgumentException("Invalid repeats syntax: missing 'times'");
      }
      String countStr = afterFor.substring(0, timesIdx).trim();
      int count = Integer.parseInt(countStr);
      if (count <= 0) {
        throw new IllegalArgumentException("Repeat count must be positive");
      }
      return new RecurrenceRule(days, count);
    } else if (tail.startsWith("until ")) {
      String dateStr = tail.substring(6).trim();
      parseLocalDate(dateStr);
      return new RecurrenceRule(days, dateStr);
    } else {
      throw new IllegalArgumentException("Invalid repeats syntax");
    }
  }

  private List<DayOfWeek> parseWeekdays(String token) {
    EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
    String t = token.trim().toUpperCase();
    for (int i = 0; i < t.length(); i++) {
      char c = t.charAt(i);
      DayOfWeek d = mapWeekdayChar(c);
      if (d == null) {
        throw new IllegalArgumentException("Invalid weekday code: " + c);
      }
      set.add(d);
    }
    return new ArrayList<>(set);
  }

  private DayOfWeek mapWeekdayChar(char c) {
    switch (c) {
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
        return DayOfWeek.SATURDAY;
      case 'U':
        return DayOfWeek.SUNDAY;
      default:
        return null;
    }
  }

  private Instant parseLocalDateTimeToInstant(String isoLocalDateTime, ZoneId zone) {
    LocalDateTime ldt = LocalDateTime.parse(isoLocalDateTime);
    ZonedDateTime zdt = ldt.atZone(zone);
    return zdt.toInstant();
  }

  private LocalDate parseLocalDate(String isoLocalDate) {
    return LocalDate.parse(isoLocalDate);
  }

  private int findClosingQuote(String s) {
    for (int i = 1; i < s.length(); i++) {
      if (s.charAt(i) == '"') {
        return i;
      }
    }
    return -1;
  }

  private int indexOfKeyword(String haystack, String[] needles) {
    int best = -1;
    for (String n : needles) {
      int idx = haystack.indexOf(n);
      if (idx >= 0 && (best < 0 || idx < best)) {
        best = idx;
      }
    }
    return best;
  }
}
