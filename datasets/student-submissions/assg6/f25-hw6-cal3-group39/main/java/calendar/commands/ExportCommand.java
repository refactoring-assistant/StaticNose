package calendar.commands;

import calendar.export.CsvExporter;
import calendar.export.Exporter;
import calendar.export.IcalExporter;
import calendar.model.InterfaceEvent;
import calendar.model.Model;
import calendar.view.View;
import java.util.List;


/**
 * calendar.Commands.Command to export the calendar to a CSV file.
 */
public class ExportCommand implements Command {

  /**
   * Executes the export command using the provided user input, model, and view.
   *
   * @param userInput the full user input string containing the export command and filename
   * @param model     the model containing calendar data
   * @param view      the view used to display messages or errors
   */
  @Override
  public void execute(String calName, String userInput, Model model, View view) {
    String[] parts = userInput.split(" ");
    if (parts.length < 3 || !userInput.startsWith("export cal")) {
      view.showError("Error: Invalid syntax. Use: export cal <filename.ext>");
      return;
    }
    String filename = parts[2];
    try {

      Exporter exporter;
      List<InterfaceEvent> events = model.queryEvents(calName, null, null,
          null, null, true);
      if (filename.endsWith(".csv")) {
        exporter = new CsvExporter();
        exporter.export(events, filename);
      } else if (filename.endsWith(".ical")) {
        exporter = new IcalExporter();
        exporter.export(events, filename);
      } else {
        view.showError("Error: Unsupported file extension. Use .csv or .ical");
        return;
      }
      view.showMessage("Calendar exported successfully for command: " + userInput);
      java.io.File exportedFile = new java.io.File(filename);
      String absolutePath = exportedFile.getAbsolutePath();
      view.showMessage("Calendar exported to: " + absolutePath);
    } catch (Exception e) {
      view.showError("Unexpected error during export: " + e.getMessage());
    }
  }
}
