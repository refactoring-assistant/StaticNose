package calendar.controller;

import calendar.command.InCommand;
import calendar.exception.CalendarException;
import calendar.exception.InvalidCommandException;
import calendar.service.InEventService;
import calendar.service.InExportService;
import calendar.view.InCalendarView;

/**
 * Orchestrates command execution using Command pattern.
 * Delegates to appropriate services while maintaining separation of concerns.
 * - Implements InCalendarController for abstraction and testability
 * - Uses dependency injection for loose coupling
 */
public class CalendarController implements InCalendarController {

  private static final String MSG_STARTED = "Calendar application started. Type 'exit' to quit.";
  private static final String MSG_SHUTDOWN = "Calendar application shutting down.";
  private static final String MSG_INVALID_COMMAND = "Invalid command: ";
  private static final String MSG_EXECUTION_ERROR = "Error executing command: ";
  private static final String MSG_UNEXPECTED_ERROR = "Unexpected error: ";

  private final InEventService eventService;
  private final InExportService exportService;
  private final InCalendarView view;
  private final CommandParser parser;
  private boolean running;

  /**
   * Constructs a CalendarController with required dependencies.
   *
   * @param eventService  the event service for business logic
   * @param exportService the export service for calendar export
   * @param view          the view for user interaction
   * @throws IllegalArgumentException if any dependency is null
   */
  public CalendarController(InEventService eventService,
                            InExportService exportService,
                            InCalendarView view) {
    if (eventService == null || exportService == null || view == null) {
      throw new IllegalArgumentException("Dependencies cannot be null");
    }
    this.eventService = eventService;
    this.exportService = exportService;
    this.view = view;
    this.parser = new CommandParser(eventService, exportService, view);
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
      view.displayError(MSG_INVALID_COMMAND + e.getMessage());
      throw e;
    } catch (CalendarException e) {
      view.displayError(MSG_EXECUTION_ERROR + e.getMessage());
    } catch (Exception e) {
      view.displayError(MSG_UNEXPECTED_ERROR + e.getMessage());
    }
  }

  @Override
  public void start() {
    running = true;
    view.displayMessage(MSG_STARTED);
  }

  @Override
  public void shutdown() {
    running = false;
    view.displayMessage(MSG_SHUTDOWN);
  }

  /**
   * Checks if the controller is currently running.
   * Used by CalendarRunner to determine when to stop reading commands.
   *
   * @return true if running, false otherwise
   */
  public boolean isRunning() {
    return running;
  }
}