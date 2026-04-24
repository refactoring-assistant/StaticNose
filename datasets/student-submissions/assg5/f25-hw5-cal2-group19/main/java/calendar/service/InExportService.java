package calendar.service;

import calendar.exception.CalendarException;
import calendar.model.InCalendar;
import java.nio.file.Path;

/**
 * Service for exporting calendar data.
 */
public interface InExportService {

  /**
   * Exports the calendar to csv format.
   *
   * @param outputPath the file path where the CSV should be written
   * @return the path of the exported file
   * @throws CalendarException if the export operation fails
   */
  Path exportToCsv(Path outputPath) throws CalendarException;

  /**
   * Exports the calendar to a file (format determined by file extension).
   * Supported formats: .csv, .ics, .ical
   *
   * @param calendar the calendar to export
   * @param outputPath the path where file should be saved
   * @return the absolute path of the created file
   * @throws CalendarException if export fails
   */
  Path exportCalendar(InCalendar calendar, Path outputPath) throws CalendarException;
}