package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import calendar.model.CalendarInterface;
import calendar.util.CalendarExporterInterface;
import calendar.util.CsvCalendarExporter;
import calendar.util.IcalCalendarExporter;
import java.io.File;
import java.io.IOException;

/**
 * Command to export the active calendar to a CSV or iCal file.
 * The format is automatically detected from the file extension:
 * - .csv → CSV format
 * - .ical or .ics → iCal format
 * The absolute path of the exported file is printed upon success.
 */
public class ExportCalendarCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final String filename;

  /**
   * Creates a command to export the current calendar to the specified file.
   */
  public ExportCalendarCommand(CalendarManagerControllerInterface controller,
                               String filename) {
    this.controller = controller;
    this.filename = filename;
  }

  @Override
  public String execute() {
    try {
      CalendarInterface activeCalendar = controller.getActiveCalendar();
      File file = new File(filename);
      String absolutePath = file.getAbsolutePath();
      CalendarExporterInterface exporter;
      String lowerFilename = filename.toLowerCase();

      if (lowerFilename.endsWith(".csv")) {
        exporter = new CsvCalendarExporter();
      } else if (lowerFilename.endsWith(".ical") || lowerFilename.endsWith(".ics")) {
        exporter = new IcalCalendarExporter();
      } else {
        return "Error: Unsupported file extension. Use .csv, .ical, or .ics";
      }

      exporter.export(activeCalendar, absolutePath);
      return "Calendar successfully exported to " + absolutePath;

    } catch (RuntimeException e) {
      if (e.getMessage() != null && e.getMessage().contains("No active calendar")) {
        return "Error: No active calendar. Use 'use calendar' command first.";
      }
      return "Error: " + e.getMessage();
    } catch (IOException e) {
      return "Error writing file: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Export calendar to file: " + filename + " (auto-detects format from extension)";
  }
}