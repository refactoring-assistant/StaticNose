package calendar.services.export;

import calendar.model.event.EventInterface;
import calendar.model.event.EventStatus;
import calendar.model.util.EventUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendar events to iCal format (.ics) compatible with Google Calendar.
 */
public class IcalExporter implements ExportInterface {

  private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter ICAL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  /**
   * Exports the calendar to a iCal file compatible with Google Calendar.
   * File format follows Google Calendar import specifications.
   *
   * @param events list of all events
   * @param filePath the path where Csv file should be created (relative or absolute)
   * @return the absolute path of the created iCal file
   * @throws IOException if file cannot be written
   */
  @Override
  public String export(List<EventInterface> events, String filePath) throws IOException {
    StringBuilder ical = new StringBuilder();

    ical.append("BEGIN:VCALENDAR\n");
    ical.append("VERSION:2.0\n");
    ical.append("PRODID:-//Calendar Application//EN\n");

    for (EventInterface event : events) {
      ical.append(formatAsIcal(event));
    }

    ical.append("END:VCALENDAR\n");

    Path path = Paths.get(filePath);
    Files.writeString(path, ical.toString());

    return path.toAbsolutePath().toString();
  }

  private String formatAsIcal(EventInterface event) {
    StringBuilder vevent = new StringBuilder();

    vevent.append("BEGIN:VEVENT\n");
    vevent.append("UID:").append(UUID.randomUUID().toString()).append("\n");
    vevent.append("SUMMARY:").append(event.getSubject()).append("\n");

    if (EventUtils.isAllDayEvent(event)) {
      vevent.append("DTSTART;VALUE=DATE:")
          .append(event.getStartDateTime().format(ICAL_DATE_FORMAT)).append("\n");
      vevent.append("DTEND;VALUE=DATE:")
          .append(event.getEndDateTime().format(ICAL_DATE_FORMAT)).append("\n");
    } else {
      vevent.append("DTSTART:").append(formatDateTime(event.getStartDateTime())).append("\n");
      vevent.append("DTEND:").append(formatDateTime(event.getEndDateTime())).append("\n");
    }

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      vevent.append("DESCRIPTION:").append(event.getDescription()).append("\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      vevent.append("LOCATION:").append(event.getLocation()).append("\n");
    }

    if (event.getStatus() == EventStatus.PRIVATE) {
      vevent.append("CLASS:PRIVATE\n");
    } else {
      vevent.append("CLASS:PUBLIC\n");
    }

    vevent.append("END:VEVENT\n");

    return vevent.toString();
  }

  private String formatDateTime(ZonedDateTime dateTime) {
    return dateTime.format(ICAL_DATE_TIME_FORMAT);
  }
}