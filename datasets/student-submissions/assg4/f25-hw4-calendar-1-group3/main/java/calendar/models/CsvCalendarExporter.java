package calendar.models;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Exports calendar events into a CSV file compatible with Google Calendar import.
 */
public class CsvCalendarExporter implements CalendarExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

  @Override
  public void export(ObservableCalendar calendar, String filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.append(
          "Subject,Start Date,Start Time,End Date,End Time,"
              + "All Day Event,Description,Location,Private"
              + System.lineSeparator()
      );

      Set<Event> events = calendar.filterEvents(e -> true);

      for (Event e : events) {
        String subject = sanitize(e.getSubject());
        String startDate = e.getStartDate().format(DATE_FORMAT);
        String endDate = e.getEndDate().format(DATE_FORMAT);
        String startTime = e.getStartTime().format(TIME_FORMAT);
        String endTime = e.getEndTime().format(TIME_FORMAT);
        String description = sanitize(e.getDescription());
        String location = e.getLocation() != null ? sanitize(e.getLocation().toString()) : "";
        String isPrivate = (e.getStatus() == Status.PRIVATE) ? "True" : "False";

        writer.append(String.join(",",
            subject,
            startDate,
            startTime,
            endDate,
            endTime,
            "False",
            description,
            location,
            isPrivate
        ));
        writer.append(System.lineSeparator());
      }
    }
  }

  /**
   * CSV fields that contain commas, quotes, or newlines must be wrapped in double quotes.
   *
   * @param s string to make CSV safe
   * @return CSV safe string
   */
  private String sanitize(String s) {
    if (s == null) {
      return "";
    }
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }
}
