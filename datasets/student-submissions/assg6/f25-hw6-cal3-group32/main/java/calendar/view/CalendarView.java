package calendar.view;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Console-based view for calendar application.
 */
public class CalendarView {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
  private static final int DEFAULT_END_HOUR = 17;

  /**
   * Displays error message to stderr.
   *
   * @param msg the error message
   */
  public void displayError(String msg) {
    System.err.println("Error: " + msg);
  }

  /**
   * Displays success message to stdout.
   *
   * @param msg the success message
   */
  public void displaySuccess(String msg) {
    System.out.println(msg);
  }

  /**
   * Displays list of events.
   *
   * @param events the events to display
   */
  public void displayEvents(List<Event> events) {
    if (events == null || events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (Event e : events) {
      System.out.println(formatEvent(e));
    }
  }

  /**
   * Displays single event.
   *
   * @param e the event to display
   */
  public void displayEvent(Event e) {
    if (e == null) {
      System.out.println("No event to display.");
      return;
    }
    System.out.println(formatEvent(e));
  }

  /**
   * Displays busy or available status.
   *
   * @param busy true if busy, false if available
   */
  public void displayBusyStatus(boolean busy) {
    System.out.println(busy ? "busy" : "available");
  }

  /**
   * Displays exported file path.
   *
   * @param path the absolute file path
   */
  public void displayExportPath(String path) {
    System.out.println("Calendar exported to: " + path);
  }

  /**
   * Displays calendar information.
   *
   * @param cal the calendar to display
   */
  public void displayCalendarInfo(Calendar cal) {
    if (cal == null) {
      System.out.println("No calendar to display.");
      return;
    }
    System.out.println("Calendar: " + cal.getName());
    System.out.println("Timezone: " + cal.getTimezone().getId());
    System.out.println("Events: " + cal.getAllEvents().size());
  }

  /**
   * Displays list of calendars.
   *
   * @param cals the calendars to display
   */
  public void displayCalendars(List<Calendar> cals) {
    if (cals == null || cals.isEmpty()) {
      System.out.println("No calendars found.");
      return;
    }
    System.out.println("Available calendars:");
    for (Calendar c : cals) {
      System.out.println("  - " + c.getName() + " (Timezone: " + c.getTimezone().getId() + ")");
    }
  }

  /**
   * Displays current calendar selection.
   *
   * @param name the calendar name
   */
  public void displayCurrentCalendar(String name) {
    System.out.println("Now using calendar: " + name);
  }

  private String formatEvent(Event e) {
    StringBuilder sb = new StringBuilder("- ");
    sb.append(e.getSubject());
    sb.append(" starting on ").append(e.getStart().format(DATE_FMT));
    sb.append(" at ").append(e.getStart().format(TIME_FMT));
    sb.append(", ending on ");
    if (e.getEnd() != null) {
      sb.append(e.getEnd().format(DATE_FMT));
      sb.append(" at ").append(e.getEnd().format(TIME_FMT));
    } else {
      sb.append(e.getStart().toLocalDate().format(DATE_FMT));
      sb.append(" at ").append(String.format("%02d:00", DEFAULT_END_HOUR));
    }
    if (e.getLocation() != null && !e.getLocation().isEmpty()) {
      sb.append(" at ").append(e.getLocation());
    }
    return sb.toString();
  }
}