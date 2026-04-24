package calendar.view;

import calendar.model.CalendarEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Console-based implementation of CalendarView.
 * Outputs to System.out and System.err.
 */
public class ConsoleView implements CalendarView {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String error) {
    System.err.println("Error: " + error);
  }

  @Override
  public void displayEvents(List<CalendarEvent> events, boolean showDateRange) {
    if (events.isEmpty()) {
      displayMessage("No events found.");
      return;
    }

    for (CalendarEvent event : events) {
      if (showDateRange) {
        displayEventWithRange(event);
      } else {
        displayEventBrief(event);
      }
    }
  }

  @Override
  public void displayStatus(boolean isBusy) {
    displayMessage(isBusy ? "busy" : "available");
  }

  @Override
  public void displayExportPath(String filePath) {
    displayMessage("Calendar exported to: " + filePath);
  }

  private void displayEventBrief(CalendarEvent event) {
    StringBuilder sb = new StringBuilder();
    sb.append("• ").append(event.getSubject());
    sb.append(" (").append(event.getStartDateTime().format(TIME_FORMATTER));
    sb.append(" - ").append(event.getEndDateTime().format(TIME_FORMATTER));
    sb.append(")");

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append(" at ").append(event.getLocation());
    }

    displayMessage(sb.toString());
  }

  private void displayEventWithRange(CalendarEvent event) {
    StringBuilder sb = new StringBuilder();
    sb.append("• ").append(event.getSubject());
    sb.append(" starting on ");
    sb.append(event.getStartDateTime().format(DATE_FORMATTER));
    sb.append(" at ");
    sb.append(event.getStartDateTime().format(TIME_FORMATTER));
    sb.append(", ending on ");
    sb.append(event.getEndDateTime().format(DATE_FORMATTER));
    sb.append(" at ");
    sb.append(event.getEndDateTime().format(TIME_FORMATTER));

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append(" at ").append(event.getLocation());
    }

    displayMessage(sb.toString());
  }
}