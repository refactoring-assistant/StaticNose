package calendar.service.export;

import calendar.model.Calendar;
import calendar.model.Event;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Exports calendars to iCalendar (RFC 5545) format.
 */
public class IcalExporter {
  private static final DateTimeFormatter UTC_FMT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
  private static final String PRODID = "-//Calendar Application//NONSGML v2.0//EN";

  /**
   * Exports calendar to iCalendar file.
   *
   * @param cal the calendar to export
   * @param filename the output filename
   * @return absolute path to exported file
   * @throws IOException if file write fails
   * @throws IllegalArgumentException if cal or filename is invalid
   */
  public String export(Calendar cal, String filename) throws IOException {
    if (cal == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be empty");
    }
    try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
      w.println("BEGIN:VCALENDAR");
      w.println("VERSION:2.0");
      w.println("PRODID:" + PRODID);
      w.println("CALSCALE:GREGORIAN");
      w.println("METHOD:PUBLISH");
      w.println("X-WR-CALNAME:" + escape(cal.getName()));
      w.println("X-WR-TIMEZONE:" + cal.getTimezone().getId());
      for (Event e : cal.getAllEvents()) {
        writeEvent(w, e);
      }
      w.println("END:VCALENDAR");
    }
    return new File(filename).getAbsolutePath();
  }

  /**
   * Checks if filename has iCalendar extension.
   *
   * @param filename the filename to check
   * @return true if filename ends with .ical or .ics
   */
  public boolean supports(String filename) {
    if (filename == null) {
      return false;
    }
    String lower = filename.toLowerCase();
    return lower.endsWith(".ical") || lower.endsWith(".ics");
  }

  /**
   * Writes single event in VEVENT format.
   *
   * @param w the print writer
   * @param e the event to write
   */
  private void writeEvent(PrintWriter w, Event e) {
    w.println("BEGIN:VEVENT");
    w.println("UID:" + UUID.randomUUID().toString() + "@calendar-app.com");
    w.println("DTSTAMP:" + formatUtc(ZonedDateTime.now()));
    w.println("DTSTART:" + formatUtc(e.getStart()));
    w.println("DTEND:" + formatUtc(e.getEnd()));
    w.println("SUMMARY:" + escape(e.getSubject()));
    if (e.getDescription() != null && !e.getDescription().isEmpty()) {
      w.println("DESCRIPTION:" + escape(e.getDescription()));
    }
    if (e.getLocation() != null && !e.getLocation().isEmpty()) {
      w.println("LOCATION:" + escape(e.getLocation()));
    }
    w.println("CLASS:" + (e.isPublic() ? "PUBLIC" : "PRIVATE"));
    w.println("STATUS:CONFIRMED");
    w.println("TRANSP:OPAQUE");
    w.println("END:VEVENT");
  }

  /**
   * Formats datetime in UTC for iCalendar.
   *
   * @param zdt the datetime to format
   * @return UTC formatted string
   */
  private String formatUtc(ZonedDateTime zdt) {
    return zdt.withZoneSameInstant(java.time.ZoneOffset.UTC).format(UTC_FMT);
  }

  /**
   * Escapes special iCalendar characters.
   *
   * @param txt the text to escape
   * @return escaped text or empty string if null
   */
  public String escape(String txt) {
    if (txt == null) {
      return "";
    }
    return txt.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n")
        .replace("\r", "");
  }
}