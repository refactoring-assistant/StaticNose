package calendar.util;

import calendar.model.EventStatus;
import calendar.model.InEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for CSV export operations.
 * Centralizes CSV writing logic to avoid duplication.
 */
public final class CsvUtil {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("hh:mm a");

  private CsvUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Writes CSV header row.
   */
  public static void writeHeader(BufferedWriter writer) throws IOException {
    writer.write("Subject,Start Date,Start Time,End Date,End Time,"
        + "All Day Event,Description,Location,Private");
    writer.newLine();
  }

  /**
   * Writes a single event to CSV.
   */
  public static void writeEvent(BufferedWriter writer, InEvent event) throws IOException {
    StringBuilder line = new StringBuilder();

    line.append(escapeCsvField(event.getSubject())).append(",");
    line.append(event.getStartDateTime().format(DATE_FORMAT)).append(",");
    line.append(event.getStartDateTime().format(TIME_FORMAT)).append(",");
    line.append(event.getEndDateTime().format(DATE_FORMAT)).append(",");
    line.append(event.getEndDateTime().format(TIME_FORMAT)).append(",");
    line.append(event.isAllDayEvent() ? "True" : "False").append(",");
    line.append(escapeCsvField(event.getDescription().orElse(""))).append(",");
    line.append(escapeCsvField(event.getLocation().orElse(""))).append(",");
    line.append(event.getStatus() == EventStatus.PRIVATE ? "True" : "False");

    writer.write(line.toString());
    writer.newLine();
  }

  /**
   * Escapes CSV field values containing special characters.
   */
  public static String escapeCsvField(String field) {
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