package calendar;

import calendar.model.CalendarDatabaseModelReadOnly;
import calendar.model.CalendarModelReadOnly;
import calendar.model.EventObject;
import calendar.model.RenderIcal;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Represents an exporter that exports the calendar in ical format.
 */
public class IcalExporter implements Exporter {
  private final String filename;

  /**
   * Constructs an exporter of files with extension ical (ics).
   *
   * @param filename the filename.
   * @throws NullPointerException if a filename is null.
   * @throws IllegalArgumentException if the csv filename is invalid.
   */
  public IcalExporter(String filename) {
    if (filename == null) {
      throw new NullPointerException("Filename cannot be null");
    }
    if (filename.length() < 6 || !filename.endsWith(".ical")) {
      throw new IllegalArgumentException("Filename must end with .ical");
    }
    this.filename = filename;
  }


  @Override
  public String export(CalendarModelReadOnly model) throws IOException {
    File file = new File(this.filename.substring(0, this.filename.lastIndexOf('.'))
        + ".ics");
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write("BEGIN:VCALENDAR" + System.lineSeparator()
          + "PRODID:-//Google Inc//Google Calendar 70.9054//EN" + System.lineSeparator()
          + "VERSION:2.0" + System.lineSeparator() + "CALSCALE:GREGORIAN" + System.lineSeparator()
          + "METHOD:PUBLISH" + System.lineSeparator());

    for (EventObject event : model.getAllEvents()) {
      writer.write("BEGIN:VEVENT" + System.lineSeparator());
      writer.write(new RenderIcal().render(event) + System.lineSeparator());
      writer.write("END:VEVENT" + System.lineSeparator());
    }
    writer.write("END:VCALENDAR");
    writer.close();
    return file.getAbsolutePath();
  }
}
