package calendar.export;

import calendar.model.CalendarEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A utility class dedicated to exporting calendar data to file formats.
 */
public class CalendarExporter {

  /**
   * The header for the Google Calendar CSV format.
   */
  private static final String
      CSV_HEADER = "Subject,Start Date,Start Time,End Date,End Time,Description,Location,Private";

  /**
   * Exports a list of events to a CSV file.
   *
   * @param events   The list of events to export.
   * @param filename The desired filename (e.g., "my_calendar.csv").
   * @return The absolute, platform-independent path of the created file.
   * @throws IOException if a file I/O error occurs.
   */
  public static Path exportTocsv(List<CalendarEvent> events, String filename) throws IOException {
    Path filePath = Paths.get(filename).toAbsolutePath();

    try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
      writer.write(CSV_HEADER);
      writer.newLine();

      for (CalendarEvent event : events) {
        writer.write(event.toCsvRow());
        writer.newLine();
      }
    }
    return filePath;
  }
}