package calendar.exporter;

import calendar.model.IntEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 * The CSV format includes headers and properly escaped values.
 */
public class CsvExporter implements CalendarExporter {
  private static final String EXPORT_DIR = "exports";
  private static final String[] SUPPORTED_EXTENSIONS = {"csv"};

  @Override
  public String export(List<IntEvent> events, String fileName, String calendarName) {
    try {
      File directory = new File(EXPORT_DIR);
      directory.mkdirs();

      // Construct full file path with the default directory
      String filePath = EXPORT_DIR + File.separator + fileName;
      File file = new File(filePath);

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        // Write CSV header
        writer.write("Subject,Start Date,Start Time,"
            + "End Date,End Time,Description,Location,Status\n");

        // Write events sorted by start date/time
        events.stream()
            .sorted(Comparator.comparing(IntEvent::getStartDate)
                .thenComparing(IntEvent::getStartTime))
            .forEach(e -> {
              try {
                writer.write(formatEventAsCsv(e));
                writer.write("\n");
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
      }

      return file.getAbsolutePath();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to export calendar to CSV: " + e.getMessage());
    }
  }

  @Override
  public String[] getSupportedExtensions() {
    return SUPPORTED_EXTENSIONS;
  }

  /**
   * Formats an event as a CSV row.
   *
   * @param event the event to format
   * @return the CSV-formatted string
   */
  private String formatEventAsCsv(IntEvent event) {
    return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
        escapeCsv(event.getSubject()),
        event.getStartDate(),
        event.getStartTime(),
        event.getEndDate(),
        event.getEndTime(),
        escapeCsv(event.getDescription() != null ? event.getDescription() : ""),
        escapeCsv(event.getLocation() != null ? event.getLocation().name() : ""),
        event.getStatus() != null ? event.getStatus().name() : "");
  }

  /**
   * Escapes special characters in CSV values.
   *
   * @param value the value to escape
   * @return the escaped value
   */
  private String escapeCsv(String value) {
    return value.replace("\"", "\"\"");
  }
}

