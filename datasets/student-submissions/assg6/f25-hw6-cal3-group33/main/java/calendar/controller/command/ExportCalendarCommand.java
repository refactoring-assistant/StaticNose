package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.event.EventInterface;
import calendar.model.manager.CalendarManager;
import calendar.services.export.ExportInterface;
import calendar.services.export.ExporterFactory;
import calendar.view.CalendarView;
import java.util.List;

/**
 * Command to export the calendar to a file.
 *
 * <p>Exports all events in the current calendar to the specified file.
 * The export format is determined by the file extension:
 * - .Csv: Google Calendar Csv format
 * - .ics: iCalendar format
 */
public class ExportCalendarCommand implements Command {

  private final String filePath;

  /**
   * Creates a command to export the calendar.
   *
   * @param filePath the path where the file should be created
   */
  public ExportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    List<EventInterface> events = calendar.getAllEvents();
    ExportInterface exporter = ExporterFactory.getExporter(filePath);
    String absolutePath = exporter.export(events, filePath);

    view.displayMessage("Calendar exported to: " + absolutePath);
  }
}