package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;
import calendar.model.InterfaceEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Handles the printing of event/s on a single date or within a specified date range.
 */
public class PrintCommand implements Command {
  private final String raw;
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter DATE_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ROOT);
  private static final DateTimeFormatter OUT_DATE =
      DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter OUT_TIME =
      DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

  /**
   * Constructing a command using user input.
   *
   * @param input the user input.
   */
  public PrintCommand(String input) {
    this.raw = input.trim();
  }

  @Override
  public String execute(CalendarSystemModel model) {
    String lower = raw.toLowerCase(Locale.ROOT);
    if (!lower.startsWith("print events")) {
      throw new IllegalArgumentException("Invalid print syntax.");
    }
    String rest = raw.substring("print events".length()).trim();
    String lowerRest = rest.toLowerCase(Locale.ROOT);
    if (lowerRest.startsWith("on ")) {
      String dateStr = rest.substring(3).trim();
      return printOnDate(model, dateStr);
    } else if (lowerRest.startsWith("from ")) {
      String afterFrom = rest.substring(5).trim();
      int toIdx = lowerRest.indexOf(" to ");
      if (toIdx < 0) {
        throw new IllegalArgumentException("Missing 'to' in range print.");
      }
      int toPosInRest = afterFrom.toLowerCase(Locale.ROOT).indexOf(" to ");
      if (toPosInRest < 0) {
        throw new IllegalArgumentException("Missing 'to' in range print.");
      }
      String fromStr = afterFrom.substring(0, toPosInRest).trim();
      String toStr = afterFrom.substring(toPosInRest + " to ".length()).trim();
      return printInRange(model, fromStr, toStr);
    } else {
      throw new IllegalArgumentException("Invalid print syntax.");
    }
  }

  private String printOnDate(CalendarSystemModel model, String dateStr) {
    LocalDate date;
    try {
      date = LocalDate.parse(dateStr, DATE_FMT);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date: " + dateStr);
    }
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    List<InterfaceEvent> events = model.getActiveCalendar().getEventsOn(date);
    if (events.isEmpty()) {
      return "No events on " + dateStr;
    }
    Collections.sort(events, Comparator.comparing(InterfaceEvent::getStart));
    StringBuilder sb = new StringBuilder();
    for (InterfaceEvent e : events) {
      LocalDateTime start = LocalDateTime.ofInstant(e.getStart(), zone);
      LocalDateTime end = LocalDateTime.ofInstant(e.getEnd(), zone);
      sb.append("- ")
          .append(e.getSubject())
          .append(" starting on ")
          .append(start.toLocalDate().format(OUT_DATE))
          .append(" at ")
          .append(start.toLocalTime().format(OUT_TIME))
          .append(", ending on ")
          .append(end.toLocalDate().format(OUT_DATE))
          .append(" at ")
          .append(end.toLocalTime().format(OUT_TIME));
      if (e.getLocation() != null && !e.getLocation().isEmpty()) {
        sb.append(" at ").append(e.getLocation());
      }
      sb.append(System.lineSeparator());
    }
    return sb.toString().trim();
  }

  private String printInRange(CalendarSystemModel model,
                              String fromStr,
                              String toStr) {
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    Instant fromInstant = parseDateTimeToInstant(fromStr, zone);
    Instant toInstant = parseDateTimeToInstant(toStr, zone);
    if (toInstant.isBefore(fromInstant)) {
      throw new IllegalArgumentException("End of range is before start.");
    }
    List<InterfaceEvent> events = model.getActiveCalendar().getEvents();
    events = new java.util.ArrayList<InterfaceEvent>(events);
    events.removeIf(e ->
        e.getEnd().isBefore(fromInstant) || e.getStart().isAfter(toInstant));
    if (events.isEmpty()) {
      return "No events in the given range.";
    }
    Collections.sort(events, Comparator.comparing(InterfaceEvent::getStart));
    StringBuilder sb = new StringBuilder();
    for (InterfaceEvent e : events) {
      LocalDateTime start = LocalDateTime.ofInstant(e.getStart(), zone);
      LocalDateTime end = LocalDateTime.ofInstant(e.getEnd(), zone);
      sb.append("- ")
          .append(e.getSubject())
          .append(" starting on ")
          .append(start.toLocalDate().format(OUT_DATE))
          .append(" at ")
          .append(start.toLocalTime().format(OUT_TIME))
          .append(", ending on ")
          .append(end.toLocalDate().format(OUT_DATE))
          .append(" at ")
          .append(end.toLocalTime().format(OUT_TIME));
      if (e.getLocation() != null && !e.getLocation().isEmpty()) {
        sb.append(" at ").append(e.getLocation());
      }
      sb.append(System.lineSeparator());
    }
    return sb.toString().trim();
  }

  private Instant parseDateTimeToInstant(String text, ZoneId zone) {
    try {
      LocalDateTime ldt = LocalDateTime.parse(text, DATE_TIME_FMT);
      return ldt.atZone(zone).toInstant();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date/time: " + text);
    }
  }
}