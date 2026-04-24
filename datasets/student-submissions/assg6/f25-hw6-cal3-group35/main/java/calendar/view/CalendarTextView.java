package calendar.view;

import calendar.model.Event;
import java.io.IOException;
import java.util.List;

/**
 * Console implementation of ICalendarView using an Appendable output stream.
 * Works with System.out or a StringBuilder (for tests).
 *
 * <p>This is part of the View layer in the MVC architecture.
 * It handles all presentation logic for displaying calendar events
 * and messages to the user.
 *
 * <p>Design Decision: Uses Appendable interface to allow flexible
 * output destinations (console, files, test mocks).
 *
 * @author MH
 * @version 2.0
 */
public class CalendarTextView implements IcalendarView {

  private final Appendable out;

  /**
   * Creates a new text view with the given output stream.
   *
   * @param out the Appendable output (System.out or StringBuilder)
   * @throws IllegalArgumentException if out is null
   */
  public CalendarTextView(Appendable out) {
    if (out == null) {
      throw new IllegalArgumentException("Output stream cannot be null");
    }
    this.out = out;
  }

  @Override
  public void render(List<Event> events) {
    try {
      if (events == null || events.isEmpty()) {
        out.append("No events.\n");
        return;
      }
      for (Event e : events) {
        out.append("- ").append(e.toString()).append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to render events", e);
    }
  }

  @Override
  public void renderMessage(String message) {
    try {
      out.append(message).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to render message", e);
    }
  }
}