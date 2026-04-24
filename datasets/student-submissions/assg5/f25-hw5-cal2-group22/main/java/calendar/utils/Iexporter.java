package calendar.utils;

import calendar.model.AbstractEvent;
import java.util.List;

/**
 * Interface for exporting calendar events to various file formats.
 */

public interface Iexporter {

  /**
   * Exports the given list of events to a file.
   *
   * @param events   list of events to export
   * @param filename path to the output file
   * @return message indicating success or failure with file path
   */

  String export(List<AbstractEvent> events, String filename);

}
