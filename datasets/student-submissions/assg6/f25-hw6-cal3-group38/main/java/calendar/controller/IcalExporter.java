package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendar events to iCal format.
 */
public class IcalExporter {

  /**
   * Exports events to iCal format.
   *
   * @param events the events to export
   * @param fileName the output file name
   * @return the absolute file path
   * @throws IOException if file writing fails
   */
  public String exportToIcal(List<Event> events, String fileName) throws IOException {
    String filePath = Paths.get(fileName).toAbsolutePath().toString();

    try (FileWriter writer = new FileWriter(filePath)) {
      // Write iCal header
      writer.write("BEGIN:VCALENDAR\n");
      writer.write("VERSION:2.0\n");
      writer.write("PRODID:-//Calendar Application//EN\n");

      // Write events
      for (Event event : events) {
        writer.write(formatEventForIcal(event));
      }

      // Write iCal footer
      writer.write("END:VCALENDAR\n");
    }

    return filePath;
  }

  /**
   * formatEventForIcal.
   *
   * @param event event
   * @return return
   */
  public String formatEventForIcal(Event event) {
    StringBuilder ical = new StringBuilder();

    ical.append("BEGIN:VEVENT\n");

    // Generate unique ID
    ical.append("UID:").append(UUID.randomUUID()).append("@calendarapp\n");

    // Subject/Summary
    ical.append("SUMMARY:").append(escapeIcalValue(event.getSubject())).append("\n");

    // Start and end times
    ical.append("DTSTART:").append(formatIcalDateTime(event.getStartDateTime())).append("\n");
    ical.append("DTEND:").append(formatIcalDateTime(event.getEndDateTime())).append("\n");

    // Description
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      ical.append("DESCRIPTION:").append(escapeIcalValue(event.getDescription())).append("\n");
    }

    // Location
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      ical.append("LOCATION:").append(escapeIcalValue(event.getLocation())).append("\n");
    }

    // Status (CLASS in iCal)
    if (event.getStatus() == EventStatus.PRIVATE) {
      ical.append("CLASS:PRIVATE\n");
    } else {
      ical.append("CLASS:PUBLIC\n");
    }

    // Timestamp (current time)
    ical.append("DTSTAMP:").append(formatIcalDateTime(java.time.LocalDateTime.now())).append("\n");

    ical.append("END:VEVENT\n");

    return ical.toString();
  }

  /**
   * formatIcalDateTime.
   *
   * @param dateTime dateTime
   * @return return
   */
  public String formatIcalDateTime(java.time.LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
  }

  /**
   * escapeIcalValue.
   *
   * @param value value
   * @return return
   */
  public String escapeIcalValue(String value) {
    if (value == null) {
      return "";
    }
    // Basic escaping for iCal values
    return value.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }
}