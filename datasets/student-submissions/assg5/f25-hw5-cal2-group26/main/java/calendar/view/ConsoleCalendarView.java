package calendar.view;

import calendar.model.EventInterface;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based implementation of CalendarViewInterface.
 * Supports interactive and headless modes.
 */
public class ConsoleCalendarView implements CalendarViewInterface {

  private final Scanner scanner;

  /**
   * Creates a new console view and sets up input reading.
   */
  public ConsoleCalendarView() {
    scanner = new Scanner(System.in);
  }

  @Override
  public void showEvents(List<EventInterface> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }

    System.out.println("Events:");
    for (EventInterface event : events) {
      String start = event.getStart().toLocalDateTime().toString();
      String end = event.getEnd() != null ? event.getEnd().toLocalDateTime().toString() : "N/A";
      String location = event.getLocation() != null ? event.getLocation() : "No location";
      System.out.printf("- %s | Start: %s | End: %s | Location: %s%n",
          event.getSubject(), start, end, location);
    }
  }

  @Override
  public void showStatus(boolean isBusy, EventInterface conflictingEvent) {
    if (isBusy) {
      System.out.println("Time slot is busy. Conflicting event: " + conflictingEvent.getSubject());
    } else {
      System.out.println("Time slot is free.");
    }
  }

  @Override
  public void showError(String message) {
    System.err.println("Error: " + message);
  }

  @Override
  public String getUserInput() {
    System.out.print("> ");
    return scanner.nextLine();
  }

  @Override
  public void showMessage(String message) {
    System.out.println(message);
  }
}
