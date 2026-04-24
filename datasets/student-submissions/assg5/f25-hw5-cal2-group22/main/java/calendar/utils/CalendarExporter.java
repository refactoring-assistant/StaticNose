package calendar.utils;

/**
 * Class for creating appropriate exporters based on file extension.
 */

public class CalendarExporter {

  private CalendarExporter() {
    throw new AssertionError("No instances");
  }

  /**
   * Gets the appropriate exporter for the given filename.
   *
   * @param filename the output filename
   * @return the appropriate exporter
   * @throws IllegalArgumentException if file extension is not supported
   */
  public static Iexporter getExporter(String filename) {
    if (filename == null || filename.isEmpty()) {
      throw new IllegalArgumentException("Filename cannot be null or empty");
    }

    String lowerFilename = filename.toLowerCase();

    if (lowerFilename.endsWith(".csv")) {
      return new CsvExporter();
    } else if (lowerFilename.endsWith(".ical") || lowerFilename.endsWith(".ics")) {
      return new IcalExporter();
    } else {
      throw new IllegalArgumentException(
          "Unsupported file format. Supported formats: .csv, .ical, .ics");
    }
  }

  /**
   * Checks if the given filename has a supported export format.
   *
   * @param filename the filename to check
   * @return true if the format is supported
   */
  public static boolean isSupportedFormat(String filename) {
    if (filename == null || filename.isEmpty()) {
      return false;
    }
    String lower = filename.toLowerCase();
    return lower.endsWith(".csv") || lower.endsWith(".ical") || lower.endsWith(".ics");
  }
}