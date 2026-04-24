package calendar.view;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View component that handles all output display for the calendar application.
 * Console-based implementation of CalendarView interface.
 */
public class CalendarViewImpl implements CalendarView {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Displays a success message.
   */
  @Override
  public void displaySuccess(String message) {
    System.out.println(message);
  }

  /**
   * Displays an error message.
   */
  @Override
  public void displayError(String message) {
    System.err.println("Error: " + message);
  }

  /**
   * Displays events on a specific date in bulleted format.
   */
  @Override
  public void displayEventsOnDate(LocalDate date, List<Event> events) {
    if (events.isEmpty()) {
      System.out.println("No events on " + date.format(DATE_FORMATTER));
      return;
    }

    System.out.println("Events on " + date.format(DATE_FORMATTER) + ":");
    for (Event event : events) {
      StringBuilder line = new StringBuilder("• ");
      line.append(event.getSubject());

      // Add time
      line.append(" from ");
      line.append(event.getStartDateTime().format(TIME_FORMATTER));
      line.append(" to ");
      if (event.getEndDateTime() != null) {
        line.append(event.getEndDateTime().format(TIME_FORMATTER));
      } else {
        line.append("(no end time)");
      }

      // Add location if present
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        line.append(" at ").append(event.getLocation());
      }

      System.out.println(line.toString());
    }
  }

  /**
   * Displays events in a date/time range.
   */
  @Override
  public void displayEventsInRange(LocalDateTime start, LocalDateTime end, List<Event> events) {
    if (events.isEmpty()) {
      System.out.println("No events between " + formatDateTime(start)
          + " and " + formatDateTime(end));
      return;
    }

    System.out.println("Events from " + formatDateTime(start)
        + " to " + formatDateTime(end) + ":");

    for (Event event : events) {
      StringBuilder line = new StringBuilder("• ");
      line.append(event.getSubject());
      line.append(" starting on ");
      line.append(event.getStartDateTime().format(DATE_FORMATTER));
      line.append(" at ");
      line.append(event.getStartDateTime().format(TIME_FORMATTER));

      if (event.getEndDateTime() != null) {
        line.append(", ending on ");
        line.append(event.getEndDateTime().format(DATE_FORMATTER));
        line.append(" at ");
        line.append(event.getEndDateTime().format(TIME_FORMATTER));
      }

      // Add location if present
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        line.append(" at ").append(event.getLocation());
      }

      System.out.println(line.toString());
    }
  }

  /**
   * Displays busy/available status.
   */
  @Override
  public void displayStatus(LocalDateTime dateTime, boolean busy) {
    String status = busy ? "busy" : "available";
    System.out.println("Status at " + formatDateTime(dateTime) + ": " + status);
  }

  /**
   * Displays a prompt for interactive mode.
   */
  @Override
  public void displayPrompt() {
    System.out.print("> ");
  }

  /**
   * Displays a welcome message.
   */
  @Override
  public void displayWelcome() {
    System.out.println("Calendar Application - Interactive Mode");
    System.out.println("Type 'EXIT' to quit");
    System.out.println();
  }

  /**
   * Formats a date/time for display.
   */
  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(DATE_FORMATTER) + "T" + dateTime.format(TIME_FORMATTER);
  }
}