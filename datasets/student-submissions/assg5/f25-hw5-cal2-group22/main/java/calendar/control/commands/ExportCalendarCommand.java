package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.AbstractEvent;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.CalendarExporter;
import calendar.utils.Iexporter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command: export all events to a file in CSV or iCal format.
 * The format is automatically detected from the file extension.
 * Syntax:
 * export cal fileName.csv
 * export cal fileName.ical
 * export cal fileName.ics
 */
public class ExportCalendarCommand extends AbstractCommand {

  /**
   * Constructor creating the export command object.
   *
   * @param multipleCalendar calendar database instance with events
   * @param input            raw command string
   */
  public ExportCalendarCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  /**
   * Executes the export command.
   * Detects file format from extension and uses appropriate exporter.
   *
   * @return the result to be printed on view
   */
  @Override
  public CommandResult execute() {
    try {
      if (!multipleCalendar.hasCalendars()) {
        return CommandResult.error(
            "No calendars exist. Create a calendar first using:\n"
                + "  create calendar --name <name> --timezone <timezone>");
      }

      Optional<Imodel> currentCal = multipleCalendar.getCurrent();
      if (currentCal.isEmpty()) {
        return CommandResult.error(
            "No calendar is currently selected. Use:\n"
                + "  use calendar --name <name>\n\n"
                + "Available calendars: " + getAvailableCalendarNames());
      }

      Pattern p = Pattern.compile(
          "export\\s+cal\\s+(\\S+\\.(csv|ical|ics))",
          Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(input);

      if (!m.find()) {
        return CommandResult.error(
            "Invalid export command. Supported formats:\n"
                + "  export cal filename.csv\n"
                + "  export cal filename.ical\n");
      }

      String filename = m.group(1);

      if (!CalendarExporter.isSupportedFormat(filename)) {
        return CommandResult.error(
            "Unsupported file format. Use .csv or .ical extension.");
      }

      Imodel model = currentCal.get();
      List<AbstractEvent> events = model.getAllEvents();

      String fullPath = "res/" + filename;
      Iexporter exporter = CalendarExporter.getExporter(filename);
      String result = exporter.export(events, fullPath);

      if (result.contains("successfully")) {
        return CommandResult.success(result);
      } else {
        return CommandResult.error(result);
      }

    } catch (IllegalArgumentException e) {
      return CommandResult.error("Export error: " + e.getMessage());
    } catch (Exception e) {
      return CommandResult.error("Export failed: " + e.getMessage());
    }
  }

}