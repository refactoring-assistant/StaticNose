package calendar.command.export;

import calendar.model.Event;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy interface for exporting events to a specific file format.
 */
public interface Export {

  /**
   * Determines whether this exporter supports the supplied filename.
   *
   * @param fileName the output file name (typically inspected by extension)
   * @return {@code true} if the exporter can handle the filename; otherwise {@code false}
   */
  boolean supports(String fileName);

  /**
   * Writes the provided events to the given target path in the exporter format.
   *
   * @param events the list of events to serialize
   * @param target the filesystem path to write to
   * @throws IOException if writing fails
   */
  void write(List<Event> events, Path target) throws IOException;
}