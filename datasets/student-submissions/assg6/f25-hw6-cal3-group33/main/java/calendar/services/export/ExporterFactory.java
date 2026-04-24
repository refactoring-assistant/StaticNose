package calendar.services.export;

/**
 * Factory class for creating appropriate exporters based on file extension.
 * Supports Csv and iCal formats.
 */
public class ExporterFactory {




  /**
   * Returns the appropriate exporter based on the file extension.
   *
   * @param filePath the file path with extension
   * @return the appropriate Exporter implementation
   * @throws IllegalArgumentException if file extension is not supported
   */
  public static ExportInterface getExporter(String filePath) {
    String extension = getFileExtension(filePath);

    switch (extension) {
      case ".csv":
        return new CsvExporter();
      case ".ical":
      case ".ics":
        return new IcalExporter();
      default:
        throw new IllegalArgumentException(
            "Unsupported export format: " + extension + ". Supported formats: .csv, .ical, .ics");
    }
  }

  /**
   * Extracts the file extension from a file path.
   */
  private static String getFileExtension(String filePath) {
    int lastDotIndex = filePath.lastIndexOf('.');

    if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
      throw new IllegalArgumentException("File path must have a valid extension: " + filePath);
    }

    return filePath.substring(lastDotIndex);
  }
}