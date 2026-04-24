package calendar.util;

import calendar.model.Calendar;
import calendar.model.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 */
public class CsvExporter implements Exporter {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("hh:mm a");

  @Override
  public String export(Calendar calendar, String filename) throws IOException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be null or empty");
    }

    Path filePath = Paths.get(filename).toAbsolutePath();

    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      writeHeader(writer, calendar);

      List<Event> events = calendar.getAllEvents();
      for (Event event : events) {
        writeEvent(writer, event, calendar);
      }

      writeFooter(writer, calendar);
    }

    return filePath.toString();
  }

  @Override
  public void writeHeader(FileWriter writer, Calendar calendar) throws IOException {
    writer.write(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,"
            + "Private\n");
  }

  @Override
  public void writeEvent(FileWriter writer, Event event, Calendar calendar)
      throws IOException {
    StringBuilder line = new StringBuilder();

    // Subject
    line.append(escapeString(event.getSubject())).append(",");

    // Start Date
    line.append(event.getStartDateTime().format(DATE_FORMATTER)).append(",");

    // Start Time
    if (event.isAllDay()) {
      line.append(",");
    } else {
      line.append(event.getStartDateTime().format(TIME_FORMATTER)).append(",");
    }

    // End Date
    if (event.getEndDateTime() != null) {
      line.append(event.getEndDateTime().format(DATE_FORMATTER)).append(",");
    } else {
      line.append(",");
    }

    // End Time
    if (event.getEndDateTime() != null && !event.isAllDay()) {
      line.append(event.getEndDateTime().format(TIME_FORMATTER)).append(",");
    } else {
      line.append(",");
    }

    // All Day Event
    line.append(event.isAllDay() ? "True" : "False").append(",");

    // Description
    line.append(escapeString(event.getDescription())).append(",");

    // Location
    line.append(escapeString(event.getLocation())).append(",");

    // Private
    line.append(event.isPrivate() ? "True" : "False");

    line.append("\n");
    writer.write(line.toString());
  }

  @Override
  public void writeFooter(FileWriter writer, Calendar calendar) throws IOException {
    // CSV doesn't need a footer
  }

  @Override
  public String escapeString(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    // If contains comma, quote, or newline, wrap in quotes and escape quotes
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }

  @Override
  public String getFileExtension() {
    return "csv";
  }

  @Override
  public String getFormatName() {
    return "CSV";
  }
}