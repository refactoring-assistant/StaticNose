package calendar.export;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating appropriate calendar exporters based on file extension.
 */
public class ExporterFactory {

  private static final Map<String, CalendarExporter> exporters = new HashMap<>();

  static {
    registerExporter(new CsvExport());
    registerExporter(new IcalendarExport());
  }

  /**
   * Registers an exporter.
   */
  private static void registerExporter(CalendarExporter exporter) {
    exporters.put(exporter.getFileExtension().toLowerCase(), exporter);
  }

  /**
   * Gets the appropriate exporter for a file extension.
   *
   * @param fileExtension the file extension (e.g., "csv", "ical")
   * @return the appropriate exporter
   * @throws IllegalArgumentException if no exporter found for extension
   */
  public static CalendarExporter getExporter(String fileExtension) {
    String ext = fileExtension.toLowerCase();
    CalendarExporter exporter = exporters.get(ext);

    if (exporter == null) {
      throw new IllegalArgumentException("No exporter found for extension: " + fileExtension);
    }

    return exporter;
  }

  /**
   * Gets the appropriate exporter based on filename.
   *
   * @param fileName the filename
   * @return the appropriate exporter
   * @throws IllegalArgumentException if no exporter found or no extension
   */
  public static CalendarExporter getExporterForFile(String fileName) {
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot == -1 || lastDot == fileName.length() - 1) {
      throw new IllegalArgumentException("File must have an extension");
    }

    String extension = fileName.substring(lastDot + 1);
    return getExporter(extension);
  }
}