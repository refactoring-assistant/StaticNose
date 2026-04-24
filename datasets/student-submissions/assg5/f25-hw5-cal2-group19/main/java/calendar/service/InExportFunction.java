package calendar.service;

import calendar.exception.CalendarException;
import calendar.model.InCalendar;
import java.nio.file.Path;

/**
 * Interface for different export formats.
 * Allows adding new export formats without modifying existing code (Open/Closed Principle).
 * Implementations: CsvExportFunction, ICalExportFunction
 */
public interface InExportFunction {

  /**
   * Exports the calendar to the specified path in the main function's format.
   *
   * @param calendar the calendar to export
   * @param outputPath the destination file path
   * @return the absolute path of the created file
   * @throws CalendarException if export fails
   */
  Path export(InCalendar calendar, Path outputPath) throws CalendarException;

  /**
   * Gets the file extension for this export format.
   *
   * @return the file extension (e.g., "csv", "ics")
   */
  String getFileExtension();
}