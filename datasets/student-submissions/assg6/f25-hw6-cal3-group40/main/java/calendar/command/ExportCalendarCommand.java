package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;
import java.io.IOException;

/**
 * Command to export the calendar to a CSV or iCal file.
 */
public class ExportCalendarCommand implements CalendarCommand {
  private final String fileName;

  /**
   * Constructs an ExportCalendarCommand.
   *
   * @param fileName The name of the file to export to (e.g., "myCal.csv" or "myCal.ical").
   */
  public ExportCalendarCommand(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) throws IllegalArgumentException {
    try {
      String absolutePath = service.exportCalendar(fileName);

      view.showExportResult(absolutePath);
    } catch (IOException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to export calendar: " + e.getMessage(), e);
    }
  }
}