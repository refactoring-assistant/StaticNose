package calendar.view;

import java.util.List;

/**
 * Represents the View in the MVC pattern.
 * Its only job is to display information to the user.
 */
public interface InterfaceCalendarView {

  /**
   * Displays a bulleted list of events.
   *
   * @param eventDetails A list of pre-formatted event detail strings.
   */
  void displayEvents(List<String> eventDetails);

  /**
   * Displays the user's status (Busy/Available).
   *
   * @param status The status string to display.
   */
  void displayStatus(String status);

  /**
   * Displays a standard, non-error message (e.g., "Event created.").
   *
   * @param message The message to display.
   */
  void displayMessage(String message);

  /**
   * Displays an error message (e.g., "Invalid command.").
   *
   * @param error The error message to display.
   */
  void displayError(String error);

  /**
   * Displays confirmation of file export.
   *
   * @param absolutePath The absolute path of the generated CSV file.
   */
  void displayExportConfirmation(String absolutePath);
}