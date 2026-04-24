package calendar.export;

import calendar.model.CalendarModel;
import java.io.IOException;
import java.io.Writer;

/**
 * Exporter interface for writing calendar data in different ouput formats.
 */
public interface EventExporter {

  /**
   * Calendar model exported to a writer.
   *
   * @param calendar model to export.
   * @param writer   the output writer
   * @throws IOException Invalid writing.
   */
  void export(CalendarModel calendar, Writer writer) throws IOException;
}