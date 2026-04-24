package calendar.controller.calendar;

import calendar.controller.Command;
import calendar.controller.export.Exporter;
import calendar.controller.export.ExporterFactory;
import calendar.model.Calendar;
import calendar.model.CalendarApplication;
import calendar.view.CalendarView;

/**
 * Command for exporting cal.
 * Implements the new Command interface and delegates to the ExporterFactory.
 */
public class ExportCalendarCommand implements Command {

  private final String filePath;

  /**
   * Constructor to initialize the object.
   *
   * @param filePath path to export the calendar.
   */
  public ExportCalendarCommand(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be empty.");
    }
    this.filePath = filePath;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      Calendar activeCalendar = model.getActiveCalendar();
      Exporter exporter = ExporterFactory.getExporter(filePath);
      String absolutePath = exporter.export(activeCalendar, filePath);
      view.displayExportResult(absolutePath);
    } catch (Exception e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    }
  }
}