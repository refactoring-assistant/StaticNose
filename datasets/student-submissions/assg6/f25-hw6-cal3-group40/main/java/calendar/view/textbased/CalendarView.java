package calendar.view.textbased;

import calendar.view.EventViewData;
import java.util.List;

/**
 * Represents the View in the MVC pattern.
 * Its only job is to display information to the user,
 * it has no logic.
 */
public interface CalendarView {

  /**
   * Displays a generic success or informational message.
   *
   * @param message The message to display.
   */
  void showMessage(String message);

  /**
   * Displays an error message.
   *
   * @param errorMessage The error message to display.
   */
  void showError(String errorMessage);

  /**
   * Displays the command prompt in the main loop.
   */
  void showPrompt();

  /**
   * Displays a list of events in the "bulleted list" format.
   * (Used for 'print events on (date)')
   *
   * @param events The list of events to display.
   */
  void showEvents(List<EventViewData> events);

  /**
   * Displays a list of events in the "schedule" format.
   * (Used for 'print events from (start) to (end)')
   *
   * @param events The list of events to display.
   */
  void showEventSchedule(List<EventViewData> events);

  /**
   * Displays the result of a calendar export.
   *
   * @param path The absolute path of the exported file.
   */
  void showExportResult(String path);

  /**
   * Displays the user's status (busy/available).
   *
   * @param isBusy True if the user has an event, false otherwise.
   */
  void showStatus(boolean isBusy);

  /**
   * Displays the help message with all available commands.
   */
  void showHelp();

}
