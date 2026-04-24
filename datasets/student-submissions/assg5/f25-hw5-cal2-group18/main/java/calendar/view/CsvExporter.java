package calendar.view;


import calendar.model.Event;
import calendar.model.EventStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * A utility class for exporting calendar events to a Google Calendar–friendly CSV file.
 */
public class CsvExporter {


  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");


  /**
   * Exports the given list of events to a CSV file compatible with Google Calendar.
   *
   * @param events   the list of events to export
   * @param filePath the path of the CSV file to create
   * @throws IOException if an error occurs while writing the file
   */
  public void exportEvents(List<Event> events, String filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
              + "Description,Location,Private\n");


      for (Event event : events) {
        LocalDateTime start = event.getStartDateTime();
        LocalDateTime end = event.getEndDateTime();


        writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s%n", sanitize(event.getSubject()),
            start.format(DATE_FORMAT), start.format(TIME_FORMAT), end.format(DATE_FORMAT),
            end.format(TIME_FORMAT), event.isAllDay() ? "TRUE" : "FALSE",
            sanitize(event.getDescription()), sanitize(event.getLocation()),
            event.getStatus() == EventStatus.PRIVATE ? "TRUE" : "FALSE"));
      }
    }
  }


  /**
   * Removes commas and newlines from text to keep CSV format clean.
   */
  private String sanitize(String text) {
    if (text == null) {
      return "";
    }
    return text.replace(",", " ").replace("\n", " ").trim();
  }
}
