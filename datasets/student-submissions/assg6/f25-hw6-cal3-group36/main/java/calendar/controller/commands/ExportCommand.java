package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;

/**
 * Handles exporting of calendar to .csv and .ical format.
 */
public class ExportCommand implements Command {

  private final String fileName;

  /**
   * Forming a export command using user input.
   */
  public ExportCommand(String input) {
    String trimmed = input.trim();
    String[] parts = trimmed.split("\\s+");
    if (parts.length != 3
        || !parts[0].equalsIgnoreCase("export")
        || !parts[1].equalsIgnoreCase("cal")) {
      throw new IllegalArgumentException(
          "Invalid syntax. Expected: export cal <fileName>.csv or .ical");
    }
    this.fileName = parts[2];
  }

  @Override
  public String execute(CalendarSystemModel model) {
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".csv")) {
      String path = model.exportCsv(fileName);
      return "Calendar exported to CSV at: " + path;
    } else if (lower.endsWith(".ical") || lower.endsWith(".ics")) {
      String path = model.exportIcal(fileName);
      return "Calendar exported to iCal at: " + path;
    } else {
      throw new IllegalArgumentException("Unsupported export format: " + fileName);
    }
  }
}