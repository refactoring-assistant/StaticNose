package calendar.view;

import calendar.model.Event;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Text based implementation of CalendarView.
 * Displays calendar information as formatted text to an output.
 */
public class CalendarTextView implements CalendarView {
  private final Appendable output;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Creates a CalendarTextView with the specified output destination.
   *
   * @param output the destination for text output
   * @throws IllegalArgumentException if output is null
   */
  public CalendarTextView(Appendable output) {
    if (output == null) {
      throw new IllegalArgumentException("output must not be null");
    }
    this.output = output;
  }

  @Override
  public void displayMessage(String message) {
    try {
      output.append(message).append("\n");
    } catch (Exception e) {
      throw new RuntimeException("Error writing to output", e);
    }
  }

  @Override
  public void displayError(String error) {
    displayMessage("Error: " + error);
  }

  @Override
  public void displayEvents(List<Event> events) {
    if (events == null || events.isEmpty()) {
      displayMessage("No events found");
      return;
    }
    for (Event event : events) {
      displayMessage(formatEvent(event));
    }
  }

  private String formatEvent(Event event) {
    StringBuilder builder = new StringBuilder();
    builder.append(event.getSubject())
        .append(" starting on ")
        .append(event.getStartDateTime().format(DATE_FORMATTER))
        .append(" at ")
        .append(event.getStartDateTime().format(TIME_FORMATTER))
        .append(" ending on ")
        .append(event.getEndDateTime().format(DATE_FORMATTER))
        .append(" at ")
        .append(event.getEndDateTime().format(TIME_FORMATTER));

    String location = event.getLocation();
    String description = event.getDescription();
    String status = event.getStatus().toString();

    if (location != null && !location.isEmpty()) {
      builder.append(", Location: ").append(location);
    }
    if (status != null) {
      builder.append(", Status: ").append(status);
    }
    if (description != null && !description.isEmpty()) {
      builder.append(", Description: ").append(description);
    }

    return builder.toString();
  }

  @Override
  public void displayStatus(boolean isBusy) {
    displayMessage(isBusy ? "Busy" : "Available");
  }
}
