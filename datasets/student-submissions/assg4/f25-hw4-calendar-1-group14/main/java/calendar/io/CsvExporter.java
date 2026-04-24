package calendar.io;

import calendar.model.calendar.Icalendar;
import calendar.model.event.EventStatus;
import calendar.model.event.Ievent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 */
public class CsvExporter {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Export calendar to CSV file.
   *
   * @param calendar the calendar to export
   * @param filePath the output file path
   * @throws IOException if file writing fails
   */
  public static void exportCalendar(Icalendar calendar, String filePath) throws IOException {
    BufferedWriter writer = null;

    try {
      writer = new BufferedWriter(new FileWriter(filePath));

      writer.write("Subject,Start Date,Start Time,End "
          + "Date,End Time,All Day Event,Description,Location,Private");
      writer.newLine();

      List<Ievent> events = calendar.getAllEvents();

      for (Ievent event : events) {
        String csvLine = eventToCsv(event);
        writer.write(csvLine);
        writer.newLine();
      }

    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          // Ignore close error
        }
      }
    }
  }

  /**
   * Convert an event to CSV format.
   *
   * @param event the event to convert
   * @return CSV string representation of the event
   */
  private static String eventToCsv(Ievent event) {
    StringBuilder sb = new StringBuilder();

    sb.append(escapeCsv(event.getSubject()));
    sb.append(",");

    LocalDateTime start = event.getStartDateTime();
    sb.append(start.format(DATE_FORMATTER));
    sb.append(",");

    if (event.isAllDay()) {
      sb.append("");
    } else {
      sb.append(start.format(TIME_FORMATTER));
    }
    sb.append(",");

    LocalDateTime end = event.getEndDateTime();
    sb.append(end.format(DATE_FORMATTER));
    sb.append(",");

    if (event.isAllDay()) {
      sb.append("");
    } else {
      sb.append(end.format(TIME_FORMATTER));
    }
    sb.append(",");

    sb.append(event.isAllDay() ? "True" : "False");
    sb.append(",");

    String description = event.getDescription();
    sb.append(description != null ? escapeCsv(description) : "");
    sb.append(",");

    String location = event.getLocation();
    sb.append(location != null ? escapeCsv(location) : "");
    sb.append(",");

    boolean isPrivate = event.getStatus() == EventStatus.PRIVATE;
    sb.append(isPrivate ? "True" : "False");

    return sb.toString();
  }

  /**
   * Escape CSV special characters by wrapping in quotes if needed.
   *
   * @param value the value to escape
   * @return escaped value safe for CSV format
   */
  private static String escapeCsv(String value) {
    if (value == null) {
      return "";
    }

    boolean needsEscape = value.contains(",") || value.contains("\"") || value.contains("\n");

    if (!needsEscape) {
      return value;
    }

    String escaped = value.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }
}