package calendar.export;

import calendar.model.InterfaceEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implements the {@link Exporter} interface to write a list of events to
 * the standard iCalendar (.ics) format. This format is compatible with
 * Google Calendar, Apple Calendar, and other standard calendar applications.
 */
public class IcalExporter implements Exporter {

  /**
   * The specific date-time format required by the iCalendar standard (RFC 5545).
   * Formats a UTC DateTime as {@code YYYYMMDD'T'HHMMSS'Z'}.
   */
  private static final DateTimeFormatter ICAL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  /**
   * Exports a list of calendar events to a specified file in the iCalendar (.ics) format.
   *
   * @param events   The list of {@link InterfaceEvent} objects to export.
   * @param filename The name (and path) of the .ics file to create.
   * @throws IOException If an error occurs while writing to the file.
   */
  @Override
  public void export(List<InterfaceEvent> events, String filename) throws IOException {

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {

      bw.write("BEGIN:VCALENDAR");
      bw.newLine();
      bw.write("VERSION:2.0");
      bw.newLine();
      bw.write("PRODID:-//MyCalendarApp//EN");
      bw.newLine();

      String timestamp = LocalDateTime.now(ZoneOffset.UTC).format(ICAL_DATE_FORMAT);

      for (InterfaceEvent e : events) {
        LocalDateTime startDateTime = LocalDateTime.of(e.getStartDate(), e.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(e.getEndDate(), e.getEndTime());

        bw.write("BEGIN:VEVENT");
        bw.newLine();

        bw.write("UID:" + UUID.randomUUID());
        bw.newLine();
        bw.write("DTSTAMP:" + timestamp);
        bw.newLine();
        bw.write("DTSTART:" + startDateTime.atZone(ZoneOffset.UTC).format(ICAL_DATE_FORMAT));
        bw.newLine();
        bw.write("DTEND:" + endDateTime.atZone(ZoneOffset.UTC).format(ICAL_DATE_FORMAT));
        bw.newLine();
        bw.write("SUMMARY:" + e.getSubject());
        bw.newLine();

        bw.write("DESCRIPTION:" + e.getDescription());
        bw.newLine();

        bw.write("LOCATION:" + e.getLocation());
        bw.newLine();

        bw.write("STATUS:" + e.getStatus());
        bw.newLine();


        bw.write("END:VEVENT");
        bw.newLine();
      }

      bw.write("END:VCALENDAR");
      bw.newLine();
    }
  }
}