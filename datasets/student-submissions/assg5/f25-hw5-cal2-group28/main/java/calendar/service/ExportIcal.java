package calendar.service;

import calendar.model.Events;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Class to export a calendar's events to a standard .ics (iCal) file.
 */
public class ExportIcal {

  /**
   * iCal date format (e.g., 20250120).
   */
  private static final DateTimeFormatter ICAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  /**
   * iCal date-time format (e.g., 20250120T143000).
   */
  private static final DateTimeFormatter ICAL_DATETIME_FORMAT = DateTimeFormatter.ofPattern(
      "yyyyMMdd'T'HHmmss");

  /**
   * Exports the given collection of events to a standard .ics (iCal) file.
   * This is the method called by the Export controller.
   *
   * @param events   The collection of events to export.
   * @param fileName The name of the file to create (e.g., "myCal.ics").
   * @return The absolute path of the generated file as a String.
   * @throws IOException If an I/O error occurs writing to the file.
   */
  public String export(Collection<Events> events, String fileName) throws IOException {
    Path outputDir = Paths.get("exports");
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }
    Path path = outputDir.resolve(fileName).toAbsolutePath();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
      // Write iCal Header
      writer.write("BEGIN:VCALENDAR");
      writer.newLine();
      writer.write("VERSION:2.0");
      writer.newLine();
      writer.write("PRODID:-//Calendar App//EN");
      writer.newLine();

      // Get a single timestamp for all events
      String timestamp = LocalDateTime.now().format(ICAL_DATETIME_FORMAT);

      // Write each event
      for (Events event : events) {
        writer.write("BEGIN:VEVENT");
        writer.newLine();

        // Handle All-Day vs. Timed events
        if (event.isAllDay()) {
          // The 'Events' constructor for all-day sets start to 8:00 and end to 17:00.
          String startDate = event.getStartTime().toLocalDate().format(ICAL_DATE_FORMAT);
          writer.write("DTSTART;VALUE=DATE:" + startDate);
          writer.newLine();

          String endDate = event.getStartTime().toLocalDate().plusDays(1).format(ICAL_DATE_FORMAT);
          writer.write("DTEND;VALUE=DATE:" + endDate);
          writer.newLine();
        } else {
          String startTime = event.getStartTime().format(ICAL_DATETIME_FORMAT);
          writer.write("DTSTART:" + startTime);
          writer.newLine();
          String endTime = event.getEndTime().format(ICAL_DATETIME_FORMAT);
          writer.write("DTEND:" + endTime);
          writer.newLine();
        }

        // Write event details, escaping special characters
        writer.write("SUMMARY:" + escapeIcalText(event.getSubject()));
        writer.newLine();

        if (event.getDescription() != null) {
          writer.write("DESCRIPTION:" + escapeIcalText(event.getDescription()));
          writer.newLine();
        }
        if (event.getLocation() != null) {
          writer.write("LOCATION:" + escapeIcalText(event.getLocation()));
          writer.newLine();
        }

        writer.write("CLASS:" + event.getStatus().toString());
        writer.newLine();

        writer.write("END:VEVENT");
        writer.newLine();
      }

      // Write iCal Footer
      writer.write("END:VCALENDAR");
    }

    return path.toString();
  }

  /**
   * Escapes text for iCal format (handles commas, semicolons, newlines, and backslashes).
   *
   * @param text The text to escape.
   * @return The escaped text.
   */
  private String escapeIcalText(String text) {
    // Per iCal spec, escape backslashes, semicolons, commas, and newlines
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }
}