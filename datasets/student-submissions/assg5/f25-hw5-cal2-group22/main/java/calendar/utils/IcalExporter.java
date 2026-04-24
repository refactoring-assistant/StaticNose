package calendar.utils;

import calendar.model.AbstractEvent;
import calendar.model.SingleEvent;
import calendar.model.enums.EventStatus;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendar events to iCal format compatible with Google Calendar.
 * Follows RFC 5545 (iCalendar) specification.
 */
public class IcalExporter implements Iexporter {

  private static final DateTimeFormatter iCalFormat =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter icalUtcFormat =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  @Override
  public String export(List<AbstractEvent> events, String filename) {
    try {
      Path path = Paths.get(filename).toAbsolutePath();

      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }

      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        writer.write("BEGIN:VCALENDAR\r\n");
        writer.write("VERSION:2.0\r\n");
        writer.write("PRODID:-//Calendar Application//EN\r\n");
        writer.write("CALSCALE:GREGORIAN\r\n");
        writer.write("METHOD:PUBLISH\r\n");

        for (AbstractEvent event : events) {
          writeEvent(writer, event);
        }

        writer.write("END:VCALENDAR\r\n");
      }
      return "Calendar exported successfully to: " + path;
    } catch (IOException e) {
      return "Error exporting iCal: " + e.getMessage();
    }
  }

  /**
   * Writes a single event in iCal format.
   *
   * @param writer the BufferedWriter to write to
   * @param event  the event to write
   * @throws IOException if writing fails
   */
  private void writeEvent(BufferedWriter writer, AbstractEvent event) throws IOException {
    writer.write("BEGIN:VEVENT\r\n");

    String uid = UUID.randomUUID() + "@calendar-app";
    writer.write("UID:" + uid + "\r\n");


    writer.write("DTSTART;TZID=" + event.getTimeZone().getId() + ":"
        + event.getStart().format(iCalFormat) + "\r\n");
    writer.write("DTEND;TZID=" + event.getTimeZone().getId() + ":"
        + event.getEnd().format(iCalFormat) + "\r\n");

    writer.write("SUMMARY:" + escapeText(event.getSubject()) + "\r\n");

    if (!event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeText(event.getDescription()) + "\r\n");
    }

    if (event.getLocation() != null && event.getLocation().toString() != "NONE") {
      writer.write("LOCATION:" + escapeText(event.getLocation().toString()) + "\r\n");
    }

    String classification = (event.getStatus() == EventStatus.PRIVATE) ? "PRIVATE" : "PUBLIC";
    writer.write("CLASS:" + classification + "\r\n");

    writer.write("DTSTAMP:" + ZonedDateTime.now().format(icalUtcFormat) + "\r\n");

    writer.write("END:VEVENT\r\n");
  }

  /**
   * Escapes special characters in iCal text fields.
   * According to RFC 5545, backslash, semicolon, comma, and newline need escaping.
   *
   * @param text the text to escape
   * @return escaped text
   */
  private String escapeText(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }
}