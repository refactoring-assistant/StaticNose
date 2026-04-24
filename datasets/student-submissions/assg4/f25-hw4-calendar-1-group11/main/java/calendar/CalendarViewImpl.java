package calendar;

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
   *  @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays a list of events for a specific date.
   *
   * @param date the date to display events for
   *
   * @param events the list of events on that date
   */
  public void displayEventsOnDate(LocalDate date, List<Event> events) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    System.out.println("Events on " + date.format(dateFormatter) + ":");

    if (events.isEmpty()) {
      System.out.println("No events found");
      return;
    }

    for (Event event : events) {
      displayEvent(event);
    }
  }

  /**
   * Displays a list of events within a time range.
   *
   * @param startDateTime the start of the time range
   *
   * @param endDateTime the end of the time range
   *
   * @param events the list of events in that range
   */
  public void displayEventsInRange(LocalDateTime startDateTime,
                                   LocalDateTime endDateTime, List<Event> events) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    System.out.println("Events from " + startDateTime.format(formatter)
        +
        " to " + endDateTime.format(formatter) + ":");

    if (events.isEmpty()) {
      System.out.println("No events found");
      return;
    }

    for (Event event : events) {
      displayEvent(event);
    }
  }

  /**
   * Displays the availability status at a specific time.
   *
   * @param dateTime the time to check
   *
   * @param isBusy true if busy, false if available
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

  /**
   * Displays an individual event with proper formatting.
   *
   * @param event the event to display
   */
  private void displayEvent(Event event) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    StringBuilder sb = new StringBuilder();
    sb.append("- ").append(event.getSubject());


    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();

    if (startDate.equals(endDate)
        &&
        event.getStartDateTime().toLocalTime().equals(java.time.LocalTime.MIDNIGHT)) {
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

    System.out.println(sb.toString());
  }
}