package calendar.controller.export;

import calendar.model.interfaces.EventReadOnly;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Exports calendar events into a CSV (Comma-Separated Values) file format.
 */
public class CsvExporter implements FileExporter {

  @Override
  public String export(String fileName, Map<LocalDate, List<EventReadOnly>> events) {

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    File file = new File(fileName);

    try (FileWriter fileWriter = new FileWriter(fileName)) {
      fileWriter.append("Subject,StartDate,StartTime").append(System.lineSeparator());

      for (Map.Entry<LocalDate, List<EventReadOnly>> entry : events.entrySet()) {
        for (EventReadOnly event : entry.getValue()) {
          fileWriter
              .append(event.getSubject()).append(",")
              .append(event.getStartDateTime().format(dateFormatter)).append(",")
              .append(formatTime(event.getStartDateTime().format(timeFormatter)))
              .append(System.lineSeparator());
        }
      }
      return file.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException("Error writing to file: " + fileName, e);
    }
  }

  /**
   * Helper to converts a 24-hour formatted time string into a 12-hour AM/PM format.
   *
   * @param time the time string in HH:mm format
   * @return the formatted time in 12-hour AM/PM format
   */
  private String formatTime(String time) {
    String[] parts = time.split(":");
    int hour = Integer.parseInt(parts[0]);
    String minutes = parts[1];

    if (hour == 0) {
      return "12:" + minutes + "AM";
    }
    if (hour < 12) {
      return hour + ":" + minutes + "AM";
    }
    if (hour == 12) {
      return hour + ":" + minutes + "PM";
    }
    return (hour - 12) + ":" + minutes + "PM";
  }
}
