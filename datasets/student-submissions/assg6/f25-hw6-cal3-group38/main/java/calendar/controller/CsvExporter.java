package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 */
public class CsvExporter {

  /**
   * export to csv.
   *
   * @param events event.
   * @param fileName filename.
   * @return filepath.
   * @throws IOException error.
   */
  public String exportToCsv(List<Event> events, String fileName) throws IOException {
    String filePath = Paths.get(fileName).toAbsolutePath().toString();

    try (FileWriter writer = new FileWriter(filePath)) {
      // Write header
      writer.write("Subject,Start Date,Start Time,End "
          + "Date,End Time,All Day Event,Description,Location,Private\n");

      // Write events
      for (Event event : events) {
        writer.write(formatEventForCsv(event) + "\n");
      }
    }

    return filePath;
  }

  /**
   * formatEventForCsv.
   *
   * @param event event
   * @return return
   */
  public String formatEventForCsv(Event event) {
    String subject = escapeCsv(event.getSubject());
    String startDate = event.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
    String startTime = event.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
    String endDate = event.getEndDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
    String endTime = event.getEndDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME);

    // Fix all-day event detection
    String allDayEvent = event.isAllDayEvent() ? "True" : "False";

    // For all-day events, Google Calendar expects empty time fields
    if (event.isAllDayEvent()) {
      startTime = "";
      endTime = "";
    }

    String description = escapeCsv(event.getDescription());
    String location = escapeCsv(event.getLocation());
    String isPrivate = event.getStatus() == EventStatus.PRIVATE ? "True" : "False";

    return String.join(",",
        subject, startDate, startTime, endDate, endTime,
        allDayEvent, description, location, isPrivate);
  }

  /**
   * escapeCsv.
   *
   * @param value value
   * @return return
   */
  public String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    // Remove any existing quotes and escape properly
    String cleaned = value.replace("\"", "");

    // Wrap in quotes if contains comma or if it's empty
    if (cleaned.contains(",") || cleaned.isEmpty()) {
      return "\"" + cleaned + "\"";
    }
    return cleaned;
  }
}