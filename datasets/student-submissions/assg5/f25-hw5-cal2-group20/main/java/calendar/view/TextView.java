package calendar.view;

import java.io.IOException;

/**
 * Represents a simple text view of the Calendar. Provides with useful messages for users,
 * shows the list of events and user status.
 */
public class TextView implements CalendarView {
  private final Appendable out;

  /**
   * Constructs a simple calendar view, given an appendable.
   *
   * @param out to append messages to.
   */
  public TextView(Appendable out) {
    if (out == null) {
      throw new IllegalArgumentException("View appendable cannot be null.");
    }
    this.out = out;
  }

  @Override
  public void printEvents(String events) {
    String[] eventsArray = events.split(System.lineSeparator());
    StringBuilder s = new StringBuilder();
    if  (events.isEmpty()) {
      s.append("No events to print.").append(System.lineSeparator());
    } else {
      for (String event : eventsArray) {
        s.append("- ").append(event).append(System.lineSeparator());
      }
    }
    write(s.toString());
  }

  private void write(String message) {
    try {
      out.append(message);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot write to output." + System.lineSeparator(), e);
    }
  }

  @Override
  public void renderMessage(String message) {
    write(message + System.lineSeparator());
  }

  @Override
  public void showUserStatus(String status) {
    write("The user status: " + status + System.lineSeparator());
  }
}
