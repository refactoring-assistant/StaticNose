package calendar.service;

import calendar.exception.CalendarException;
import calendar.model.EventStatus;
import calendar.model.InCalendar;
import calendar.model.InEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Strategy for exporting calendar to iCal (RFC 5545) format.
 * Generates .ics files compatible with Google Calendar, Apple Calendar, Outlook, etc.
 */
public class CalExportFunction implements InExportFunction {

  private static final DateTimeFormatter ICAL_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  private static final DateTimeFormatter ICAL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  private final ZoneId calendarTimezone;

  /**
   * Constructs an ICalExportStrategy with a timezone.
   *
   * @param timezone the timezone for datetime conversions
   */
  public CalExportFunction(ZoneId timezone) {
    Objects.requireNonNull(timezone, "Timezone cannot be null");
    this.calendarTimezone = timezone;
  }

  @Override
  public Path export(InCalendar calendar, Path outputPath) throws CalendarException {
    Objects.requireNonNull(calendar, "Calendar cannot be null");
    Objects.requireNonNull(outputPath, "Output path cannot be null");

    try {
      if (outputPath.getParent() != null) {
        Files.createDirectories(outputPath.getParent());
      }

      try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
        writeiCalHeader(writer, calendar);

        List<InEvent> events = calendar.getAllEvents();
        for (InEvent event : events) {
          writeEventToiCal(writer, event);
        }

        writeiCalFooter(writer);
      }

      return outputPath.toAbsolutePath();
    } catch (IOException e) {
      throw new CalendarException("Failed to export calendar to iCal format", e);
    }
  }

  @Override
  public String getFileExtension() {
    return "ics";
  }

  /**
   * Writes the iCal file header.
   */
  private void writeiCalHeader(BufferedWriter writer, InCalendar calendar)
      throws IOException {
    writer.write("BEGIN:VCALENDAR");
    writer.newLine();
    writer.write("VERSION:2.0");
    writer.newLine();
    writer.write("PRODID:-//Calendar Application//NONSGML v1.0//EN");
    writer.newLine();
    writer.write("CALSCALE:GREGORIAN");
    writer.newLine();
    writer.write("METHOD:PUBLISH");
    writer.newLine();
    writer.write("X-WR-CALNAME:" + escapeiCalText(calendar.getCalendarName()));
    writer.newLine();
    writer.write("X-WR-TIMEZONE:" + calendarTimezone.getId());
    writer.newLine();
  }

  /**
   * Writes a single event in iCal VEVENT format.
   */
  private void writeEventToiCal(BufferedWriter writer, InEvent event)
      throws IOException {
    writer.write("BEGIN:VEVENT");
    writer.newLine();

    String uid = generateUid(event);
    writer.write("UID:" + uid);
    writer.newLine();

    ZonedDateTime startZoned = event.getStartDateTime()
        .atZone(calendarTimezone);
    ZonedDateTime endZoned = event.getEndDateTime()
        .atZone(calendarTimezone);

    if (event.isAllDayEvent()) {
      writer.write("DTSTART;VALUE=DATE:"
          + event.getStartDateTime().format(ICAL_DATE_FORMAT));
      writer.newLine();
      writer.write("DTEND;VALUE=DATE:"
          + event.getEndDateTime().plusDays(1).format(ICAL_DATE_FORMAT));
      writer.newLine();
    } else {
      writer.write("DTSTART:"
          + startZoned.withZoneSameInstant(ZoneId.of("UTC"))
              .format(ICAL_DATETIME_FORMAT));
      writer.newLine();
      writer.write("DTEND:"
          + endZoned.withZoneSameInstant(ZoneId.of("UTC"))
              .format(ICAL_DATETIME_FORMAT));
      writer.newLine();
    }

    writer.write("DTSTAMP:"
        + ZonedDateTime.now(ZoneId.of("UTC")).format(ICAL_DATETIME_FORMAT));
    writer.newLine();

    writer.write("SUMMARY:" + escapeiCalText(event.getSubject()));
    writer.newLine();

    event.getDescription().ifPresent(desc -> {
      try {
        writer.write("DESCRIPTION:" + escapeiCalText(desc));
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    event.getLocation().ifPresent(loc -> {
      try {
        writer.write("LOCATION:" + escapeiCalText(loc));
        writer.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    String classification = event.getStatus() == EventStatus.PRIVATE
        ? "PRIVATE" : "PUBLIC";
    writer.write("CLASS:" + classification);
    writer.newLine();

    writer.write("STATUS:CONFIRMED");
    writer.newLine();

    writer.write("TRANSP:OPAQUE");
    writer.newLine();

    writer.write("END:VEVENT");
    writer.newLine();
  }

  /**
   * Writes the iCal file footer.
   */
  private void writeiCalFooter(BufferedWriter writer) throws IOException {
    writer.write("END:VCALENDAR");
    writer.newLine();
  }

  /**
   * Generates a unique identifier for an event.
   * Uses a combination of event properties and a UUID to ensure uniqueness.
   */
  private String generateUid(InEvent event) {
    String base = event.getSubject().replaceAll("[^a-zA-Z0-9]", "")
        + "-" + event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + "-" + UUID.randomUUID().toString().substring(0, 8);
    return base + "@calendarapp.local";
  }

  /**
   * Escapes special characters in iCal text fields.
   * Per RFC 5545: escape comma, semicolon, backslash, and newline.
   */
  private String escapeiCalText(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }

    return text
        .replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n");
  }
}