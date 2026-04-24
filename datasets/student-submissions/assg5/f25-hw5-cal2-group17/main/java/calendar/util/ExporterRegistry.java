package calendar.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for exporters mapped to file extensions.
 * Implements Registry pattern to select appropriate exporter based on file format.
 */
public class ExporterRegistry {
  private static final Map<String, Exporter> exporters = new HashMap<>();

  // Register available exporters
  static {
    register(new CsvExporter());
    register(new IcalExporter());
  }

  // Private constructor to prevent instantiation
  private ExporterRegistry() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Registers an exporter for a specific file extension.
   *
   * @param exporter Exporter to register
   */
  public static void register(Exporter exporter) {
    if (exporter == null) {
      throw new IllegalArgumentException("Exporter cannot be null");
    }
    exporters.put(exporter.getFileExtension().toLowerCase(), exporter);
  }

  /**
   * Gets an exporter based on filename extension.
   *
   * @param filename Filename with extension (e.g., "calendar.csv")
   * @return Appropriate exporter for the file format
   * @throws IllegalArgumentException if format is not supported
   */
  public static Exporter getExporter(String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be null or empty");
    }

    String extension = extractExtension(filename);

    if (extension.isEmpty()) {
      throw new IllegalArgumentException(
          "Filename must have an extension (e.g., .csv, .ical)");
    }

    Exporter exporter = exporters.get(extension.toLowerCase());

    if (exporter == null) {
      throw new IllegalArgumentException(
          "Unsupported file format: ." + extension
              + ". Supported formats: " + getSupportedFormats());
    }

    return exporter;
  }

  /**
   * Extracts file extension from filename.
   *
   * @param filename Filename
   * @return Extension without dot (e.g., "csv")
   */
  private static String extractExtension(String filename) {
    int lastDot = filename.lastIndexOf('.');
    if (lastDot == -1 || lastDot == filename.length() - 1) {
      return "";
    }
    return filename.substring(lastDot + 1);
  }

  /**
   * Gets list of supported file formats.
   *
   * @return Comma-separated list of supported extensions
   */
  public static String getSupportedFormats() {
    return String.join(", ", exporters.keySet());
  }

  /**
   * Checks if a file format is supported.
   *
   * @param filename Filename to check
   * @return true if format is supported
   */
  public static boolean isFormatSupported(String filename) {
    try {
      String extension = extractExtension(filename);
      return exporters.containsKey(extension.toLowerCase());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets the format name for a filename.
   *
   * @param filename Filename
   * @return Format name (e.g., "CSV", "iCalendar")
   * @throws IllegalArgumentException if format not supported
   */
  public static String getFormatName(String filename) {
    Exporter exporter = getExporter(filename);
    return exporter.getFormatName();
  }

}