package calendar.command;

import calendar.exception.CalendarException;
import calendar.model.InCalendar;
import calendar.service.InExportService;
import calendar.view.InCalendarView;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Saves a calendar to a file in CSV or iCal format.
 * Format is auto-detected from file extension (.csv or .ics/.ical).
 * Uses function object (map) for format detection
 */
public class ExportCommand implements InCommand {

  private final InExportService exportService;
  private final InCalendarView view;
  private final InCalendar calendar;
  private final Path outputPath;

  /**
   * Creates a command to export a calendar to a file.
   *
   * @param exportService handles the actual file writing and formatting
   * @param view shows the view with the final file location
   * @param calendar the calendar to export
   * @param outputPath where to save the file
   */
  public ExportCommand(InExportService exportService,
                       InCalendarView view,
                       InCalendar calendar,
                       Path outputPath) {
    Objects.requireNonNull(exportService, "Export service cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(calendar, "Calendar cannot be null");
    Objects.requireNonNull(outputPath, "Output path cannot be null");

    this.exportService = exportService;
    this.view = view;
    this.calendar = calendar;
    this.outputPath = outputPath;
  }

  @Override
  public void execute() throws CalendarException {
    Path absolutePath = exportService.exportCalendar(calendar, outputPath);
    view.displaySuccess("Calendar exported to: " + absolutePath);
  }

  @Override
  public String getDescription() {
    return "Export calendar '" + calendar.getCalendarName() + "' to: " + outputPath;
  }
}