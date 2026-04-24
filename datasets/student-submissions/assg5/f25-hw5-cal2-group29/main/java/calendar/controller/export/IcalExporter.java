package calendar.controller.export;

import calendar.model.Calendar;
import calendar.model.EventSingle;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Implements the Exporter interface.
 */
public class IcalExporter implements Exporter {

  private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  @Override
  public String export(Calendar calendar, String filePath)
      throws IOException, IllegalArgumentException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null.");
    }
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be empty.");
    }

    Path path = Paths.get(filePath);
    Path parent = path.getParent();

    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      writer.write("BEGIN:VCALENDAR");
      writer.newLine();
      writer.write("VERSION:2.0");
      writer.newLine();
      writer.write("PRODID:-//Gogeta's Calendar App//EN");
      writer.newLine();

      ZoneId zoneId = calendar.getZoneId();
      String tzid = zoneId.getId();

      for (EventSingle event : calendar.getAllEvents()) {
        writer.write("BEGIN:VEVENT");
        writer.newLine();

        writer.write("UID:" + UUID.randomUUID().toString());
        writer.newLine();
        writer.write(formatDateTime("DTSTART", event.getStart(), tzid));
        writer.newLine();
        writer.write(formatDateTime("DTEND", event.getEnd(), tzid));
        writer.newLine();
        writer.write("SUMMARY:" + escapeIcal(event.getSubject()));
        writer.newLine();
        if (event.getDescription() != null) {
          writer.write("DESCRIPTION:" + escapeIcal(event.getDescription()));
          writer.newLine();
        }
        if (event.getLocation() != null) {
          writer.write("LOCATION:" + escapeIcal(event.getLocation()));
          writer.newLine();
        }

        writer.write("END:VEVENT");
        writer.newLine();
      }

      writer.write("END:VCALENDAR");
      writer.newLine();
    }

    return path.toAbsolutePath().toString();
  }

  private String formatDateTime(String key, java.time.LocalDateTime ldt, String tzid) {
    return key + ";TZID=" + tzid + ":" + ldt.format(ICAL_DATE_TIME_FORMAT);
  }

  private String escapeIcal(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;")
        .replace("\n", "\\n");
  }
}