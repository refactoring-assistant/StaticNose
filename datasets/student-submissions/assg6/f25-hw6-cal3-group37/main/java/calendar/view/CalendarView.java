package calendar.view;

import calendar.model.CalendarEvent;
import java.util.List;

/**
 * Interface for calendar view operations.
 * Handles all output/display responsibilities.
 */
public interface CalendarView {

  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays a list of events.
   *
   * @param events the events to display
   * @param showDateRange if true, show events in range format
   */
  void displayEvents(List<CalendarEvent> events, boolean showDateRange);

  /**
   * Displays busy/available status.
   *
   * @param isBusy true if busy, false if available
   */
  void displayStatus(boolean isBusy);

  /**
   * Displays the path of an exported file.
   *
   * @param filePath the absolute path of the exported file
   */
  void displayExportPath(String filePath);
}