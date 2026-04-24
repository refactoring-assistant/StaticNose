package calendar.export;

import calendar.model.Event;
import java.io.IOException;
import java.util.Set;
import java.util.TimeZone;

/**
 * Interface for exporting calendar events to different file formats.
 */
public interface CalendarExporter {

  /**
   * Exports a set of events to a file.
   *
   * @param events   the events to export
   * @param fileName the file path to write to
   * @param timeZone the timezone of the calendar
   * @throws IOException if writing fails
   */
  void export(Set<Event> events, String fileName, TimeZone timeZone) throws IOException;

  /**
   * Gets the file extension this exporter handles.
   *
   * @return the file extension
   */
  String getFileExtension();
}