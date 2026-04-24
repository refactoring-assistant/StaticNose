package calendar.view;

import java.util.List;

/**
 * An implementation of the CalendarView interface that displays output
 * to the standard command line.
 */
public class Interface implements CalendarView {

  /**
   * Displays a list of event strings, one per line, as a bulleted list.
   *
   * @param eventStrings A list of pre-formatted strings, each representing one event.
   */
  @Override
  public void displayEvents(List<String> eventStrings) {
    if (eventStrings.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (String event : eventStrings) {
      System.out.println("- " + event);
    }
  }

  /**
   * Displays the user's status.
   *
   * @param status The status string to display (e.g., "Busy", "Available").
   */
  @Override
  public void displayStatus(String status) {
    System.out.println("Status: " + status);
  }

  /**
   * Displays a general success message to the user.
   *
   * @param message The success message to show.
   */
  @Override
  public void displaySuccess(String message) {
    System.out.println("Success: " + message);
  }

  /**
   * Displays an error message to the user (prints to System.err).
   *
   * @param message The error message to show.
   */
  @Override
  public void displayError(String message) {
    System.err.println("Error: " + message);
  }

  /**
   * Displays the success message for an export, including the file path.
   *
   * @param absolutePath The platform-independent absolute path to the exported CSV file.
   */
  @Override
  public void displayExportResult(String absolutePath) {
    System.out.println("Calendar successfully exported to: " + absolutePath);
  }

  /**
   * Displays the command prompt for interactive mode.
   */
  @Override
  public void showPrompt() {
    System.out.print("> ");
  }
}
