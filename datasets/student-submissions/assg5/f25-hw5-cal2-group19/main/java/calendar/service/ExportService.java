package calendar.service;

import calendar.exception.CalendarException;
import calendar.model.InCalendar;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Unified export service that supports multiple formats using strategy pattern.
 * Automatically detects export format based on file extension.
 * Supported formats: CSV (.csv), iCal (.ics, .ical)
 */
public class ExportService implements InExportService {

  private final Map<String, InExportFunction> exportFunctionMap;

  /**
   * Constructs an ExportService with timezone for iCal export.
   * Registers both CSV and iCal export strategies.
   *
   * @param timezone the timezone for datetime conversions in iCal format
   */
  public ExportService(ZoneId timezone) {
    Objects.requireNonNull(timezone, "Timezone cannot be null");

    this.exportFunctionMap = new HashMap<>();

    CsvExportFunction csvFunction = new CsvExportFunction();
    CalExportFunction icalFunction = new CalExportFunction(timezone);

    exportFunctionMap.put("csv", csvFunction);
    exportFunctionMap.put("ics", icalFunction);
    exportFunctionMap.put("ical", icalFunction);
  }

  @Override
  public Path exportToCsv(Path outputPath) throws CalendarException {
    return null;
  }

  @Override
  public Path exportCalendar(InCalendar calendar, Path outputPath)
      throws CalendarException {
    Objects.requireNonNull(calendar, "Calendar cannot be null");
    Objects.requireNonNull(outputPath, "Output path cannot be null");

    String extension = getFileExtension(outputPath);
    InExportFunction inExportFunction = exportFunctionMap.get(extension.toLowerCase());

    if (inExportFunction == null) {
      throw new CalendarException(
          "Unsupported export format: ." + extension + ". "
              + "Supported formats: .csv, .ics, .ical");
    }

    return inExportFunction.export(calendar, outputPath);
  }

  /**
   * Extracts the file extension from a path.
   *
   * @param path the file path
   * @return the file extension without the dot (e.g., "csv", "ics")
   */
  private String getFileExtension(Path path) {
    String fileName = path.getFileName().toString();
    int lastDot = fileName.lastIndexOf('.');

    if (lastDot == -1 || lastDot == fileName.length() - 1) {
      throw new IllegalArgumentException(
          "File must have an extension (e.g., .csv or .ics): " + fileName);
    }

    return fileName.substring(lastDot + 1);
  }

  @Override
  public String toString() {
    return "ExportService{supportedFormats=" + exportFunctionMap.keySet() + "}";
  }
}