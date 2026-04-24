package calendar.view;

import java.util.List;

/**
 * A console-based implementation of the InterfaceCalendarView.
 * Prints all output to System.out and System.err.
 */
public class ConsoleView implements InterfaceCalendarView {

  @Override
  public void displayEvents(List<String> eventDetails) {
    if (eventDetails == null || eventDetails.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (String detail : eventDetails) {
      System.out.println("- " + detail);
    }
  }

  @Override
  public void displayStatus(String status) {
    System.out.println(status);
  }

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String error) {
    System.err.println("Error: " + error);
  }

  @Override
  public void displayExportConfirmation(String absolutePath) {
    System.out.println("Calendar exported successfully.");
    System.out.println("File saved to: " + absolutePath);
  }
}