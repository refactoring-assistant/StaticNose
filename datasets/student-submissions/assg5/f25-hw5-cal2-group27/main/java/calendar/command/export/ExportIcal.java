package calendar.command.export;

import calendar.model.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports events to an iCalendar file (.ical or .ics).
 */
public class ExportIcal extends AbstractExportCommand {

  /** UTC timestamp format without separators. */
  private static final DateTimeFormatter ICAL_DT_FMT =
          DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

  /**
   * Creates an iCal exporter for the given filename.
   *
   * @param fileName output file name (supports .ical and .ics)
   */
  public ExportIcal(String fileName) {
    super(fileName);
  }

  @Override
  public boolean supports(String fileName) {
    if (fileName == null) {
      return false;
    }
    String lower = fileName.toLowerCase();
    return lower.endsWith(".ical") || lower.endsWith(".ics");
  }

  @Override
  public void write(List<Event> events, Path target) throws IOException {
    try (FileWriter writer = new FileWriter(target.toFile())) {
      writer.append("BEGIN:VCALENDAR\n");
      writer.append("VERSION:2.0\n");
      writer.append("PRODID:-//cs5010/calendar//EN\n");
      writer.append("CALSCALE:GREGORIAN\n");
      writer.append("METHOD:PUBLISH\n");

      for (Event event : events) {
        appendEvent(writer, event);
      }

      writer.append("END:VCALENDAR\n");
    }
  }

  /** Appends a single VEVENT block to the file. */
  private void appendEvent(FileWriter writer, Event event) throws IOException {
    writer.append("BEGIN:VEVENT\n");
    writer.append("UID:").append(event.getId().toString()).append('\n');
    writer.append("DTSTAMP:").append(formatUtc(event.getStart())).append('\n');
    writer.append("DTSTART:").append(formatUtc(event.getStart())).append('\n');
    writer.append("DTEND:").append(formatUtc(event.getEnd())).append('\n');
    writer.append("SUMMARY:").append(event.getSubject()).append('\n');

    if (event.getLocation() != null && !event.getLocation().isBlank()) {
      writer.append("LOCATION:").append(event.getLocation()).append('\n');
    }
    if (event.getDescription() != null && !event.getDescription().isBlank()) {
      writer.append("DESCRIPTION:").append(event.getDescription()).append('\n');
    }
    writer.append("END:VEVENT\n");
  }

  /** Formats a ZonedDateTime as a UTC iCal timestamp. */
  private String formatUtc(java.time.ZonedDateTime zdt) {
    return zdt.withZoneSameInstant(ZoneOffset.UTC).format(ICAL_DT_FMT);
  }
}
