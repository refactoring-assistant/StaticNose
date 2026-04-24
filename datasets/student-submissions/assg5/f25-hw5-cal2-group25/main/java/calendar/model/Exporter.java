package calendar.model;

import calendar.model.impl.Event;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Defines a way to export a list of events to a file.
 */
public interface Exporter {

  /**
   * Exports the given list of events to the specified file.
   *
   * @param events the list of events to export
   * @param file   the file to write the exported data to
   * @throws java.io.IOException if an error occurs during export
   */
  void export(List<Event> events, Path file) throws IOException;

  /**
   * Exports events using the provided timezone when supported by the exporter.
   * Default implementation ignores the timezone and delegates to {@link #export(List, Path)}.
   *
   * @param events   the events to export
   * @param timezone the calendar's timezone
   * @param file     the output file path
   * @throws IOException if writing the export fails
   */
  default void export(List<Event> events, String timezone, Path file) throws IOException {
    export(events, file);
  }
}