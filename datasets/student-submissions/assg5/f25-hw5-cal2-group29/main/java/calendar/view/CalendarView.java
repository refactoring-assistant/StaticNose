package calendar.view;

import java.util.List;

/**
 * This interface defines methods for displaying information to the user.
 */
public interface CalendarView {

  /**
   * Displays a list of event strings to the user, typically as a bulleted list.
   * Used for 'print events on' and 'print events from'.
   *
   * @param eventStrings A list of pre-formatted strings, each representing one event.
   */
  void displayEvents(List<String> eventStrings);

  /**
   * Displays the user's status (e.g., "Busy", "Available").
   *
   * @param status The status string to display.
   */
  void displayStatus(String status);

  /**
   * Displays a general success message to the user.
   *
   * @param message The success message to show (e.g., "Event created successfully.").
   */
  void displaySuccess(String message);

  /**
   * Displays an error message to the user.
   *
   * @param message The error message to show (e.g., "Event conflict detected.").
   */
  void displayError(String message);

  /**
   * Displays the result of a successful calendar export.
   *
   * @param absolutePath The platform-independent absolute path to the exported CSV file.
   */
  void displayExportResult(String absolutePath);

  /**
   * Displays the command prompt (e.g., "> ") for interactive mode.
   */
  void showPrompt();
}