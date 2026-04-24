package calendar.util;

import calendar.model.CalendarInterface;
import java.io.IOException;

/**
 * Generic calendar exporter for any format (CSV, JSON, ICS, etc.).
 */
public interface CalendarExporterInterface {

  /**
   * Saves the calendar to a file at the specified location.
   *
   * @param calendar The calendar to be exported.
   * @param filePath The location where the file will be saved.
   * @throws IOException If there's a problem writing to the file.
   */
  void export(CalendarInterface calendar, String filePath) throws IOException;
}