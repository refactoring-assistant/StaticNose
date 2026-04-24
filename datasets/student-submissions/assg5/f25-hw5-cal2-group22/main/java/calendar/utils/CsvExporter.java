package calendar.utils;

import calendar.model.AbstractEvent;
import calendar.model.enums.EventLocation;
import calendar.model.enums.EventStatus;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 */

public class CsvExporter implements Iexporter {

  private static final String[] HEADERS = {
      "Subject", "Start Date", "Start Time", "End Date", "End Time",
      "All Day Event", "Description", "Location", "Status"
  };

  @Override
  public String export(List<AbstractEvent> events, String filename) {
    try {
      Path path = Paths.get(filename).toAbsolutePath();

      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }

      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        writer.write(String.join(",", HEADERS));
        writer.newLine();

        for (AbstractEvent e : events) {
          boolean allDay = isAllDay(e);
          String line = String.format("\"%s\",%s,%s,%s,%s,%s,\"%s\",\"%s\",%s",
              e.getSubject(),
              e.getStart().toLocalDate(),
              e.getStart().toLocalTime(),
              e.getEnd().toLocalDate(),
              e.getEnd().toLocalTime(),
              allDay,
              escapeDescription(e.getDescription()),
              (e.getLocation() == EventLocation.NONE) ? "" : e.getLocation(),
              (e.getStatus() == EventStatus.NONE) ? "" : e.getStatus());
          writer.write(line);
          writer.newLine();
        }
      }
      return "Calendar exported successfully to: " + path;
    } catch (IOException e) {
      return "Error exporting CSV: " + e.getMessage();
    }
  }

  /**
   * Checks if the event is an all-day event (8 AM to 5 PM on same day).
   *
   * @param e event to check
   * @return true if it is an all-day event
   */
  private boolean isAllDay(AbstractEvent e) {
    if (!e.getStart().toLocalDate().equals(e.getEnd().toLocalDate())) {
      return false;
    }

    LocalTime startTime = e.getStart().toLocalTime();
    LocalTime endTime = e.getEnd().toLocalTime();


    return startTime.equals(LocalTime.of(8, 0)) && endTime.equals(
        LocalTime.of(17, 0));
  }

  /**
   * Escapes description text for CSV format.
   *
   * @param description the description to escape
   * @return escaped description
   */
  private String escapeDescription(String description) {
    if (description == null) {
      return "";
    }
    return description.replace("\"", "\"\"");
  }


}
