package calendar.view;

import calendar.model.Icalendar;
import calendar.model.Ievent;
import calendar.model.RecurringEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendars to iCal format.
 */
public class IcalExporter {
  private static final DateTimeFormatter ICAL_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  /**
   * Exports a calendar to iCal file.
   */
  public void export(Icalendar calendar, Path filePath) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
      writer.write("BEGIN:VCALENDAR");
      writer.newLine();
      writer.write("VERSION:2.0");
      writer.newLine();
      writer.write("PRODID:-//Calendar Application//EN");
      writer.newLine();
      writer.write("CALSCALE:GREGORIAN");
      writer.newLine();

      List<Ievent> events = calendar.getEvents();
      for (Ievent event : events) {
        writeEvent(writer, event, calendar);
      }

      writer.write("END:VCALENDAR");
      writer.newLine();
    }
  }

  private void writeEvent(BufferedWriter writer, Ievent event, Icalendar calendar)
      throws IOException {
    writer.write("BEGIN:VEVENT");
    writer.newLine();

    String uid = UUID.randomUUID().toString() + "@calendar-app.com";
    writer.write("UID:" + uid);
    writer.newLine();

    ZonedDateTime now = ZonedDateTime.now();
    writer.write("DTSTAMP:" + formatDateTime(now));
    writer.newLine();

    writer.write("DTSTART;TZID=" + calendar.getTimezone().getId() + ":"
        + formatDateTime(event.getStartDateTime()));
    writer.newLine();

    writer.write("DTEND;TZID=" + calendar.getTimezone().getId() + ":"
        + formatDateTime(event.getEndDateTime()));
    writer.newLine();

    writer.write("SUMMARY:" + escapeIcalText(event.getName()));
    writer.newLine();

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeIcalText(event.getDescription()));
      writer.newLine();
    }

    if (event instanceof RecurringEvent) {
      RecurringEvent recurring = (RecurringEvent) event;
      writer.write("RRULE:FREQ=" + recurring.getPattern().name());
      if (recurring.getRecurrenceEndDateTime() != null) {
        writer.write(";UNTIL=" + formatDateTime(recurring.getRecurrenceEndDateTime()));
      }
      writer.newLine();
    }

    writer.write("END:VEVENT");
    writer.newLine();
  }

  private String formatDateTime(ZonedDateTime dateTime) {
    return dateTime.format(ICAL_FORMATTER);
  }

  private String escapeIcalText(String text) {
    return text.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n");
  }
}
