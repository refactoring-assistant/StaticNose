package calendar.service;

import calendar.exception.CalendarException;
import java.nio.file.Path;

/**
 * Service for exporting calendar data.
 */
public interface InExportService {

  /**
   * Exports the calendar to a CSV file.
   *
   * @param outputPath the path where CSV should be saved
   * @return the absolute path of the created file
   * @throws CalendarException if export fails
   */
  Path exportToCsv(Path outputPath) throws CalendarException;
}