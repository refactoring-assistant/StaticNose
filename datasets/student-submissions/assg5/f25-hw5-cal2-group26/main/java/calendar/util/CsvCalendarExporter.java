package calendar.util;

import calendar.model.CalendarInterface;
import calendar.model.EventInterface;
import calendar.model.EventStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

/**
 * Exports calendar events to a Google Calendar–compatible CSV file.
 * Columns (Google CSV): Subject, Start Date, Start Time, End Date, End Time,
 * All Day Event, Description, Location, Private.
 * This class implements CalendarExporterInterface.export(CalendarInterface, String).
 * and offers an overload export for convenience.
 */
public class CsvCalendarExporter implements CalendarExporterInterface {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private static final String[] HEADER = {
      "Subject", "Start Date", "Start Time", "End Date", "End Time",
      "All Day Event", "Description", "Location", "Private"
  };

  @Override
  public void export(CalendarInterface calendar, String filePath) throws IOException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    export(calendar.getAllCalendarEvents(), filePath);
  }

  /**
   * Export.
   *
   * @param events event.
   * @param filePath path.
   * @throws IOException exception thrown.
   */
  public void export(List<EventInterface> events, String filePath) throws IOException {
    if (events == null) {
      throw new IllegalArgumentException("Events cannot be null");
    }

    try (Writer writer = new FileWriter(filePath, false)) {

      writer.write(String.join(",", HEADER));
      writer.write("\n");


      for (EventInterface event : events) {
        writeRow(writer, event);
      }
    }
  }

  private void writeRow(Writer writer, EventInterface event) throws IOException {
    StringJoiner row = new StringJoiner(",");

    row.add(csvCell(event.getSubject()));
    row.add(csvCellDate(event.getStart()));
    row.add(csvCellTime(event.getStart(), event.isAllDay()));
    row.add(csvCellDate(event.getEnd() != null ? event.getEnd() : event.getStart()));
    row.add(csvCellTime(event.getEnd(), event.isAllDay()));
    row.add(event.isAllDay() ? "True" : "False");
    row.add(csvCell(event.getDescription()));
    row.add(csvCell(event.getLocation()));
    row.add(event.getStatus() == EventStatus.PRIVATE ? "True" : "False");

    writer.write(row.toString());
    writer.write("\n");
  }

  private String csvCellDate(java.time.ZonedDateTime dt) {
    if (dt == null) {
      return "";
    }
    return dt.format(DATE_FORMAT);
  }

  private String csvCellTime(java.time.ZonedDateTime dt, boolean allDay) {
    if (dt == null || allDay) {
      return "";
    }
    return dt.format(TIME_FORMAT);
  }


  private String csvCell(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    String v = value.strip();
    boolean needsQuote =
        v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");

    v = v.replace("\"", "\"\"");

    return needsQuote ? "\"" + v + "\"" : v;
  }
}
