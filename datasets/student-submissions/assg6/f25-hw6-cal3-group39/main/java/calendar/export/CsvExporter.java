package calendar.export;

import calendar.model.InterfaceEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * This is the method that receives the list of events to export and exports them in csv format.
 */
public class CsvExporter implements Exporter {
  @Override
  public void export(List<InterfaceEvent> events, String filename) throws IOException {

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
      bw.write(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,"
              + "Status");
      bw.newLine();

      for (InterfaceEvent e : events) {
        boolean allDay = e.getStartTime().equals(LocalTime.of(8, 0))
            && e.getEndTime().equals(LocalTime.of(17, 0));

        String startTimeStr = allDay ? "" : e.getStartTime().format(timeFormatter);
        String endTimeStr = allDay ? "" : e.getEndTime().format(timeFormatter);

        String description = refurbish(e.getDescription());
        String location = refurbish(e.getLocation());
        String subject = refurbish(e.getSubject());
        String status = refurbish(e.getStatus());

        String row = '"' + subject + '"' + "," + e.getStartDate().format(dateFormatter) + ","
            + startTimeStr + "," + e.getEndDate().format(dateFormatter) + "," + endTimeStr + ","
            + (allDay ? "True" : "False") + "," + '"' + description + '"' + "," + '"' + location
            + '"' + "," + '"' + status + '"';

        bw.write(row);
        bw.newLine();
      }
    }
  }

  /**
   * A private helper for string sanitation.
   *
   * @param text is the input text.
   * @return is the sanitized string.
   */
  private String refurbish(String text) {
    if (text.equals("null")) {
      return "";
    } else {
      return text.trim().replace("\"", "\"\"");
    }
  }

}
