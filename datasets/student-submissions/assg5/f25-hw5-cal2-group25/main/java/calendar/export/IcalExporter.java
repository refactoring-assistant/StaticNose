package calendar.export;

import calendar.model.Exporter;
import calendar.model.impl.Event;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports calendar events into the iCalendar (.ics) format.
 * Converts event times to UTC and generates standards-compliant VEVENT entries.
 */

public class IcalExporter implements Exporter {
  private static final DateTimeFormatter utc_fmt =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  /**
   * Fallback when the model calls export(events, file) without a timezone.
   * Default to UTC if no timezone is given.
   */
  @Override
  public void export(List<Event> events, Path file) throws IOException {
    export(events, "UTC", file);
  }

  @Override
  public void export(List<Event> events, String calendarTimezone, Path file) throws IOException {
    final ZoneId zone = ZoneId.of(calendarTimezone);
    final DateTimeFormatter utc_fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    try (BufferedWriter w = Files.newBufferedWriter(file)) {
      w.write("BEGIN:VCALENDAR\n");
      w.write("VERSION:2.0\n");
      w.write("PRODID:-//RushB//Calendar//EN\n");

      for (Event e : events) {
        w.write("BEGIN:VEVENT\n");
        w.write("UID:" + e.id() + "@rushb\n");
        w.write("SUMMARY:" + escape(e.subject()) + "\n");

        if (e.allDay()) {
          LocalDate startDate = e.start().toLocalDate();
          LocalDate endDateExclusive = e.end().toLocalDate().plusDays(1);

          w.write(
              "DTSTART;VALUE=DATE:" + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "\n");
          w.write("DTEND;VALUE=DATE:" + endDateExclusive.format(DateTimeFormatter.BASIC_ISO_DATE)
              + "\n");
        } else {
          ZonedDateTime zstart = e.start().atZone(zone).withZoneSameInstant(ZoneOffset.UTC);
          ZonedDateTime zend = e.end().atZone(zone).withZoneSameInstant(ZoneOffset.UTC);

          w.write("DTSTART:" + utc_fmt.format(zstart) + "\n");
          w.write("DTEND:" + utc_fmt.format(zend) + "\n");
        }

        if (e.description() != null && !e.description().isBlank()) {
          w.write("DESCRIPTION:" + escape(e.description()) + "\n");
        }
        if (e.location() != null && !e.location().isBlank()) {
          w.write("LOCATION:" + escape(e.location()) + "\n");
        }

        w.write("CLASS:" + e.status().name() + "\n");

        w.write("END:VEVENT\n");
      }

      w.write("END:VCALENDAR\n");
    }
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n");
  }
}
