package calendar.controller.text.commandfactory;

import calendar.controller.exporter.AbstractExportCalendar;
import calendar.controller.exporter.CsvExporter;
import calendar.controller.exporter.IcsExporter;
import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.model.event.CalendarEvent;
import calendar.view.CalendarViewInterface;
import java.util.List;
import java.util.Map;

/**
 * Exports the calendar in csv format.
 */
public class ExportCalendar implements CommandInterface {
  private final Map<String, Object> parameters;
  private final CalendarModelInterface model;
  private final CalendarViewInterface view;
  private final CalendarManagerInterface calendar;

  /**
   * Constructor to initialize the parameters, model from active calendar, and view when
   * this class is created.
   *
   * @param parameters map of all placeholder command names to its values
   * @param calendar   the active calendar
   * @param view       the calendar view
   * @throws IllegalArgumentException for missing parameters
   */
  public ExportCalendar(Map<String, Object> parameters,
                        CalendarManagerInterface calendar,
                        CalendarViewInterface view) throws IllegalArgumentException {
    if (calendar == null || view == null) {
      throw new IllegalArgumentException(
          "Calendar or View is non-existent in ExportCalendar");
    }
    this.parameters = parameters;
    this.calendar = calendar;
    this.model = calendar.getActiveCalendar();
    this.view = view;
  }

  @Override
  public void execute() {
    try {
      List<CalendarEvent> eventList = model.getAllEvents();
      if (eventList.isEmpty()) {
        view.displayError("No events exist in calendar " + calendar.getCalendarName() + ".");
      } else {
        AbstractExportCalendar exporter = getExporter();
        Boolean exported = exporter.exportHelper(eventList);
        if (exported) {
          view.display("Exported events in calendar " + calendar.getCalendarName() + ".");
        }
      }
    } catch (Exception e) {
      view.displayError("ExportCalendar command: " + e.getMessage());
    }
  }

  /**
   * Checks the file extension then returns the correct exporter class.
   *
   * @return an exporter class of the correct extension
   */
  public AbstractExportCalendar getExporter() {
    String filename = this.parameters.get("filename").toString();
    String extension = filename.split("\\.")[1];
    switch (extension) {
      case "csv":
        return new CsvExporter(filename);
      case "ics":
        return new IcsExporter(filename, this.calendar);
      default:
        throw new IllegalArgumentException("Unsupported file extension: " + extension);
    }
  }
}
