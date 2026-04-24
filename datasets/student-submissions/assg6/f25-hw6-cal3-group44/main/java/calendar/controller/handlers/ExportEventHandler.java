package calendar.controller.handlers;

import calendar.controller.commanddata.ExportCommandData;
import calendar.controller.export.CsvExporter;
import calendar.controller.export.FileExporter;
import calendar.controller.export.IcalExporter;
import calendar.model.interfaces.CalendarEditable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handler class that performs the logic for exporting events.
 * Takes parsed command data and interacts with the model.
 */
public class ExportEventHandler {

  private final CalendarEditable calendarModel;
  private final Map<String, FileExporter> fileExporter;

  /**
   * Constructor for ExportEventHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public ExportEventHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
    this.fileExporter = new HashMap<>();
    fileExporter.put("csv", new CsvExporter());
    fileExporter.put("ical", new IcalExporter());
  }

  /**
   * Executes the export logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(ExportCommandData data) {
    String fileName = data.getFileName();
    String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
    FileExporter exporter = fileExporter.getOrDefault(fileType, null);
    if (exporter == null) {
      throw new IllegalArgumentException("Error: Invalid file type." + fileType);
    }
    String path = exporter.export(fileName, calendarModel.getAllEvents());
    return "Successfully exported to: " + path;
  }
}

