package calendar.controller.export;

/**
 * Factory class to decide which exporter to use.
 */
public class ExporterFactory {

  /**
   * Gets the appropriate exporter based on the file extension.
   *
   * @param filePath The destination file path (e.g., "myCal.csv", "myCal.ical").
   * @return An implementation of the Exporter interface.
   * @throws IllegalArgumentException if the file extension is not supported.
   */
  public static Exporter getExporter(String filePath) {
    if (filePath == null) {
      throw new IllegalArgumentException("File path cannot be null.");
    }
    String lowerPath = filePath.toLowerCase();

    if (lowerPath.endsWith(".csv")) {
      return new CsvExporter();
    }
    if (lowerPath.endsWith(".ical") || lowerPath.endsWith(".ics")) {
      return new IcalExporter();
    }

    throw new IllegalArgumentException(
        "Unsupported file format. Please use '.csv' or '.ical'.");
  }
}