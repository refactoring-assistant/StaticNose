package calendar.controller.exporter;

import calendar.model.event.CalendarEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * This class extends export functionality in the CSV format.
 */
public class CsvExporter extends AbstractExportCalendar {

  /**
   * Initializes the filename to export in the correct format.
   *
   * @param filename path of the file where the calendar is exported to including file name
   */
  public CsvExporter(String filename) {
    super(filename);
  }

  @Override
  public Boolean exportHelper(List<CalendarEvent> eventList) throws RuntimeException {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write("Subject,Start date,Start time,End date,");
      writer.write("End time,Description,Location,Status" + System.lineSeparator());

      for (CalendarEvent event : eventList) {

        String subject = escapeCsv(event.getSubject());

        ZonedDateTime start = event.getStartDateTime();
        String startDate = start.toLocalDate().toString();
        String startTime = start.toLocalTime().toString();

        String description = escapeCsv(nullToString(event.getDescription()));
        String location = escapeCsv(nullToString(event.getLocation()));
        String status = escapeCsv(nullToString(event.getStatus()));

        ZonedDateTime end = event.getEndDateTime();
        String endDate = end.toLocalDate().toString();
        String endTime = end.toLocalTime().toString();

        writer.write(String.join(",",
            subject, startDate, startTime, endDate, endTime, description, location, status));
        writer.write(System.lineSeparator());
      }
    } catch (IOException e) {
      throw new RuntimeException("IO exception encountered: " + e.getMessage());
    }
    return true;
  }

  /**
   * Adds escape character so that insert in csv would not break with formatting.
   *
   * @param value the string value to be added
   * @return the string value with added escape sequence
   */
  private static String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\n")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }
}
