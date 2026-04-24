package calendar.controller.commands;

import calendar.controller.ParsedCommand;
import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.util.CsvExporter;
import calendar.util.IcalExporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Exports calendar to CSV or iCal format based on file extension.
 */
public class ExportCommand implements CalendarCommand {

  private final CalendarManager manager;
  private final ParsedCommand cmd;

  /**
   * Constructs the command.
   *
   * @param manager calendar manager
   * @param cmd parsed command
   */
  public ExportCommand(CalendarManager manager, ParsedCommand cmd) {
    this.manager = manager;
    this.cmd = cmd;
  }

  /**
   * Executes the command.
   *
   * @return result string
   */
  @Override
  public String execute() {
    Calendar current = manager.getCurrentCalendar()
        .orElseThrow(() -> new IllegalStateException("No calendar in use"));

    try {
      Path targetPath = Paths.get("").toAbsolutePath().resolve(cmd.args.get("file"));

      Path parent = targetPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }

      String fileName = targetPath.getFileName().toString().toLowerCase();

      List<Event> events = current.getService().getAllEvents();
      Path result;

      if (fileName.endsWith(".ical") || fileName.endsWith(".ics")) {
        result = IcalExporter.exportToIcal(events,
            current.getTimezone(), targetPath);
      } else {
        result = CsvExporter.exportToCsv(events, targetPath);
      }

      return result.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to export: " + e.getMessage(), e);
    }
  }
}