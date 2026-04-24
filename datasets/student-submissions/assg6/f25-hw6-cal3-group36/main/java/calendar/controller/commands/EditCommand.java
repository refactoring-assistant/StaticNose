package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.model.InterfaceEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Single event or the series event editing.
 */
public class EditCommand implements Command {
  private final String raw;
  private static final DateTimeFormatter DATE_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ROOT);

  /**
   * Constructor.
   *
   * @param input the raw edit command string
   */
  public EditCommand(String input) {
    this.raw = input.trim();
  }

  @Override
  public String execute(CalendarSystemModel model) {
    String lower = raw.toLowerCase(Locale.ROOT);
    boolean seriesAll = lower.startsWith("edit series");
    boolean eventsFuture = !seriesAll && lower.startsWith("edit events");
    boolean single = lower.startsWith("edit event");
    if (!single && !eventsFuture && !seriesAll) {
      throw new IllegalArgumentException("Invalid edit syntax.");
    }

    String withoutPrefix;
    if (seriesAll) {
      withoutPrefix = raw.substring("edit series".length()).trim();
    } else if (eventsFuture) {
      withoutPrefix = raw.substring("edit events".length()).trim();
    } else {
      withoutPrefix = raw.substring("edit event".length()).trim();
    }

    String[] tokens = withoutPrefix.split("\\s+", 2);
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Missing property in edit command.");
    }
    String property = tokens[0].toLowerCase(Locale.ROOT);
    String rest = tokens[1].trim();

    String subject;
    String afterSubject;

    if (rest.startsWith("\"")) {
      int endQuote = rest.indexOf('"', 1);
      if (endQuote < 0) {
        throw new IllegalArgumentException("Unterminated subject.");
      }
      subject = rest.substring(1, endQuote);
      afterSubject = rest.substring(endQuote + 1).trim();
    } else {
      int fromIdx = rest.toLowerCase(Locale.ROOT).indexOf(" from ");
      if (fromIdx < 0) {
        throw new IllegalArgumentException("Missing 'from' in edit command.");
      }
      subject = rest.substring(0, fromIdx).trim();
      afterSubject = rest.substring(fromIdx + " from ".length()).trim();
    }

    String lowerAfter = afterSubject.toLowerCase(Locale.ROOT);
    if (lowerAfter.startsWith("from ")) {
      afterSubject = afterSubject.substring(5).trim();
    }

    String startStr;
    String afterStart;
    int withIdx = indexOfKeywordIgnoreCase(afterSubject, " with ");
    int toIdx = indexOfKeywordIgnoreCase(afterSubject, " to ");

    if (toIdx >= 0 && (withIdx < 0 || toIdx < withIdx)) {
      startStr = afterSubject.substring(0, toIdx).trim();
      afterStart = afterSubject.substring(toIdx + " to ".length()).trim();

      int withIdx2 = indexOfKeywordIgnoreCase(afterStart, " with ");
      if (withIdx2 < 0) {
        throw new IllegalArgumentException("Missing 'with' in edit command.");
      }

      String endStr = afterStart.substring(0, withIdx2).trim();
      String newValueRaw = afterStart.substring(withIdx2 + " with ".length()).trim();
      String newValue = stripOptionalQuotes(newValueRaw);

      return editWithRange(model, seriesAll, eventsFuture, property,
          subject, startStr, endStr, newValue);
    } else {
      if (withIdx < 0) {
        throw new IllegalArgumentException("Missing 'with' in edit command.");
      }
      startStr = afterSubject.substring(0, withIdx).trim();
      String newValueRaw = afterSubject.substring(withIdx + " with ".length()).trim();
      String newValue = stripOptionalQuotes(newValueRaw);

      return editWithoutEnd(model, seriesAll, eventsFuture, property,
          subject, startStr, newValue);
    }
  }

  private String editWithRange(CalendarSystemModel model,
                               boolean seriesAll,
                               boolean eventsFuture,
                               String property,
                               String subject,
                               String startStr,
                               String endStr,
                               String newValue) {
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    Instant start = parseLocalDateTime(startStr, zone);
    Instant end = parseLocalDateTime(endStr, zone);

    InterfaceEvent existing = model.getActiveCalendar().findEvent(subject, start);
    if (existing == null) {
      throw new IllegalArgumentException("Event not found to edit.");
    }

    EventBuilder builder = new EventBuilder()
        .subject(existing.getSubject())
        .start(existing.getStart())
        .end(existing.getEnd())
        .description(existing.getDescription())
        .location(existing.getLocation())
        .isPublic(existing.isPublicEvent())
        .zone(existing.getZone());

    applyProperty(builder, property, newValue, zone, start, end);
    Event replacement = builder.build();

    if (seriesAll || eventsFuture) {
      boolean affectFuture = eventsFuture;
      boolean affectAll = seriesAll;
      model.editSeries(subject, start, replacement, affectFuture, affectAll);
      return "Series edited.";
    } else {
      model.editEvent(subject, start, replacement);
      return "Event edited.";
    }
  }

  private String editWithoutEnd(CalendarSystemModel model,
                                boolean seriesAll,
                                boolean eventsFuture,
                                String property,
                                String subject,
                                String startStr,
                                String newValue) {
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    Instant start = parseLocalDateTime(startStr, zone);

    InterfaceEvent existing = model.getActiveCalendar().findEvent(subject, start);
    if (existing == null) {
      throw new IllegalArgumentException("Event not found to edit.");
    }

    EventBuilder builder = new EventBuilder()
        .subject(existing.getSubject())
        .start(existing.getStart())
        .end(existing.getEnd())
        .description(existing.getDescription())
        .location(existing.getLocation())
        .isPublic(existing.isPublicEvent())
        .zone(existing.getZone());

    applyProperty(builder, property, newValue, zone, existing.getStart(), existing.getEnd());
    Event replacement = builder.build();

    if (seriesAll || eventsFuture) {
      boolean affectFuture = eventsFuture;
      boolean affectAll = seriesAll;
      model.editSeries(subject, start, replacement, affectFuture, affectAll);
      return "Series edited.";
    } else {
      model.editEvent(subject, start, replacement);
      return "Event edited.";
    }
  }

  private void applyProperty(EventBuilder builder,
                             String property,
                             String newValue,
                             ZoneId zone,
                             Instant oldStart,
                             Instant oldEnd) {
    if ("subject".equals(property)) {
      builder.subject(newValue);
    } else if ("start".equals(property)) {
      Instant newStart = parseLocalDateTime(newValue, zone);
      long duration = oldEnd.getEpochSecond() - oldStart.getEpochSecond();
      builder.start(newStart);
      builder.end(newStart.plusSeconds(duration));
    } else if ("end".equals(property)) {
      Instant newEnd = parseLocalDateTime(newValue, zone);
      builder.end(newEnd);
    } else if ("description".equals(property)) {
      builder.description(newValue);
    } else if ("location".equals(property)) {
      builder.location(newValue);
    } else if ("status".equals(property)) {
      boolean isPublic = "public".equalsIgnoreCase(newValue);
      builder.isPublic(isPublic);
    } else {
      throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }

  private Instant parseLocalDateTime(String text, ZoneId zone) {
    try {
      LocalDateTime ldt = LocalDateTime.parse(text, DATE_TIME_FMT);
      return ldt.atZone(zone).toInstant();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date/time: " + text);
    }
  }

  private int indexOfKeywordIgnoreCase(String haystack, String needle) {
    return haystack.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT));
  }

  private String stripOptionalQuotes(String value) {
    String v = value.trim();
    if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
      return v.substring(1, v.length() - 1);
    }
    return v;
  }
}