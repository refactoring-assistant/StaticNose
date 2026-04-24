package controller.command;

import controller.CommandResult;
import java.io.File;
import model.Icalendar;


/**
 * Command implementation for exporting the calendar to a CSV file.
 * The exported file follows Google Calendar's CSV format for compatibility.
 */
public class ExportCommand implements Command {
  private final String filename;

  /**
   * Constructs an ExportCommand with the specified filename.
   *
   * @param filename the name of the file to create
   */
  public ExportCommand(String filename) {
    this.filename = filename;
  }


  /**
   * Executes the export command, creating a CSV file with all calendar events.
   * The method returns the absolute path of the created file on success.
   *
   * @param calendar the calendar model to export from
   * @return a CommandResult containing the file path or error message
   */
  @Override
  public CommandResult execute(Icalendar calendar) {
    try {
      calendar.exportToCsv(filename);
      File file = new File(filename);
      String absolutePath = file.getAbsolutePath();
      return new CommandResult(true, "Calendar exported to: " + absolutePath);
    } catch (Exception e) {
      return new CommandResult(false, "Error exporting calendar: " + e.getMessage());
    }
  }
}