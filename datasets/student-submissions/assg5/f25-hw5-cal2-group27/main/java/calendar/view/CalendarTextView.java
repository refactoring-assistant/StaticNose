package calendar.view;

import calendar.model.Event;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The view of our MVC responsible for all text output.
 */
public class CalendarTextView {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Prints a message to the console.
   *
   * @param message The message to print.
   */
  public void printMessage(String message) {
    System.out.println(message);
  }

  /**
   * Prints a formatted event to the console.
   *
   * @param event The event to print.
   */
  public void printEvent(Event event) {
    String startDate = event.getStart().format(DATE_FORMATTER);
    String startTime = event.getStart().format(TIME_FORMATTER);
    String endDate = event.getEnd().format(DATE_FORMATTER);
    String endTime = event.getEnd().format(TIME_FORMATTER);
    String zoneId = event.getStart().getZone().toString();
    String locationSuffix =
        (event.getLocation() != null && !event.getLocation().isBlank())
            ? ", Location: " + event.getLocation()
            : "";
    System.out.println("- " + event.getSubject() + " starting on " + startDate + " at " + startTime
        + ", ending on " + endDate + " at " + endTime + " (" + zoneId + ")" + locationSuffix);
  }

  /**
   * Prints a list of events to the console.
   *
   * @param events The list of events to print.
   */
  public void printEvents(List<Event> events) {
    if (events == null || events.isEmpty()) {
      printMessage("No events found.");
    } else {
      printMessage("Found " + events.size() + " event(s):");
      for (Event event : events) {
        printEvent(event);
      }
    }
  }

  /**
   * Prints the user's busy/available status.
   *
   * @param isBusy true if the user is busy, false otherwise.
   */
  public void printStatus(boolean isBusy) {
    if (isBusy) {
      printMessage("Status: Busy");
    } else {
      printMessage("Status: Available");
    }
  }
}
