package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.view.CsvExporter;
import calendar.view.IcalExporter;
import java.io.File;
import java.io.IOException;

/**
 * Command to export calendar events to a CSV or iCal file.
 */
public class ExportCommand implements CalendarCommand {
  private final String fileName;

  /**
   * Constructs an export command.
   *
   * @param fileName the name of the file to export to.
   */
  public ExportCommand(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public String execute(Calendar model) {
    try {
      File f = new File(fileName);
      String lowerFileName = fileName.toLowerCase();

      if (lowerFileName.endsWith(".csv")) {
        CsvExporter exporter = new CsvExporter();
        exporter.exportEvents(model.getAllEvents(), fileName);
        return "Exported calendar to: " + f.getAbsolutePath();
      } else if (lowerFileName.endsWith(".ical") || lowerFileName.endsWith(".ics")) {
        IcalExporter exporter = new IcalExporter();
        exporter.exportEvents(model.getAllEvents(), fileName, model.getTimezone());
        return "Exported calendar to: " + f.getAbsolutePath();
      } else {
        return "Error: Unsupported file format. Use .csv, .ical, or .ics extension";
      }
    } catch (IOException e) {
      return "Error: Invalid file path";
    }
  }
}