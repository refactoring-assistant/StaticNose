package calendar.view;

import calendar.model.event.Ievent;
import java.util.List;

/**
 * Text-based view implementation that displays to console.
 * Used for both interactive and headless modes.
 */
public class TextView implements IcalendarView {

  /**
   * Display a single event with full details.
   *
   * @param event the event to display
   */
  @Override
  public void displayEvent(Ievent event) {
    System.out.println(EventFormatter.formatEventForRange(event));
  }

  /**
   * Display a list of events with full details.
   *
   * @param events the list of events to display
   */
  @Override
  public void displayEvents(List<Ievent> events) {
    if (events.isEmpty()) {
      displayMessage("No events found.");
      return;
    }

    for (Ievent event : events) {
      displayEvent(event);
    }
  }

  /**
   * Display events on a specific date with time-only format.
   *
   * @param events the list of events to display
   */
  @Override
  public void displayEventsOnDate(List<Ievent> events) {
    if (events.isEmpty()) {
      displayMessage("No events found.");
      return;
    }

    for (Ievent event : events) {
      System.out.println(EventFormatter.formatEventForDate(event));
    }
  }

  /**
   * Display a general message to the user.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Display an error message to the user.
   *
   * @param error the error message to display
   */
  @Override
  public void displayError(String error) {
    System.err.println("Error: " + error);
  }

  /**
   * Display the busy status of the user.
   *
   * @param isBusy true if busy, false if available
   */
  @Override
  public void displayStatus(boolean isBusy) {
    System.out.println(isBusy ? "busy" : "available");
  }

  /**
   * Display the file path where the calendar was exported.
   *
   * @param filePath the path to the exported file
   */
  @Override
  public void displayExportPath(String filePath) {
    System.out.println("Calendar exported to: " + filePath);
  }
}