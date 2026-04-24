package calendar.util;

import calendar.model.Event;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting events to iCal format.
 */
public class IcalExporter {

  private static final DateTimeFormatter ICAL_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  /**
   * Exports events to iCal format file.
   *
   * @param events list of events to export
   * @param timezone timezone of the calendar
   * @param target file path
   * @return absolute path of created file
   * @throws IOException if writing fails
   */
  public static Path exportToIcal(List<Event> events, ZoneId timezone,
                                  Path target) throws IOException {
    try (BufferedWriter w = Files.newBufferedWriter(target)) {
      w.write("BEGIN:VCALENDAR");
      w.newLine();
      w.write("VERSION:2.0");
      w.newLine();
      w.write("PRODID:-//Calendar App//EN");
      w.newLine();
      w.write("CALSCALE:GREGORIAN");
      w.newLine();

      for (Event e : events) {
        writeEvent(w, e, timezone);
      }

      w.write("END:VCALENDAR");
      w.newLine();
    }
    return target.toAbsolutePath();
  }

  private static void writeEvent(BufferedWriter w, Event e, ZoneId timezone)
      throws IOException {
    w.write("BEGIN:VEVENT");
    w.newLine();

    w.write("UID:" + java.util.UUID.randomUUID().toString());
    w.newLine();

    ZonedDateTime startZoned = e.getStart().atZone(timezone);
    w.write("DTSTART:" + startZoned.format(ICAL_FORMAT));
    w.newLine();

    ZonedDateTime endZoned = e.getEnd().atZone(timezone);
    w.write("DTEND:" + endZoned.format(ICAL_FORMAT));
    w.newLine();

    w.write("SUMMARY:" + escape(e.getSubject()));
    w.newLine();

    if (e.getDescription().isPresent()) {
      w.write("DESCRIPTION:" + escape(e.getDescription().get()));
      w.newLine();
    }

    if (e.getLocation().isPresent()) {
      w.write("LOCATION:" + escape(e.getLocation().get()));
      w.newLine();
    }

    if (e.getStatus().isPresent()
        && e.getStatus().get().equalsIgnoreCase("private")) {
      w.write("CLASS:PRIVATE");
      w.newLine();
    } else {
      w.write("CLASS:PUBLIC");
      w.newLine();
    }

    w.write("END:VEVENT");
    w.newLine();
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n");
  }
}