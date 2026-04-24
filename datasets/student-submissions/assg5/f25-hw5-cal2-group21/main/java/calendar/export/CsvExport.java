package calendar.export;

import calendar.Status;
import calendar.model.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TimeZone;

/**
 * Handles exporting calendar events to CSV format.
 */
public class CsvExport implements CalendarExporter {

  @Override
  public void export(Set<Event> events, String fileName, TimeZone timeZone) throws IOException {
    try (FileWriter writer = new FileWriter(fileName)) {
      writer.write(
          "Subject,Start Date,Start Time,End Date,End Time,"
              + "All Day Event,Description,Location,Private\n");

      SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("MM/dd/yyyy");
      SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("hh:mm a");

      if (events != null && !events.isEmpty()) {
        for (Event event : events) {
          writeEvent(writer, event, dateOnlyFormat, timeOnlyFormat);
        }
      }
    }
  }

  @Override
  public String getFileExtension() {
    return "csv";
  }

  /**
   * Writes a single event to CSV.
   */
  private void writeEvent(FileWriter writer, Event event,
      SimpleDateFormat dateFormat, SimpleDateFormat timeFormat)
      throws IOException {
    String subject = escapeCsv(event.getSubject());
    String startDate = dateFormat.format(event.getStart());
    String startTime = timeFormat.format(event.getStart());
    String endDate = dateFormat.format(event.getEnd());
    String endTime = timeFormat.format(event.getEnd());

    boolean isAllDay = (event.getStart().getHours() == 8
        && event.getStart().getMinutes() == 0
        && event.getEnd().getHours() == 17
        && event.getEnd().getMinutes() == 0);

    String allDayEvent = isAllDay ? "True" : "False";
    String description = event.getDescription() != null
        ? escapeCsv(event.getDescription()) : "";
    String location = event.getLocation() != null
        ? event.getLocation().toString() : "";
    String privateStatus = (event.getStatus() != null
        && event.getStatus() == Status.PRIVATE) ? "True" : "False";

    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
        subject, startDate, startTime, endDate, endTime,
        allDayEvent, description, location, privateStatus));
  }

  /**
   * Escapes special characters for CSV format.
   */
  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }
}