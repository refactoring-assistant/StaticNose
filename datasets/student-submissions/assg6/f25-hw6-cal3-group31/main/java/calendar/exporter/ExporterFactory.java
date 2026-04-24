package calendar.exporter;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating appropriate calendar exporters based on file extension.
 * This class uses the Factory pattern to decouple the calendar from
 * specific exporter implementations.
 */
public class ExporterFactory {
  private static final Map<String, CalendarExporter> exporters = new HashMap<>();

  static {
    registerExporter(new CsvExporter());
    registerExporter(new IcalExporter());
  }

  /**
   * Registers an exporter for its supported file extensions.
   *
   * @param exporter the exporter to register
   */
  private static void registerExporter(CalendarExporter exporter) {
    for (String extension : exporter.getSupportedExtensions()) {
      exporters.put(extension.toLowerCase(), exporter);
    }
  }

  /**
   * Gets the appropriate exporter for the given file name.
   *
   * @param fileName the name of the file to export to
   * @return the appropriate exporter
   * @throws IllegalArgumentException if no exporter is found for the file extension
   */
  public static CalendarExporter getExporter(String fileName) {
    String extension = getFileExtension(fileName);

    if (extension == null || extension.isEmpty()) {
      throw new IllegalArgumentException("File name must have an extension");
    }

    CalendarExporter exporter = exporters.get(extension.toLowerCase());

    if (exporter == null) {
      throw new IllegalArgumentException(
          "Unsupported file format: " + extension + ". Supported formats: "
              + String.join(", ", exporters.keySet()));
    }

    return exporter;
  }

  /**
   * Extracts the file extension from a file name.
   *
   * @param fileName the file name
   * @return the file extension (without the dot), or null if no extension
   */
  private static String getFileExtension(String fileName) {
    if (fileName == null) {
      return null;
    }

    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
      return null;
    }

    return fileName.substring(lastDotIndex + 1);
  }

  /**
   * Checks if a file format is supported.
   *
   * @param fileName the file name to check
   * @return true if the format is supported, false otherwise
   */
  public static boolean isFormatSupported(String fileName) {
    String extension = getFileExtension(fileName);
    return extension != null && exporters.containsKey(extension.toLowerCase());
  }
}

