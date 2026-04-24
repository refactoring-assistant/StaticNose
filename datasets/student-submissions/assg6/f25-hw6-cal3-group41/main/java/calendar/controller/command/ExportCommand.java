package calendar.controller.command;

import calendar.model.CalendarModel;
import calendar.model.MultiCalendarModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to export calendar.
 */
class ExportCommand implements Command {

  private final MultiCalendarModel multiModel;

  /**
   * Constructor.
   *
   * @param multiModel the model
   */
  public ExportCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    // Pattern: export cal <filename.csv> or export cal <filename.ical>
    // Allow spaces in path for test export directory, and backslashes for Windows paths
    Pattern p = Pattern.compile(
        "^export\\s+cal\\s+([A-Za-z0-9_\\-./\\\\\\s]+\\.(csv|ical))$",
        Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(command);
    if (!m.matches()) {
      throw new IllegalArgumentException(
          "Invalid export command. Must specify .csv or .ical extension.");
    }

    String filename = m.group(1);
    String extension = m.group(2).toLowerCase();
    
    CalendarModel model = multiModel.getCurrentModel();
    String path;
    
    if ("csv".equals(extension)) {
      path = model.exportToCsv(filename);
    } else if ("ical".equals(extension)) {
      path = model.exportToIcal(filename, multiModel.getCurrentTimezone());
    } else {
      throw new IllegalArgumentException("Unsupported export format. Use .csv or .ical");
    }
    
    return "Exported to: " + path;
  }
}

