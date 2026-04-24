package calendar.util;

import calendar.model.CalendarEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for exporting calendar events to iCal format.
 * Follows iCalendar (RFC 5545) specification.
 */
public class CalExporter {
  private static final DateTimeFormatter ICAL_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  /**
   * Exports events to an iCal file.
   *
   * @param events the events to export
   * @param fileName the output file name
   * @param timezone the calendar timezone
   * @return the absolute path of the created file
   * @throws IOException if file cannot be written
   */
  public static String exportToIcal(List<CalendarEvent> events,
                                    String fileName,
                                    ZoneId timezone)
      throws IOException {
    Path path = Paths.get(fileName);

    try (FileWriter writer = new FileWriter(path.toFile())) {
      // Write iCal header
      writer.write("BEGIN:VCALENDAR\n");
      writer.write("VERSION:2.0\n");
      writer.write("PRODID:-//CS5010 Calendar//EN\n");
      writer.write("CALSCALE:GREGORIAN\n");
      writer.write("METHOD:PUBLISH\n");

      // Write each event
      for (CalendarEvent event : events) {
        writer.write(formatEventAsIcal(event, timezone));
      }

      // Write iCal footer
      writer.write("END:VCALENDAR\n");
    }

    return path.toAbsolutePath().toString();
  }

  private static String formatEventAsIcal(CalendarEvent event, ZoneId timezone) {
    StringBuilder sb = new StringBuilder();

    sb.append("BEGIN:VEVENT\n");

    // UID - required unique identifier
    String uid = UUID.randomUUID().toString();
    sb.append("UID:").append(uid).append("\n");

    // DTSTART - start datetime
    ZonedDateTime startZoned = ZonedDateTime.of(event.getStartDateTime(), timezone);
    sb.append("DTSTART:").append(startZoned.format(ICAL_FORMATTER)).append("\n");

    // DTEND - end datetime
    ZonedDateTime endZoned = ZonedDateTime.of(event.getEndDateTime(), timezone);
    sb.append("DTEND:").append(endZoned.format(ICAL_FORMATTER)).append("\n");

    // SUMMARY - event subject
    sb.append("SUMMARY:").append(escapeIcalText(event.getSubject())).append("\n");

    // DESCRIPTION - optional
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      sb.append("DESCRIPTION:").append(escapeIcalText(event.getDescription())).append("\n");
    }

    // LOCATION - optional
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append("LOCATION:").append(escapeIcalText(event.getLocation())).append("\n");
    }

    // CLASS - public or private
    String classification = event.getStatus().equalsIgnoreCase("private")
        ? "PRIVATE"
        : "PUBLIC";
    sb.append("CLASS:").append(classification).append("\n");

    // DTSTAMP - timestamp
    sb.append("DTSTAMP:").append(ZonedDateTime.now(timezone).format(ICAL_FORMATTER))
        .append("\n");

    sb.append("END:VEVENT\n");

    return sb.toString();
  }

  private static String escapeIcalText(String text) {
    if (text == null) {
      return "";
    }
    // Escape special characters for iCal format
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }
}