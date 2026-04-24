package calendar.service.export;

import calendar.model.Calendar;
import calendar.model.Event;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exports calendars to Google Calendar CSV format.
 */
public class CsvExporter {
  private static final String HEADER = "Subject,Start Date,Start Time,"
      + "End Date,End Time,All Day Event,Description,Location,Private";
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
  private static final int DEFAULT_END_HOUR = 17;

  /**
   * Exports calendar to CSV file.
   *
   * @param cal the calendar to export
   * @param filename the output filename
   * @return absolute path to exported file
   * @throws IOException if file write fails
   * @throws IllegalArgumentException if cal or filename is invalid
   */
  public String export(Calendar cal, String filename) throws IOException {
    if (cal == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be empty");
    }
    try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
      w.println(HEADER);
      for (Event e : cal.getAllEvents()) {
        w.println(formatEvent(e));
      }
    }
    return new File(filename).getAbsolutePath();
  }

  /**
   * Checks if filename has CSV extension.
   *
   * @param filename the filename to check
   * @return true if filename ends with .csv
   */
  public boolean supports(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".csv");
  }

  /**
   * Formats event as CSV line.
   *
   * @param e the event to format
   * @return CSV formatted string
   */
  private String formatEvent(Event e) {
    StringBuilder sb = new StringBuilder();
    sb.append(escape(e.getSubject())).append(",");
    sb.append(e.getStart().format(DATE_FMT)).append(",");
    sb.append(e.getStart().format(TIME_FMT)).append(",");
    ZonedDateTime end = e.getEnd() != null ? e.getEnd()
        : e.getStart().toLocalDate().atTime(DEFAULT_END_HOUR, 0).atZone(e.getStart().getZone());
    sb.append(end.format(DATE_FMT)).append(",");
    sb.append(end.format(TIME_FMT)).append(",");
    sb.append(e.isAllDay() ? "True" : "False").append(",");
    sb.append(escape(e.getDescription())).append(",");
    sb.append(escape(e.getLocation())).append(",");
    sb.append(e.isPublic() ? "False" : "True");
    return sb.toString();
  }

  /**
   * Escapes special CSV characters.
   *
   * @param val the value to escape
   * @return escaped value or empty string if null
   */
  private String escape(String val) {
    if (val == null) {
      return "";
    }
    if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
      return "\"" + val.replace("\"", "\"\"") + "\"";
    }
    return val;
  }
}