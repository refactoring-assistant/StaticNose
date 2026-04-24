package calendar.export;

import calendar.model.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * Handles exporting calendar events to iCal format.
 */
public class IcalendarExport implements CalendarExporter {

  private final SimpleDateFormat icalFormat;

  /**
   * Constructor for ICalendarExport.
   */
  public IcalendarExport() {
    icalFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    icalFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void export(Set<Event> events, String fileName, TimeZone tz) throws IOException {
    try (FileWriter fw = new FileWriter(fileName)) {
      fw.write("BEGIN:VCALENDAR\n");
      fw.write("VERSION:2.0\n");
      fw.write("PRODID:-//Calendar Application//EN\n");
      fw.write("CALSCALE:GREGORIAN\n");
      fw.write("METHOD:PUBLISH\n");
      fw.write("X-WR-TIMEZONE:" + tz.getID() + "\n");

      if (events != null && !events.isEmpty()) {
        for (Event event : events) {
          writeEvent(fw, event);
        }
      }

      fw.write("END:VCALENDAR\n");
    }
  }

  @Override
  public String getFileExtension() {
    return "ical";
  }

  private void writeEvent(FileWriter fw, Event event) throws IOException {
    fw.write("BEGIN:VEVENT\n");
    fw.write("UID:" + generateUid(event) + "\n");
    fw.write("DTSTAMP:" + icalFormat.format(new Date()) + "Z\n");
    fw.write("DTSTART:" + icalFormat.format(event.getStart()) + "Z\n");
    fw.write("DTEND:" + icalFormat.format(event.getEnd()) + "Z\n");
    fw.write("SUMMARY:" + escapeText(event.getSubject()) + "\n");

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      fw.write("DESCRIPTION:" + escapeText(event.getDescription()) + "\n");
    }

    if (event.getLocation() != null) {
      fw.write("LOCATION:" + event.getLocation() + "\n");
    }

    if (event.getStatus() != null) {
      fw.write("CLASS:" + event.getStatus() + "\n");
    }

    fw.write("END:VEVENT\n");
  }

  private String generateUid(Event event) {
    return event.getSubject().replaceAll("[^a-zA-Z0-9]", "")
        + "-" + event.getStart().getTime() + "@calendar.app";
  }

  private String escapeText(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n");
  }
}