package calendar.command;

import calendar.exception.CalendarException;
import calendar.service.InExportService;
import calendar.view.InCalendarView;
import java.nio.file.Path;

/**
 * Command for exporting calendar to CSV file.
 */
public class ExportCommand implements InCommand {

  private final InExportService exportService;
  private final InCalendarView view;
  private final Path outputPath;

  /**
   * Constructs an ExportCommand.
   *
   * @param exportService the export service
   * @param view          the view
   * @param outputPath    path where CSV should be saved
   */
  public ExportCommand(InExportService exportService, InCalendarView view,
                       Path outputPath) {
    this.exportService = exportService;
    this.view = view;
    this.outputPath = outputPath;
  }

  @Override
  public void execute() throws CalendarException {
    Path absolutePath = exportService.exportToCsv(outputPath);
    view.displaySuccess("Calendar exported to: " + absolutePath);
  }

  @Override
  public String getDescription() {
    return "Export calendar to: " + outputPath;
  }
}