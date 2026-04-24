package calendar.view;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of CalendarView interface for displaying calendar information
 * and user interactions.
 */
public class CalendarViewImpl implements CalendarView {

  /**
   * Displays a success message to the user.
   *
   * @param message the success message to display
   */
  @Override
  public void displaySuccess(String message) {
    System.out.println(message);
  }

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  @Override
  public void displayError(String message) {
    System.err.println("Error: " + message);
  }

  /**
   * Displays a prompt to the user.
   */
  @Override
  public void displayPrompt() {
    System.out.print("> ");
  }

  /**
   * Displays a general message to the user.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays events on a specific date.
   *
   * @param date   the date for which to display events
   * @param events the list of events to display
   */
  public void displayEventsOnDate(LocalDate date, List<Event> events) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    System.out.println("Events on " + date.format(dateFormatter) + ":");

    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }

    for (Event event : events) {
      System.out.println(formatEventForDisplay(event));
    }
  }

  /**
   * Displays events within a specified time range.
   *
   * @param startDateTime the start of the time range
   * @param endDateTime   the end of the time range
   * @param events        the list of events to display
   */
  public void displayEventsInRange(LocalDateTime startDateTime, LocalDateTime endDateTime,
                                   List<Event> events) {
    System.out.println("Events from " + startDateTime + " to " + endDateTime + ":");

    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }

    for (Event event : events) {
      System.out.println(formatEventForDisplay(event));
    }
  }

  /**
   * Formats an event for display.
   *
   * @param event the event to format
   * @return the formatted event string
   */
  private String formatEventForDisplay(Event event) {
    StringBuilder sb = new StringBuilder();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    sb.append("- ").append(event.getSubject());
    if (event.isAllDay()) {
      sb.append(" (All day)");
    } else {
      sb.append(" from ").append(event.getStartDateTime().format(timeFormatter))
          .append(" to ").append(event.getEndDateTime().format(timeFormatter));
    }

    if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
      sb.append(" at ").append(event.getLocation());
    }

    if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
      sb.append(" - ").append(event.getDescription());
    }

    if (event.getSeriesId() != null) {
      sb.append(" (Part of series)");
    }

    return sb.toString();
  }

  /**
   * Displays the availability status at a specific time.
   *
   * @param dateTime the time to check
   * @param isBusy   true if busy, false if available
   */
  public void displayAvailabilityStatus(LocalDateTime dateTime, boolean isBusy) {
    String status = isBusy ? "busy" : "available";
    System.out.println(status);
  }

  /**
   * Displays a message about successful calendar export.
   *
   * @param filename the name of the exported file
   */
  public void displayExportSuccess(String filename) {
    System.out.println("Calendar exported to " + filename);
  }
}