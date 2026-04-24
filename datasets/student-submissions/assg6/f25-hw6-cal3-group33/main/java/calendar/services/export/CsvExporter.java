package calendar.services.export;

import calendar.model.event.EventInterface;
import calendar.model.event.EventStatus;
import calendar.model.util.EventUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Exports calendar events to Csv format compatible with Google Calendar.
 */
public class CsvExporter implements ExportInterface {

  /**
   * Exports the calendar to a Csv file compatible with Google Calendar.
   * File format follows Google Calendar import specifications.
   *
   * @param events list of all events
   * @param filePath the path where Csv file should be created (relative or absolute)
   * @return the absolute path of the created Csv file
   * @throws IOException if file cannot be written
   */
  @Override
  public String export(List<EventInterface> events, String filePath) throws IOException {

    StringBuilder csv = new StringBuilder();
    csv.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,"
        + "Location,Private\n");

    for (EventInterface event : events) {
      csv.append(formatAsCsv(event)).append("\n");
    }

    Path path = Paths.get(filePath);
    Files.writeString(path, csv.toString());

    return path.toAbsolutePath().toString();
  }

  private String formatAsCsv(EventInterface event) {
    String subject = event.getSubject();
    String startDate = formatDate(event.getStartDateTime());
    String startTime = formatTime(event.getStartDateTime());
    String endDate = formatDate(event.getEndDateTime());
    String endTime = formatTime(event.getEndDateTime());
    String allDay = EventUtils.isAllDayEvent(event) ? "True" : "False";
    String description = event.getDescription() != null ? event.getDescription() : "";
    String location = event.getLocation() != null ? event.getLocation() : "";
    String isPrivate = event.getStatus() == EventStatus.PRIVATE ? "True" : "False";

    return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
        subject, startDate, startTime, endDate, endTime, allDay, description, location, isPrivate);
  }

  private String formatDate(ZonedDateTime dateTime) {
    int month = dateTime.getMonthValue();
    int day = dateTime.getDayOfMonth();
    int year = dateTime.getYear();

    return String.format("%02d/%02d/%d", month, day, year);
  }

  private String formatTime(ZonedDateTime dateTime) {
    int hour = dateTime.getHour();
    int minute = dateTime.getMinute();

    String amPm = hour >= 12 ? "PM" : "AM";
    int displayHour = hour % 12;
    if (displayHour == 0) {
      displayHour = 12;
    }

    return String.format("%02d:%02d %s", displayHour, minute, amPm);
  }

}
