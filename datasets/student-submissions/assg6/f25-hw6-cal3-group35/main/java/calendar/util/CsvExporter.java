package calendar.util;

import calendar.model.Event;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting calendar events to CSV format.
 *
 * <p>Handles file I/O and formatting separate from model logic.
 *
 * <p>Design Decision: Export moved from Model to Utility layer.
 *
 * <p>Previously, CalendarService.exportCsv() violated separation of concerns
 * by mixing business logic (event management) with I/O operations (file writing).
 *
 * <p>Current architecture:
 * - CalendarModel.getAllEvents() - Pure data retrieval (no side effects)
 * - CsvExporter.exportToCsv() - Utility for format conversion and file I/O
 * - ExportCommand.execute() - Controller orchestrating the operation
 *
 * <p>Benefits:
 * 1. Model remains focused on domain logic
 * 2. File operations can be tested/mocked separately
 * 3. Easy to add new export formats without changing model
 * 4. Follows Single Responsibility Principle
 * 5. Model can work with different I/O strategies (file, network, memory)
 */
public class CsvExporter {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Exports a list of events to a CSV file compatible with Google Calendar.
   *
   * <p>The CSV format follows Google Calendar import specifications:
   * - Header row with field names
   * - Date format: MM/DD/YYYY
   * - Time format: HH:MM (24-hour)
   * - Special handling for commas and quotes in text fields
   * - Private field: "True" for private events, empty otherwise
   *
   * @param events list of events to export (should be sorted)
   * @param target file path where CSV will be written
   * @return absolute path of the created file
   * @throws IOException if file writing fails
   */
  public static Path exportToCsv(List<Event> events, Path target) throws IOException {

    // Ensure directory exists
    if (target.getParent() != null) {
      Files.createDirectories(target.getParent());
    }

    // Overwrite existing file safely
    OpenOption[] opts = {
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    };

    try (BufferedWriter w = Files.newBufferedWriter(target, opts)) {
      w.write("Subject,Start Date,Start Time,End Date,End Time,Description,Location,Private");
      w.newLine();

      for (Event e : events) {
        String subject = escape(e.getSubject());
        String startDate = e.getStart().toLocalDate().format(DATE_FORMAT);
        String startTime = e.getStart().toLocalTime().format(TIME_FORMAT);
        String endDate = e.getEnd().toLocalDate().format(DATE_FORMAT);
        String endTime = e.getEnd().toLocalTime().format(TIME_FORMAT);
        String description = escape(e.getDescription().orElse(""));
        String location = escape(e.getLocation().orElse(""));
        String privateFlag = e.getStatus().map(s ->
            s.equalsIgnoreCase("private") ? "True" : ""
        ).orElse("");

        w.write(String.join(",", subject, startDate, startTime,
            endDate, endTime, description, location, privateFlag));
        w.newLine();
      }
    }

    return target.toAbsolutePath();
  }


  /**
   * Escapes a string for CSV format.
   *
   * <p>CSV escaping rules:
   * - If the value contains a comma or quote, wrap it in quotes
   * - If the value contains quotes, double them ("" -> """")
   * - Empty/null values become empty strings
   *
   * <p>Examples:
   * - "Meeting" -> Meeting
   * - "Team, meeting" -> "Team, meeting"
   * - "Say "hello"" -> "Say ""hello"""
   *
   * @param value string to escape
   * @return escaped string safe for CSV
   */
  private static String escape(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    if (value.contains(",") || value.contains("\"")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
