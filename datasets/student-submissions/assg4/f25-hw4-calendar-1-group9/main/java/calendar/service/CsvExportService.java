package calendar.service;

import calendar.exception.CalendarException;
import calendar.model.EventStatus;
import calendar.model.InCalendar;
import calendar.model.InEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Service for exporting calendar to CSV format.
 * All date/times are assumed to be in EST (America/New_York) timezone.
 * CSV format includes headers: Subject, Start Date, Start Time, End Date, End Time,
 * All Day Event, Description, Location, Private.
 */
public class CsvExportService implements InExportService {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

  private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");

  private static final String CSV_HEADER =
      "Subject,Start Date,Start Time,End Date,End Time,"
          + "All Day Event,Description,Location,Private";

  private final InCalendar calendar;

  /**
   * Constructs a CsvExportService with a calendar.
   *
   * @param calendar the calendar to export.
   * @throws IllegalArgumentException if calendar is null.
   */
  public CsvExportService(InCalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  /**
   * Gets the timezone used for all calendar operations.
   *
   * @return EST timezone (America/New_York).
   */
  public static ZoneId getTimezone() {
    return EST_ZONE;
  }

  @Override
  public Path exportToCsv(Path outputPath) throws CalendarException {
    if (outputPath == null) {
      throw new IllegalArgumentException("Output path cannot be null");
    }

    try {
      if (outputPath.getParent() != null) {
        Files.createDirectories(outputPath.getParent());
      }

      try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
        writer.write(CSV_HEADER);
        writer.newLine();

        List<InEvent> events = calendar.getAllEvents();
        for (InEvent event : events) {
          writeEventToCsv(writer, event);
        }
      }

      return outputPath.toAbsolutePath();
    } catch (IOException e) {
      throw new CalendarException("Failed to export calendar to CSV: "
          + e.getMessage(), e);
    }
  }

  /**
   * Writes a single event to CSV format.
   * Format: Subject,Start Date,Start Time,End Date,End Time,
   * All Day Event,Description,Location,Private.
   *
   * @param writer the BufferedWriter to write to.
   * @param event  the event to write.
   * @throws IOException if writing fails.
   */
  private void writeEventToCsv(BufferedWriter writer, InEvent event) throws IOException {
    StringBuilder line = new StringBuilder();

    line.append(escapeCsvField(event.getSubject())).append(",");

    line.append(event.getStartDateTime().format(DATE_FORMAT)).append(",");

    line.append(event.getStartDateTime().format(TIME_FORMAT)).append(",");

    line.append(event.getEndDateTime().format(DATE_FORMAT)).append(",");

    line.append(event.getEndDateTime().format(TIME_FORMAT)).append(",");

    line.append(event.isAllDayEvent() ? "True" : "False").append(",");

    line.append(escapeCsvField(
        event.getDescription().orElse(""))).append(",");

    line.append(escapeCsvField(
        event.getLocation().orElse(""))).append(",");

    line.append(event.getStatus() == EventStatus.PRIVATE ? "True" : "False");

    writer.write(line.toString());
    writer.newLine();
  }

  /**
   * Escapes a CSV field according to RFC 4180 specification.
   * Rules:
   * - If field contains comma, quote, or newline: wrap in quotes.
   * - If field contains quotes: double them (e.g., " becomes "").
   * - Empty or null fields: return empty string.
   *
   * @param field the field to escape.
   * @return the escaped field, or empty string if null/empty.
   */
  private String escapeCsvField(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }

    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      String escaped = field.replace("\"", "\"\"");
      return "\"" + escaped + "\"";
    }

    return field;
  }
}