package calendar.util;

import calendar.model.Calendar;
import calendar.model.Event;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exporter interface for exporting calendars to different formats.
 */
public interface Exporter {

  /**
   * Exports a calendar to a file.
   *
   * @param calendar Calendar to export
   * @param filename Output filename (with extension)
   * @return Absolute path of the created file
   * @throws IOException if file cannot be written
   * @throws IllegalArgumentException if calendar or filename is invalid
   */
  String export(Calendar calendar, String filename) throws IOException;

  /**
   * Gets the file extension this exporter handles.
   *
   * @return File extension (e.g., "csv", "ical")
   */
  String getFileExtension();

  /**
   * Gets a human-readable name for this export format.
   *
   * @return Format name (e.g., "CSV", "iCalendar")
   */
  String getFormatName();

  /**
   * Writes format-specific header to the file.
   *
   * @param writer File writer
   * @param calendar Calendar being exported
   * @throws IOException if write fails
   */
  void writeHeader(FileWriter writer, Calendar calendar) throws IOException;

  /**
   * Writes a single event in format-specific way.
   *
   * @param writer File writer
   * @param event Event to write
   * @param calendar Parent calendar (for timezone, etc.)
   * @throws IOException if write fails
   */
  void writeEvent(FileWriter writer, Event event, Calendar calendar) throws IOException;

  /**
   * Writes format-specific footer to the file.
   *
   * @param writer File writer
   * @param calendar Calendar being exported
   * @throws IOException if write fails
   */
  void writeFooter(FileWriter writer, Calendar calendar) throws IOException;

  /**
   * Escapes special characters for the specific format.
   *
   * @param value String to escape
   * @return Escaped string safe for the format
   */
  String escapeString(String value);
}