package calendar.util;

import calendar.model.Calendar;
import calendar.model.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendar events to iCalendar (iCal/ics) format.
 */
public class IcalExporter implements Exporter {
  private static final DateTimeFormatter ICAL_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter ICAL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  @Override
  public String export(Calendar calendar, String filename) throws IOException {
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be null or empty");
    }

    Path filePath = Paths.get(filename).toAbsolutePath();

    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      writeHeader(writer, calendar);

      List<Event> events = calendar.getAllEvents();
      for (Event event : events) {
        writeEvent(writer, event, calendar);
      }

      writeFooter(writer, calendar);
    }

    return filePath.toString();
  }

  @Override
  public void writeHeader(FileWriter writer, Calendar calendar) throws IOException {
    writer.write(
        "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Calendar Application//EN\r\n"
            + "CALSCALE:GREGORIAN\r\nMETHOD:PUBLISH\r\nX-WR-CALNAME:"
            + escapeString(calendar.getName()) + "\r\nX-WR-TIMEZONE:"
            + calendar.getTimezone().getId() + "\r\n");

    // Write timezone info
    writeTimezone(writer, calendar);
  }

  @Override
  public void writeEvent(FileWriter writer, Event event, Calendar calendar)
      throws IOException {
    writer.write("BEGIN:VEVENT\r\n");

    // UID - unique identifier (required)
    String uid = generateUid(event);
    writer.write("UID:" + uid + "\r\n");

    // DTSTAMP - creation timestamp (required)
    String timestamp = ZonedDateTime.now(calendar.getTimezone())
        .format(ICAL_DATETIME_FORMAT);
    writer.write("DTSTAMP:" + timestamp + "\r\n");

    // DTSTART - start date/time (required)
    if (event.isAllDay()) {
      String startDate = event.getStartDateTime().format(ICAL_DATE_FORMAT);
      writer.write("DTSTART;VALUE=DATE:" + startDate + "\r\n");
    } else {
      String startDateTime = event.getStartDateTime().format(ICAL_DATETIME_FORMAT);
      writer.write("DTSTART;TZID=" + calendar.getTimezone().getId()
          + ":" + startDateTime + "\r\n");
    }

    // DTEND - end date/time (optional)
    if (event.getEndDateTime() != null) {
      if (event.isAllDay()) {
        String endDate = event.getEndDateTime().toLocalDate().plusDays(1)
            .format(ICAL_DATE_FORMAT);
        writer.write("DTEND;VALUE=DATE:" + endDate + "\r\n");
      } else {
        String endDateTime = event.getEndDateTime().format(ICAL_DATETIME_FORMAT);
        writer.write("DTEND;TZID=" + calendar.getTimezone().getId()
            + ":" + endDateTime + "\r\n");
      }
    }

    // SUMMARY - event title (required)
    writer.write("SUMMARY:" + escapeString(event.getSubject()) + "\r\n");

    // DESCRIPTION - event description (optional)
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeString(event.getDescription()) + "\r\n");
    }

    // LOCATION - event location (optional)
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      writer.write("LOCATION:" + escapeString(event.getLocation()) + "\r\n");
    }

    // CLASS - privacy (optional)
    writer.write("CLASS:" + (event.isPrivate() ? "PRIVATE" : "PUBLIC") + "\r\n");

    // STATUS - event status
    writer.write("STATUS:CONFIRMED\r\n");

    // TRANSP - transparency (show as busy/free)
    writer.write("TRANSP:OPAQUE\r\n");

    writer.write("END:VEVENT\r\n");
  }

  @Override
  public void writeFooter(FileWriter writer, Calendar calendar) throws IOException {
    writer.write("END:VCALENDAR\r\n");
  }

  @Override
  public String escapeString(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    return value
        .replace("\\", "\\\\")   // Backslash
        .replace(",", "\\,")      // Comma
        .replace(";", "\\;")      // Semicolon
        .replace("\n", "\\n");    // Newline
  }

  /**
   * Writes timezone information to iCal file.
   * Simplified version - includes basic timezone info.
   */
  private void writeTimezone(FileWriter writer, Calendar calendar) throws IOException {
    writer.write("BEGIN:VTIMEZONE\r\nTZID:" + calendar.getTimezone().getId() + "\r\n"
        + "END:VTIMEZONE\r\n");
  }

  /**
   * Generates a unique ID for an event.
   */
  private String generateUid(Event event) {
    String base = event.getSubject() + "-"
        + event.getStartDateTime().toString();
    return UUID.nameUUIDFromBytes(base.getBytes()).toString()
        + "@calendar-app";
  }

  @Override
  public String getFileExtension() {
    return "ical";
  }

  @Override
  public String getFormatName() {
    return "iCalendar";
  }
}