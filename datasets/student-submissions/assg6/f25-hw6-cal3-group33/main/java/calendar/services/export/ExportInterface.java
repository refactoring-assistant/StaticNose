package calendar.services.export;

import calendar.model.event.EventInterface;
import java.io.IOException;
import java.util.List;

/**
 * Interface for exporting calendar events to various file formats.
 * Implementations provide format-specific export functionality such as Csv and iCal.
 * All exporters write events to files and return the absolute path of the created file.
 */
public interface ExportInterface {

  /**
   * Exports the calendar to the required file compatible with Google Calendar.
   * File format follows Google Calendar import specifications.
   *
   * @param events list of all events
   * @param filePath the path where Csv file should be created (relative or absolute)
   * @return the absolute path of the created iCal file
   * @throws IOException if file cannot be written
   */
  String export(List<EventInterface> events, String filePath) throws IOException;
}
