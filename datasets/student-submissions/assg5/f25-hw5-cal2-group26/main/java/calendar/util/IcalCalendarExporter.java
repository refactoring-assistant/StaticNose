package calendar.util;

import calendar.model.CalendarInterface;
import calendar.model.EventInterface;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Exports calendar events to an iCalendar (.ics) file with timezone support.
 */
public class IcalCalendarExporter implements CalendarExporterInterface {

  private static final DateTimeFormatter DATE_TIME_UTC_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  @Override
  public void export(CalendarInterface calendar, String filePath) throws IOException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }

    try (Writer writer = new FileWriter(filePath, false)) {
      writer.write("BEGIN:VCALENDAR\r\n");
      writer.write("VERSION:2.0\r\n");
      writer.write("PRODID:-//MyCalendarApp//MyCalendarApp 1.0//EN\r\n");
      writer.write("CALSCALE:GREGORIAN\r\n");

      for (EventInterface event : calendar.getAllCalendarEvents()) {
        writeEvent(writer, event);
      }

      writer.write("END:VCALENDAR\r\n");
    }
  }

  private void writeEvent(Writer writer, EventInterface event) throws IOException {
    writer.write("BEGIN:VEVENT\r\n");
    writer.write("UID:" + generateUid(event) + "\r\n");
    writer.write("DTSTAMP:" + formatDateTimeUtc(ZonedDateTime.now()) + "\r\n");
    writer.write("SUMMARY:" + escapeAndFold(event.getSubject()) + "\r\n");

    if (event.isAllDay()) {
      writer.write("DTSTART;VALUE=DATE:"
          + event.getStart().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "\r\n");
      if (event.getEnd() != null) {
        writer.write("DTEND;VALUE=DATE:"
            + event.getEnd().toLocalDate().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)
            + "\r\n");
      }
    } else {
      writer.write("DTSTART:" + formatDateTimeUtc(event.getStart()) + "\r\n");
      if (event.getEnd() != null) {
        writer.write("DTEND:" + formatDateTimeUtc(event.getEnd()) + "\r\n");
      }
    }

    if (event.getLocation() != null && !event.getLocation().isBlank()) {
      writer.write("LOCATION:" + escapeAndFold(event.getLocation()) + "\r\n");
    }

    if (event.getDescription() != null && !event.getDescription().isBlank()) {
      writer.write("DESCRIPTION:" + escapeAndFold(event.getDescription()) + "\r\n");
    }

    if (event.getStatus() != null) {
      String className = event.getStatus().name().toUpperCase();
      if (className.equals("PRIVATE") || className.equals("PUBLIC")
          || className.equals("CONFIDENTIAL")) {
        writer.write("CLASS:" + className + "\r\n");
      }
    }

    writer.write("END:VEVENT\r\n");
  }

  private String formatDateTimeUtc(ZonedDateTime dt) {
    return dt.withZoneSameInstant(ZoneId.of("UTC")).format(DATE_TIME_UTC_FORMAT);
  }

  private String generateUid(EventInterface event) {
    return UUID.randomUUID().toString() + "@mycalendarapp.com";
  }

  private String escapeText(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }

  private String escapeAndFold(String text) {
    String escaped = escapeText(text);

    if (escaped.length() <= 75) {
      return escaped;
    }

    StringBuilder folded = new StringBuilder();
    int pos = 0;
    folded.append(escaped, 0, Math.min(75, escaped.length()));
    pos = 75;
    while (pos < escaped.length()) {
      folded.append("\r\n ");
      int end = Math.min(pos + 74, escaped.length());
      folded.append(escaped, pos, end);
      pos = end;
    }

    return folded.toString();
  }
}