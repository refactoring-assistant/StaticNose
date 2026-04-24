package calendar.view;

import calendar.model.Icalendar;
import calendar.model.Ievent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendars to CSV format.
 */
public class CsvExporter {
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * Exports a calendar to CSV file.
   */
  public void export(Icalendar calendar, Path filePath) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
      writer.write("Name,Start,End,Description");
      writer.newLine();

      List<Ievent> events = calendar.getEvents();
      for (Ievent event : events) {
        writer.write(escapeCsv(event.getName()));
        writer.write(",");
        writer.write(event.getStartDateTime().format(FORMATTER));
        writer.write(",");
        writer.write(event.getEndDateTime().format(FORMATTER));
        writer.write(",");
        writer.write(escapeCsv(event.getDescription()));
        writer.newLine();
      }
    }
  }

  private String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
