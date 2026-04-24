package calendar.controller;

import calendar.command.InCommand;
import calendar.exception.CalendarException;
import calendar.exception.InvalidCommandException;
import calendar.model.CalendarDatabase;
import calendar.service.InExportService;
import calendar.view.InCalendarView;

/**
 * Main controller that handles user commands for the calendar app.
 * - Now works with CalendarDatabase instead of single calendar
 * - Uses abstract getActiveCalendar() method for subclass flexibility
 * This class sits between the user interface and the rest of the system.
 */
public class CalendarController implements InCalendarController {

  private final InCalendarView view;
  private final CommandParser parser;
  private boolean running;

  /**
   * Constructs a CalendarController with required dependencies.
   * Now accepts CalendarDatabase instead of individual services.
   *
   * @param calendarDatabase the database managing all calendars
   * @param exportService    the export service
   * @param view             the view for user interaction
   */
  public CalendarController(CalendarDatabase calendarDatabase,
                            InExportService exportService,
                            InCalendarView view) {
    if (calendarDatabase == null) {
      throw new IllegalArgumentException("CalendarDatabase cannot be null");
    }
    if (exportService == null) {
      throw new IllegalArgumentException("Export service cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.view = view;
    this.parser = new CommandParser(calendarDatabase, exportService, view);
    this.running = false;
  }

  @Override
  public void executeCommand(String commandString) throws InvalidCommandException {
    try {
      InCommand command = parser.parse(commandString);

      if (command == null) {
        shutdown();
        return;
      }

      command.execute();
    } catch (InvalidCommandException e) {
      view.displayError("Invalid command: " + e.getMessage());
      throw e;
    } catch (CalendarException e) {
      view.displayError("Error executing command: " + e.getMessage());
    } catch (Exception e) {
      view.displayError("Unexpected error: " + e.getMessage());
    }
  }

  @Override
  public void start() {
    running = true;
    view.displayMessage("Calendar application started. Type 'exit' to quit.");
  }

  @Override
  public void shutdown() {
    running = false;
    view.displayMessage("Calendar application shutting down.");
  }

  /**
   * Checks if the controller is running.
   *
   * @return true if running, false otherwise
   */
  public boolean isRunning() {
    return running;
  }
}