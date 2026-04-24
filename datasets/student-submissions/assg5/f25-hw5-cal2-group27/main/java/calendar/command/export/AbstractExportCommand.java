package calendar.command.export;

import calendar.command.event.AbstractEventCommand;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.MyCalendar;
import calendar.view.CalendarTextView;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Base class for export commands.
 */
public abstract class AbstractExportCommand extends AbstractEventCommand
        implements Export {

  /** File to export to. */
  protected final String fileName;

  /**
   * Constructs a new export command.
   *
   * @param fileName the output file name
   */
  public AbstractExportCommand(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Executes the export for the active calendar.
   *
   * @param manager  the calendar manager
   * @param calendar the active calendar to be exported
   * @param view     the output view for user messages
   */
  @Override
  protected void executeWithCalendar(
          CalendarManager manager,
          MyCalendar calendar,
          CalendarTextView view) {

    Path target = Paths.get(System.getProperty("user.dir"), fileName);
    List<Event> events = List.copyOf(calendar.getEvents());
    try {
      write(events, target);
      view.printMessage("Calendar exported successfully to: " + target);
    } catch (IOException e) {
      view.printMessage("Error exporting calendar: " + e.getMessage());
    }
  }

  /**
   * Fallback exporter that is returned when no registered exporter supports a filename.
   */
  static class UnsupportedExport extends AbstractExportCommand {

    /**
     * Creates an unsupported-export placeholder for the given filename.
     *
     * @param fileName the filename that could not be matched
     */
    UnsupportedExport(String fileName) {
      super(fileName);
    }

    /** Returns {@code false}. */
    @Override
    public boolean supports(String fileName) {
      return false;
    }

    @Override
    public void write(List<Event> events, Path target) {
    }

    /** Prints an error about supported extensions. */
    @Override
    protected void executeWithCalendar(
            CalendarManager manager,
            MyCalendar calendar,
            CalendarTextView view) {

      view.printMessage(
              "Error: Unsupported file extension. Supported extensions: .csv and .ical.");
    }
  }
}
