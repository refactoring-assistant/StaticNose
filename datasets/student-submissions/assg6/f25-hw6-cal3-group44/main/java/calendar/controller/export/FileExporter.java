package calendar.controller.export;

import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


/**
 * Represents a general interface for exporting calendar events to a file.
 * Implementations of this interface define how event data should be written
 * into specific file formats like csv and Ical.
 */
public interface FileExporter {

  /**
   * Exports the given events to a file.
   *
   * @param fileName the filename.
   * @param events   list of events covering the required dates.
   * @return the absolute path of the exported file.
   */
  String export(String fileName, Map<LocalDate, List<EventReadOnly>> events);
}
