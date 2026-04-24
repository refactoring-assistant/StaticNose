package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Command to export the calendar to a CSV file.
 */
public class ExportCommand extends AbstractCommand {
  private final String fileName;

  /**
   * Constructs an ExportCommand to export the calendar to a CSV file.
   *
   * @param filename the name of the file to export to
   */
  public ExportCommand(String filename) {
    this.fileName = filename;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    try {
      String absolutePath = model.exportToCsv(fileName);
      view.displayMessage("Calendar exported successfully to: " + absolutePath);
    } catch (IOException e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    }
  }
}