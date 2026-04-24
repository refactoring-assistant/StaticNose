package calendar.controller.export;

import calendar.model.Calendar;
import calendar.model.EventSingle;
import calendar.model.utils.EventStatus;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implements the Exporter interface.
 */
public class CsvExporter implements Exporter {

  private static final String[] CSV_HEADERS = {
      "Subject", "Start Date", "Start Time", "End Date", "End Time",
      "All Day Event", "Description", "Location", "Private"
  };
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  /**
   * No-arg constructor. This class is now stateless.
   */
  public CsvExporter() {
  }

  @Override
  public String export(Calendar calendar, String filePath)
      throws IOException, IllegalArgumentException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null.");
    }
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be empty.");
    }

    List<EventSingle> events = calendar.getAllEvents();

    if (events.isEmpty()) {
      throw new IOException("No events to export.");
    }

    Path path = Paths.get(filePath);
    Path parent = path.getParent();

    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      writer.write(String.join(",", CSV_HEADERS));
      writer.newLine();

      for (EventSingle event : events) {
        writer.write(formatEvent(event));
        writer.newLine();
      }
    }

    return path.toAbsolutePath().toString();
  }

  private String formatEvent(EventSingle event) {
    String subject = escapeCsv(event.getSubject());
    String startDate = event.getStart().toLocalDate().format(DATE_FORMATTER);
    String startTime = event.getStart().toLocalTime().format(TIME_FORMATTER);
    String endDate = event.getEnd().toLocalDate().format(DATE_FORMATTER);
    String endTime = event.getEnd().toLocalTime().format(TIME_FORMATTER);
    String allDay = isAllDay(event) ? "True" : "False";
    String description = escapeCsv(event.getDescription());
    String location = escapeCsv(event.getLocation());
    String isPrivate = event.getStatus() == EventStatus.PRIVATE ? "True" : "False";

    return String.join(",", subject, startDate, startTime, endDate, endTime,
        allDay, description, location, isPrivate);
  }

  private boolean isAllDay(EventSingle event) {
    LocalDateTime start = event.getStart();
    LocalDateTime end = event.getEnd();

    return start.getHour() == 8
        && start.getMinute() == 0
        && end.getHour() == 17
        && end.getMinute() == 0
        && start.toLocalDate().equals(end.toLocalDate());
  }

  private String escapeCsv(String field) {
    if (field == null || field.trim().isEmpty()) {
      return "";
    }
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      field = field.replace("\"", "\"\"");
      return "\"" + field + "\"";
    }
    return field;
  }
}