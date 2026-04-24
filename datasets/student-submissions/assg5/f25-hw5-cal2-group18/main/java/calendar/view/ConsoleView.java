package calendar.view;

import calendar.model.Event;
import java.util.List;

/**
 * Console-based implementation of the CalendarView interface.
 */
public class ConsoleView implements CalendarView {

  @Override
  public void displayMessage(String message) {
    if (message != null) {
      System.out.println(message);
    }
  }

  @Override
  public void displayError(String error) {
    if (error != null) {
      System.err.println("Error: " + error);
    }
  }

  @Override
  public void displayEvents(List<Event> events) {
    if (events == null || events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (Event event : events) {
      System.out.println(formatEvent(event));
    }
  }

  @Override
  public void displayWelcome() {
    System.out.println("Welcome to the Calendar App!");
    System.out.println("Type a command or 'exit' to quit.\n");
  }

  @Override
  public void displayGoodbye() {
    System.out.println("Goodbye!");
  }

  @Override
  public void displayPrompt(String prompt) {
    if (prompt != null && !prompt.isEmpty()) {
      System.out.print(prompt);
    }
  }

  @Override
  public void displayResult(String result) {
    if (result != null) {
      System.out.println(result);
    }
  }

  /**
   * Helper method to format one event for output.
   *
   * @param event the event to format
   * @return formatted string representation of the event
   */

  private String formatEvent(Event event) {
    return String.format(
            "%s | %s → %s | %s | %s | %s",
            event.getSubject(),
            event.getStartDateTime(),
            event.getEndDateTime(),
            event.getLocation(),
            event.getDescription() != null ? event.getDescription() : "",
            event.getStatus()
    );
  }
}