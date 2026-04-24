package calendar.util;

import calendar.model.CalendarEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting calendar events to CSV format.
 * Follows Google Calendar CSV format specifications.
 */
public class CsvExporter {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Exports events to a CSV file.
   *
   * @param events the events to export
   * @param fileName the output file name
   * @return the absolute path of the created file
   * @throws IOException if file cannot be written
   */
  public static String exportToCsv(List<CalendarEvent> events, String fileName)
      throws IOException {
    Path path = Paths.get(fileName);

    try (FileWriter writer = new FileWriter(path.toFile())) {
      // Write header
      writer.write("Subject,Start Date,Start Time,End Date,End Time,"
          + "All Day Event,Description,Location,Private\n");

      // Write events
      for (CalendarEvent event : events) {
        writer.write(formatEventAsCsv(event));
        writer.write("\n");
      }
    }

    return path.toAbsolutePath().toString();
  }

  private static String formatEventAsCsv(CalendarEvent event) {
    StringBuilder sb = new StringBuilder();

    // Subject
    sb.append(escapeCsv(event.getSubject())).append(",");

    // Start Date
    sb.append(event.getStartDateTime().format(DATE_FORMATTER)).append(",");

    // Start Time
    sb.append(event.getStartDateTime().format(TIME_FORMATTER)).append(",");

    // End Date
    sb.append(event.getEndDateTime().format(DATE_FORMATTER)).append(",");

    // End Time
    sb.append(event.getEndDateTime().format(TIME_FORMATTER)).append(",");

    // All Day Event
    sb.append(event.isAllDayEvent() ? "True" : "False").append(",");

    // Description
    String description = event.getDescription() != null
        ? event.getDescription()
        : "";
    sb.append(escapeCsv(description)).append(",");

    // Location
    String location = event.getLocation() != null
        ? event.getLocation()
        : "";
    sb.append(escapeCsv(location)).append(",");

    // Private (opposite of public status)
    String privateStatus = event.getStatus().equalsIgnoreCase("private")
        ? "True"
        : "False";
    sb.append(privateStatus);

    return sb.toString();
  }

  private static String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    // Escape quotes and wrap in quotes if needed
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }
}