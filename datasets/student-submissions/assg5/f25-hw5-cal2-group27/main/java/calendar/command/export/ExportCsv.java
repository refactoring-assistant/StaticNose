package calendar.command.export;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports events to a Google-Calendar-compatible CSV file.
 */
public class ExportCsv extends AbstractExportCommand {

  /** Date pattern: yyyy/MM/dd to match Google Calendar CSV expectations. */
  private static final DateTimeFormatter DATE_FMT =
          DateTimeFormatter.ofPattern("yyyy/MM/dd");

  /** Time pattern: 12-hour with seconds and AM/PM. */
  private static final DateTimeFormatter TIME_FMT =
          DateTimeFormatter.ofPattern("hh:mm:ss a");

  /**
   * Creates a CSV exporter for the given filename.
   *
   * @param fileName output file name (must end with .csv)
   */
  public ExportCsv(String fileName) {
    super(fileName);
  }

  @Override
  public boolean supports(String fileName) {
    return fileName != null && fileName.toLowerCase().endsWith(".csv");
  }

  @Override
  public void write(List<Event> events, Path target) throws IOException {
    try (FileWriter writer = new FileWriter(target.toFile())) {
      writer.append(String.join(
                      ",",
                      List.of("Subject", "Start Date", "Start Time", "End Date",
                              "End Time", "All Day Event", "Description", "Location",
                              "Private")))
              .append('\n');

      for (Event event : events) {
        writer.append(buildCsvLine(event)).append('\n');
      }
    }
  }

  /** Builds a single CSV line for the given event. */
  private String buildCsvLine(Event event) {
    List<String> fields = new ArrayList<>();
    fields.add(quote(event.getSubject()));
    fields.add(event.getStart().format(DATE_FMT));

    boolean isAllDay =
            event.getStart().toLocalTime().equals(LocalTime.of(8, 0))
                    && event.getEnd().toLocalTime().equals(LocalTime.of(17, 0))
                    && event.getStart().toLocalDate().equals(event.getEnd().toLocalDate());

    if (isAllDay) {
      fields.add("");
      fields.add("");
      fields.add("");
      fields.add("True");
    } else {
      fields.add(event.getStart().format(TIME_FMT));
      fields.add(event.getEnd().format(DATE_FMT));
      fields.add(event.getEnd().format(TIME_FMT));
      fields.add("False");
    }

    String description = event.getDescription() != null ? event.getDescription() : "";
    String location = event.getLocation() != null ? event.getLocation() : "";

    fields.add(quote(description));
    fields.add(quote(location));
    fields.add(event.getStatus() == EventStatus.PRIVATE ? "True" : "False");

    return String.join(",", fields);
  }

  /** Quotes a CSV field and escapes embedded quotes. */
  private String quote(String value) {
    return "\"" + value.replace("\"", "\"\"")
            + "\"";
  }
}