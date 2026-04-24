package calendar.export;

import calendar.model.Exporter;
import calendar.model.impl.Event;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendar events to a CSV file compatible with Google Calendar import format.
 */
public class CsvExporter implements Exporter {

  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  @Override
  public void export(List<Event> events, Path file) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(file)) {
      bw.write(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,"
              + "Location,Private");
      bw.newLine();

      for (Event e : events) {
        String subject = csv(e.subject());
        String startDate = csv(DATE.format(e.start()));
        String endDate = csv(DATE.format(e.end()));

        String startTime;
        String endTime;
        if (e.allDay()) {
          startTime = "";
          endTime = "";
        } else {
          startTime = csv(TIME.format(e.start()));
          endTime = csv(TIME.format(e.end()));
        }

        String allDay = e.allDay() ? "True" : "False";
        String description = csv(e.description() == null ? "" : e.description());
        String location = csv(e.location() == null ? "" : e.location());
        String priv =
            (e.status() != null && e.status().name().equalsIgnoreCase("PRIVATE")) ? "True" :
                "False";
        StringBuilder row = new StringBuilder();
        row.append(subject).append(",")
            .append(startDate).append(",")
            .append(startTime).append(",")
            .append(endDate).append(",")
            .append(endTime).append(",")
            .append(allDay).append(",")
            .append(description).append(",")
            .append(location).append(",")
            .append(priv);

        bw.write(row.toString());
        bw.newLine();
      }
    }
  }

  @Override
  public void export(List<Event> events, String timezone, Path file) throws IOException {
    export(events, file);
  }

  /**
   * Escapes and quotes a string value for CSV output if necessary.
   *
   * @param s the string to escape.
   * @return a CSV-safe representation of the string.
   */
  private static String csv(String s) {
    if (s == null) {
      return "";
    }
    boolean needsQuote =
        s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
    if (!needsQuote) {
      return s;
    }
    String escaped = s.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }

}