package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.util.CalExporter;
import calendar.util.CsvExporter;
import calendar.view.CalendarView;
import java.time.ZoneId;

/**
 * Command to export calendar to CSV or iCal file.
 * Auto-detects format based on file extension.
 */
public class ExportCommand implements Command {
  private final String fileName;

  /**
   * Constructs an ExportCommand with the specified file name.
   *
   * @param fileName the name of the file to export to
   */
  public ExportCommand(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    // Old behavior for backward compatibility (CSV only)
    try {
      String absolutePath = CsvExporter.exportToCsv(
          calendar.getAllEvents(), fileName);
      view.displayExportPath(absolutePath);
    } catch (Exception e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    }
  }

  /**
   * Executes export with timezone support.
   *
   * @param context the calendar context
   * @param view the view
   */
  public void executeOnSystem(CalendarContext context, CalendarView view) {
    try {
      CalendarModel calendar = context.getCurrentCalendar();
      String calendarName = context.getCurrentCalendarName();
      ZoneId timezone = context.getSystem().getCalendarTimezone(calendarName);

      String absolutePath;

      // Auto-detect format by file extension
      if (fileName.toLowerCase().endsWith(".ical")
          || fileName.toLowerCase().endsWith(".ics")) {
        // iCal format
        absolutePath = CalExporter.exportToIcal(
            calendar.getAllEvents(), fileName, timezone);
      } else {
        // CSV format (default)
        absolutePath = CsvExporter.exportToCsv(
            calendar.getAllEvents(), fileName);
      }

      view.displayExportPath(absolutePath);

    } catch (Exception e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    }
  }
}