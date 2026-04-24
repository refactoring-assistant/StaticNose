package calendar.views;

import calendar.models.Event;
import java.io.PrintStream;
import java.util.Set;

/**
 * Console-based implementation of {@link ObservableView} that displays messages and events to a
 * provided {@link PrintStream}.
 */
public class ConsoleView implements ObservableView {

  private static final String RESET = "\u001B[0m";
  private static final String BLUE = "\u001B[34m";
  private static final String PINK = "\u001B[95m";
  private static final String YELLOW = "\u001B[33m";
  private final PrintStream out;

  /**
   * Constructs a {@code ConsoleView} that writes to the given {@link PrintStream}.
   *
   * @param out the output stream to write console messages to
   */
  public ConsoleView(PrintStream out) {
    this.out = out;
  }

  @Override
  public void displaySuccess(String message) {
    out.println(BLUE + message + RESET);
  }

  @Override
  public void displayError(String message) {
    out.println(PINK + message + RESET);
  }

  @Override
  public void displayEvents(Set<Event> events) {
    if (events.isEmpty()) {
      out.println(YELLOW + "- No events" + RESET);
    }
    for (Event event : events) {
      out.println(YELLOW + "- " + event + RESET);
    }
  }

  @Override
  public void displayStatus(boolean isBusy) {
    if (isBusy) {
      out.println(PINK + "Status: Busy" + RESET);
      return;
    }
    out.println(BLUE + "Status: Available" + RESET);
  }
}
