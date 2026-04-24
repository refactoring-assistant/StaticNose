package calendar.exporter;

import calendar.model.IntEvent;
import java.util.List;

/**
 * Interface for exporting calendar events to different file formats.
 * Implementations of this interface handle the specifics of each export format
 * (CSV, iCal, etc.).
 */
public interface CalendarExporter {
  /**
   * Exports the given list of events to a file.
   *
   * @param events       the list of events to export
   * @param fileName     the name of the file to export to
   * @param calendarName the name of the calendar being exported
   * @return the absolute path of the exported file
   * @throws IllegalStateException if the export fails
   */
  String export(List<IntEvent> events, String fileName, String calendarName);

  /**
   * Gets the file extension(s) supported by this exporter.
   *
   * @return array of supported file extensions (without the dot)
   */
  String[] getSupportedExtensions();
}

