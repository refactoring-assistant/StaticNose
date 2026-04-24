package calendar.service;

import calendar.model.EventStatus;
import calendar.model.Events;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Class to export the calendar to a CSV file.
 * Formats the data according to Google Calendar's CSV import specifications.
 */
public class ExportCsv {

  // Formatters required by Google Calendar
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");

  // The official header row for a Google Calendar CSV
  private static final String CSV_HEADER = "\"Subject\",\"Start Date\",\"Start Time\","
      + "\"End Date\",\"End Time\",\"All Day Event\",\"Description\",\"Location\",\"Private\"";

  /**
   * Exports the given collection of events to a CSV file.
   *
   * @param events   The collection of events to export.
   * @param fileName The name of the file to create (e.g., "myCal.csv").
   * @return The absolute path of the generated file as a String.
   * @throws IOException If an I/O error occurs writing to the file.
   */
  public String export(Collection<Events> events, String fileName) throws IOException {
    Path path = Paths.get(fileName).toAbsolutePath();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
      writer.write(CSV_HEADER);
      writer.newLine();
      for (Events event : events) {
        writer.write(formatEventAsCsvRow(event));
        writer.newLine();
      }
    }
    return path.toString();
  }

  /**
   * Formats a single Event object into a Google Calendar CSV row.
   *
   * @param event The event to format.
   * @return A CSV-formatted string.
   */
  private String formatEventAsCsvRow(Events event) {
    // 1. Declare and process the variables that are used early.
    String subject = event.getSubject();
    String description = event.getDescription() != null ? event.getDescription() : "";
    String location = event.getLocation() != null ? event.getLocation() : "";

    // Make sure any quotes inside the text are properly escaped
    subject = subject.replace("\"", "\"\"");
    description = description.replace("\"", "\"\"");
    location = location.replace("\"", "\"\"");

    // 2. Declare all other variables immediately before their first use.
    String startDate = event.getStartTime().toLocalDate().format(DATE_FORMAT);
    String startTime = event.getStartTime().toLocalTime().format(TIME_FORMAT);
    String endDate = event.getEndTime().toLocalDate().format(DATE_FORMAT);
    String endTime = event.getEndTime().toLocalTime().format(TIME_FORMAT);
    String allDay = event.isAllDay() ? "True" : "False";
    String privateStatus = event.getStatus() == EventStatus.PRIVATE ? "True" : "False";

    // Build the final CSV row string
    return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
        subject,
        startDate,
        startTime,
        endDate,
        endTime,
        allDay,
        description,
        location,
        privateStatus
    );
  }
}