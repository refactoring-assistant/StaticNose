package calendar.controller.exporter;

import calendar.model.CalendarManagerInterface;
import calendar.model.event.CalendarEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This class extends export functionality in the ICS format.
 */
public class IcsExporter extends AbstractExportCalendar {
  private final CalendarManagerInterface calendar;

  /**
   * Initializes the filename to export in the correct format.
   *
   * @param filename path of the file where the calendar is exported to including file name
   */
  public IcsExporter(String filename, CalendarManagerInterface calendar) {
    super(filename);
    this.calendar = calendar;
  }

  @Override
  public Boolean exportHelper(List<CalendarEvent> eventList) throws RuntimeException {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write("BEGIN:VCALENDAR" + System.lineSeparator()
          + "PRODID:-//MAZIV//Calendar App//EN" + System.lineSeparator()
          + "VERSION:2.0" + System.lineSeparator()
          + "CALSCALE:GREGORIAN" + System.lineSeparator());

      writer.write("X-WR-CALNAME:" + calendar.getCalendarName() + System.lineSeparator()
          + "X-WR-TIMEZONE:" + calendar.getTimeZone() + System.lineSeparator());

      for (CalendarEvent event : eventList) {
        writer.write("BEGIN:VEVENT" + System.lineSeparator()
            + "DTSTART:" + toIcsUtcFormat(event.getStartDateTime()) + System.lineSeparator()
            + "DTEND:" + toIcsUtcFormat(event.getEndDateTime()) + System.lineSeparator()
            + "UID:" + event.getSubject() + "-" + event.getStartDateTime().toLocalDate()
            + event.getEndDateTime().toLocalDate() + System.lineSeparator()
            + "SUMMARY:" + nullToString(event.getSubject()) + System.lineSeparator()
            + "END:VEVENT" + System.lineSeparator());
      }

      writer.write("END:VCALENDAR" + System.lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException("IO exception encountered: " + e.getMessage());
    }
    return true;
  }

  /**
   * Helper to convert ZonedDateTime to format recognized in the ICS format.
   *
   * @param zonedDateTime the zoned date time format
   * @return a ICS compliant date time string
   */
  public static String toIcsUtcFormat(ZonedDateTime zonedDateTime) {
    ZonedDateTime utcTime = zonedDateTime.withZoneSameInstant(java.time.ZoneOffset.UTC);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    return formatter.format(utcTime);
  }
}