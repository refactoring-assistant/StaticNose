package calendar.exporter;

import calendar.model.IntEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendar events to iCal format (RFC 5545).
 * The iCal format is compatible with Google Calendar and other calendar applications.
 */
public class IcalExporter implements CalendarExporter {
  private static final String EXPORT_DIR = "exports";
  private static final String[] SUPPORTED_EXTENSIONS = {"ical", "ics"};
  private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  @Override
  public String export(List<IntEvent> events, String fileName, String calendarName) {
    try {
      File directory = new File(EXPORT_DIR);
      directory.mkdirs();

      // Construct full file path with the default directory
      String filePath = EXPORT_DIR + File.separator + fileName;
      File file = new File(filePath);

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        // Write iCal header
        writer.write("BEGIN:VCALENDAR\r\n");
        writer.write("VERSION:2.0\r\n");
        writer.write("PRODID:-//Calendar Application//EN\r\n");
        writer.write("CALSCALE:GREGORIAN\r\n");
        writer.write("METHOD:PUBLISH\r\n");
        writer.write("X-WR-CALNAME:" + escapeIcal(calendarName) + "\r\n");
        writer.write("X-WR-TIMEZONE:America/New_York\r\n");

        // Write events sorted by start date/time
        events.stream()
            .sorted(Comparator.comparing(IntEvent::getStartDate)
                .thenComparing(IntEvent::getStartTime))
            .forEach(e -> {
              try {
                writeEvent(writer, e);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });

        // Write iCal footer
        writer.write("END:VCALENDAR\r\n");
      }

      return file.getAbsolutePath();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to export calendar to iCal: " + e.getMessage());
    }
  }

  @Override
  public String[] getSupportedExtensions() {
    return SUPPORTED_EXTENSIONS;
  }

  /**
   * Writes a single event in iCal format.
   *
   * @param writer the writer to write to
   * @param event  the event to write
   * @throws IOException if writing fails
   */
  private void writeEvent(BufferedWriter writer, IntEvent event) throws IOException {
    writer.write("BEGIN:VEVENT\r\n");

    // Generate unique ID for the event
    String uid = UUID.randomUUID().toString() + "@calendar-app";
    writer.write("UID:" + uid + "\r\n");

    // Write start date/time
    LocalDateTime startDateTime = LocalDateTime.of(
        event.getStartDate().getYear(),
        event.getStartDate().getMonth(),
        event.getStartDate().getDay(),
        event.getStartTime().getHour(),
        event.getStartTime().getMinute()
    );
    writer.write("DTSTART:" + startDateTime.format(ICAL_DATE_TIME_FORMAT) + "\r\n");

    // Write end date/time
    LocalDateTime endDateTime = LocalDateTime.of(
        event.getEndDate().getYear(),
        event.getEndDate().getMonth(),
        event.getEndDate().getDay(),
        event.getEndTime().getHour(),
        event.getEndTime().getMinute()
    );
    writer.write("DTEND:" + endDateTime.format(ICAL_DATE_TIME_FORMAT) + "\r\n");

    // Write timestamp (current time)
    LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    writer.write("DTSTAMP:" + now.format(ICAL_DATE_TIME_FORMAT) + "Z\r\n");

    // Write summary (subject)
    writer.write("SUMMARY:" + escapeIcal(event.getSubject()) + "\r\n");

    // Write description if present
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeIcal(event.getDescription()) + "\r\n");
    }

    // Write location if present
    if (event.getLocation() != null) {
      writer.write("LOCATION:" + escapeIcal(event.getLocation().name()) + "\r\n");
    }

    // Write status if present
    if (event.getStatus() != null) {
      // Map our status to iCal status
      String icalStatus = mapStatusToIcal(event.getStatus().name());
      writer.write("STATUS:" + icalStatus + "\r\n");

      // Set class based on status
      String icalClass = event.getStatus().name().equals("PRIVATE") ? "PRIVATE" : "PUBLIC";
      writer.write("CLASS:" + icalClass + "\r\n");
    }

    writer.write("END:VEVENT\r\n");
  }

  /**
   * Maps our status enum to iCal status values.
   *
   * @param status our status value
   * @return iCal status value
   */
  private String mapStatusToIcal(String status) {
    // iCal supports: TENTATIVE, CONFIRMED, CANCELLED
    // We'll map PUBLIC/PRIVATE to CONFIRMED
    return "CONFIRMED";
  }

  /**
   * Escapes special characters in iCal values.
   * According to RFC 5545, we need to escape: \ ; , and newlines
   *
   * @param value the value to escape
   * @return the escaped value
   */
  private String escapeIcal(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
        .replace("\r", "");
  }
}

