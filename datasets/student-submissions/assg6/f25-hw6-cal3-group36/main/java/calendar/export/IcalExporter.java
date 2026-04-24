package calendar.export;

import calendar.model.CalendarModel;
import calendar.model.InterfaceEvent;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Exporting calendar into .ical format.
 */
public class IcalExporter implements EventExporter {

  @Override
  public void export(CalendarModel calendar, Writer writer) throws IOException {
    writer.write("BEGIN:VCALENDAR\n");
    writer.write("VERSION:2.0\n");
    writer.write("PRODID:-//CalendarApp//EN\n");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        .withZone(ZoneOffset.UTC);

    for (InterfaceEvent e : calendar.getEvents()) {
      writer.write("BEGIN:VEVENT\n");
      writer.write("SUMMARY:" + sanitize(e.getSubject()) + "\n");
      writer.write("DTSTART:" + fmt.format(e.getStart()) + "\n");
      writer.write("DTEND:" + fmt.format(e.getEnd()) + "\n");
      writer.write("DESCRIPTION:" + sanitize(e.getDescription()) + "\n");
      writer.write("LOCATION:" + sanitize(e.getLocation()) + "\n");
      writer.write("END:VEVENT\n");
    }

    writer.write("END:VCALENDAR\n");
  }

  private String sanitize(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("\n", "\\n")
        .replace(",", "\\,")
        .replace(";", "\\;");
  }
}