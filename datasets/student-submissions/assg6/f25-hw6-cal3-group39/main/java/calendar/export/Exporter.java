package calendar.export;

import calendar.model.InterfaceEvent;
import java.io.IOException;
import java.util.List;

/**
 * Interface for exporting calendar events to various file formats.
 */
public interface Exporter {

  /**
   * This is the export method that will be overriden by the worker classes of Exporter with their
   * own logic.
   *
   * @param events   is the list of events to be exported.
   * @param filename is the filename.
   * @throws IOException is thrown if any exceptions.
   */
  void export(List<InterfaceEvent> events, String filename) throws IOException;
}