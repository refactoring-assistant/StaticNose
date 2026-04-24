package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Facilitates the copy command between calendars.
 * Support single event and multievent copy.
 */
public class CopyCommand implements Command {

  private final String raw;

  private static final DateTimeFormatter DATE_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ROOT);

  /**
   * Copy command constructed from the user input.
   *
   * @param input the command line input.
   */
  public CopyCommand(String input) {
    this.raw = input.trim();
  }

  @Override
  public String execute(CalendarSystemModel model) {
    String lower = raw.toLowerCase(Locale.ROOT);
    if (lower.startsWith("copy event ")) {
      return handleCopySingle(model);
    } else if (lower.startsWith("copy events between ")) {
      return handleCopyBetween(model);
    } else if (lower.startsWith("copy events on ")) {
      return handleCopyOn(model);
    }
    throw new IllegalArgumentException("Invalid copy syntax: '" + raw + "'");
  }

  private String handleCopySingle(CalendarSystemModel model) {
    String rest = raw.substring("copy event".length()).trim();
    String subject;
    String afterSubject;
    if (rest.startsWith("\"")) {
      int endQuote = rest.indexOf('"', 1);
      if (endQuote < 0) {
        throw new IllegalArgumentException("Unterminated event name in copy event.");
      }
      subject = rest.substring(1, endQuote);
      afterSubject = rest.substring(endQuote + 1).trim();
    } else {
      String lowerRest = rest.toLowerCase(Locale.ROOT);
      int onIdx = lowerRest.indexOf(" on ");
      if (onIdx < 0) {
        throw new IllegalArgumentException("Missing 'on' in copy event.");
      }
      subject = rest.substring(0, onIdx).trim();
      afterSubject = rest.substring(onIdx + " on ".length()).trim();
    }
    String lowerAfter = afterSubject.toLowerCase(Locale.ROOT);
    if (lowerAfter.startsWith("on ")) {
      afterSubject = afterSubject.substring(3).trim();
      lowerAfter = afterSubject.toLowerCase(Locale.ROOT);
    }
    int targetIdx = lowerAfter.indexOf(" --target ");
    if (targetIdx < 0) {
      throw new IllegalArgumentException("Missing '--target' in copy event.");
    }
    String startStr = afterSubject.substring(0, targetIdx).trim();
    String afterTarget = afterSubject.substring(targetIdx + " --target ".length()).trim();
    String lowerAfterTarget = afterTarget.toLowerCase(Locale.ROOT);
    int toIdx = lowerAfterTarget.indexOf(" to ");
    if (toIdx < 0) {
      throw new IllegalArgumentException("Missing 'to' in copy event.");
    }
    String targetCalendar = afterTarget.substring(0, toIdx).trim();
    String newStartStr = afterTarget.substring(toIdx + " to ".length()).trim();

    ZoneId sourceZone = model.getActiveCalendar().getTimeZone();
    ZoneId targetZone = model.getCalendarTimeZone(targetCalendar);

    Instant start = parseDateTimeToInstant(startStr, sourceZone);
    Instant newStart = parseDateTimeToInstant(newStartStr, targetZone);
    model.copyEvent(subject, start, targetCalendar, newStart);
    return "Single event copied.";
  }

  private String handleCopyOn(CalendarSystemModel model) {
    String rest = raw.substring("copy events".length()).trim();
    String lowerRest = rest.toLowerCase(Locale.ROOT);
    if (!lowerRest.startsWith("on ")) {
      throw new IllegalArgumentException("Invalid copy events on syntax.");
    }
    String afterOn = rest.substring(3).trim();
    String lowerAfterOn = afterOn.toLowerCase(Locale.ROOT);
    int targetIdx = lowerAfterOn.indexOf(" --target ");
    if (targetIdx < 0) {
      throw new IllegalArgumentException("Missing '--target' in copy events on.");
    }
    String sourceDateStr = afterOn.substring(0, targetIdx).trim();
    String afterTarget = afterOn.substring(targetIdx + " --target ".length()).trim();
    String lowerAfterTarget = afterTarget.toLowerCase(Locale.ROOT);
    int toIdx = lowerAfterTarget.indexOf(" to ");
    if (toIdx < 0) {
      throw new IllegalArgumentException("Missing 'to' in copy events on.");
    }
    String targetCalendar = afterTarget.substring(0, toIdx).trim();
    String targetDateStr = afterTarget.substring(toIdx + " to ".length()).trim();
    LocalDate sourceDate = parseDate(sourceDateStr);
    LocalDate targetDate = parseDate(targetDateStr);
    model.copyEventsOn(sourceDate, targetCalendar, targetDate);
    return "Events copied on date.";
  }

  private String handleCopyBetween(CalendarSystemModel model) {
    String rest = raw.substring("copy events between".length()).trim();
    String lowerRest = rest.toLowerCase(Locale.ROOT);
    int andIdx = lowerRest.indexOf(" and ");
    if (andIdx < 0) {
      throw new IllegalArgumentException("Missing 'and' in copy events between.");
    }
    String startDateStr = rest.substring(0, andIdx).trim();
    String afterAnd = rest.substring(andIdx + " and ".length()).trim();
    String lowerAfterAnd = afterAnd.toLowerCase(Locale.ROOT);
    int targetIdx = lowerAfterAnd.indexOf(" --target ");
    if (targetIdx < 0) {
      throw new IllegalArgumentException("Missing '--target' in copy events between.");
    }
    String endDateStr = afterAnd.substring(0, targetIdx).trim();
    String afterTarget = afterAnd.substring(targetIdx + " --target ".length()).trim();
    String lowerAfterTarget = afterTarget.toLowerCase(Locale.ROOT);
    int toIdx = lowerAfterTarget.indexOf(" to ");
    if (toIdx < 0) {
      throw new IllegalArgumentException("Missing 'to' in copy events between.");
    }
    String targetCalendar = afterTarget.substring(0, toIdx).trim();
    String targetStartStr = afterTarget.substring(toIdx + " to ".length()).trim();
    LocalDate start = parseDate(startDateStr);
    LocalDate end = parseDate(endDateStr);
    LocalDate targetStart = parseDate(targetStartStr);
    model.copyEventsBetween(start, end, targetCalendar, targetStart);
    return "Events copied between dates.";
  }

  private Instant parseDateTimeToInstant(String text, ZoneId zone) {
    try {
      LocalDateTime ldt = LocalDateTime.parse(text, DATE_TIME_FMT);
      return ldt.atZone(zone).toInstant();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date/time: " + text);
    }
  }

  private LocalDate parseDate(String text) {
    try {
      return LocalDate.parse(text);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date: " + text);
    }
  }
}
