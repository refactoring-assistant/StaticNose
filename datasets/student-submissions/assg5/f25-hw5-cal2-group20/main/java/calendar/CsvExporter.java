package calendar;

import calendar.model.CalendarModelReadOnly;
import calendar.model.EventObject;
import calendar.model.RenderCsv;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Represents a CSV Exporter class. The application should be able to export the calendar to a CSV
 * file.
 */
public class CsvExporter implements Exporter {
  private final String filename;

  /**
   * Constructs a CSV exporter provided the name of the file to write to.
   *
   * @param filename name of csv file.
   * @throws NullPointerException if a filename is null.
   * @throws IllegalArgumentException if the csv filename is invalid.
   */
  public CsvExporter(String filename) {
    if (filename == null) {
      throw new NullPointerException("Filename cannot be null");
    }
    if (filename.length() < 5 || !filename.endsWith(".csv")) {
      throw new IllegalArgumentException("Filename must end with .csv");
    }
    this.filename = filename;
  }

  @Override
  public String export(CalendarModelReadOnly model) throws IOException {
    File file = new File(this.filename);
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,"
            + "Private" + System.lineSeparator());
    for (EventObject event : model.getAllEvents()) {
      writer.write(new RenderCsv().render(event) + System.lineSeparator());
    }
    writer.close();
    return file.getAbsolutePath();
  }
}
