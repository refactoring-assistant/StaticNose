package calendar;

import calendar.model.CalendarModelReadOnly;
import java.io.IOException;

/**
 * Represents an Exporter interface for various types of exporters.
 */
public interface Exporter {

  /**
   * Exports the given calendar into the specific export format, supplied info from the calendar
   * model. Returns the absolute path to the file.
   *
   * @param model the calendar model to export.
   * @throws IOException if exporting fails.
   */
  String export(CalendarModelReadOnly model) throws IOException;
}
