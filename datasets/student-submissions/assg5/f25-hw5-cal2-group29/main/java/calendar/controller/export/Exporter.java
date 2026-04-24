package calendar.controller.export;

import calendar.model.Calendar;
import java.io.IOException;

/**
 * Interface for all file exporters .
 * Defines a common "contract" for exporting a single calendar's data
 * to a file, allowing for different formats (CSV, iCal, etc.).
 */
public interface Exporter {

  /**
   * Exports the data from a given calendar to a specified file path.
   *
   * @param calendar The calendar object containing the events to export.
   * @param filePath The platform-independent path to the destination file.
   * @return The absolute path of the created file, for display to the user.
   * @throws IOException              if a file writing error occurs.
   * @throws IllegalArgumentException if the calendar is null or filePath is invalid.
   */
  String export(Calendar calendar, String filePath) throws IOException, IllegalArgumentException;
}