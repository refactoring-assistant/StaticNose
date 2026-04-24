package calendar.export;

import calendar.model.CalendarModel;
import calendar.model.InterfaceEvent;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Exporting calendar into csv format.
 */
public class CsvExporter implements EventExporter {

  @Override
  public void export(CalendarModel calendar, Writer writer) throws IOException {
    DateTimeFormatter fmt =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());

    writer.write("Subject,Start,End,Description,Location,Public,TimeZone"
        + System.lineSeparator());

    for (InterfaceEvent e : calendar.getEvents()) {
      String line = String.join(",",
          quote(e.getSubject()),
          quote(fmt.format(e.getStart().atZone(e.getZone()))),
          quote(fmt.format(e.getEnd().atZone(e.getZone()))),
          quote(e.getDescription()),
          quote(e.getLocation()),
          String.valueOf(e.isPublicEvent()),
          e.getZone().getId());
      writer.write(line);
      writer.write(System.lineSeparator());
    }
  }

  private String quote(String value) {
    if (value == null) {
      return "";
    }
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}