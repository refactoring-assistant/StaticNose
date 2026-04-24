package calendar.view;

import calendar.model.InterfaceEvent;
import java.io.IOException;
import java.util.List;

/**
 * A terminal-based implementation of the {@link View} interface.
 * Handles displaying messages, errors, event lists, and command prompts to the user via console.
 */
public class TerminalView implements View {

  private final Appendable out;

  /**
   * This is the Terminal View constructor.
   *
   * @param out is the Appendable to specify when calling/creating a TerminalView object.
   */
  public TerminalView(Appendable out) {
    this.out = out;
  }

  /**
   * Displays a standard message to the user.
   *
   * @param message the message to display
   */
  @Override
  public void showMessage(String message) {
    // This try/catch is REQUIRED by the Java compiler
    // because this.out.append() can throw an IOException.
    try {
      this.out.append(message).append("\n");
      this.out.append("-----------------------").append("\n");
    } catch (IOException e) {
      // If we can't write, the app is fundamentally broken,
      // so we throw an unchecked exception.
      throw new IllegalStateException("Could not write to view output.", e);
    }
  }

  /**
   * Displays an error message to the user.
   *
   * @param errorMessage the error message to display
   */
  @Override
  public void showError(String errorMessage) {
    try {
      this.out.append("Error: ").append(errorMessage).append("\n");
      this.out.append("-----------------------").append("\n");
    } catch (IOException e) {
      throw new IllegalStateException("Could not write to view output.", e);
    }
  }

  /**
   * Displays a list of events to the user in a readable format.
   * If the list is empty, prints a "No events found" message.
   *
   * @param events the list of events to display
   */
  @Override
  public void displayEvents(List<InterfaceEvent> events) {
    try {
      if (events.isEmpty()) {
        this.out.append("No events found.").append("\n");
        return;
      }
      this.out.append("Events:").append("\n");
      this.out.append("-----------------------").append("\n");
      for (InterfaceEvent e : events) {
        String location = (e.getLocation().equals("null")) ? "N/A" : e.getLocation();
        String status = (e.getStatus().equals("null")) ? "N/A" : e.getStatus();
        String description = (e.getDescription().equals("null")) ? "N/A" : e.getDescription();

        this.out.append(String.format(
            "- %s starting on %s at %s, ending on %s at %s, location: %s, status: %s, "
                + "description: %s%n", e.getSubject(), e.getStartDate(), e.getStartTime(),
            e.getEndDate(), e.getEndTime(), location, status, description));
      }
      this.out.append("\n");
    } catch (IOException e) {
      throw new IllegalStateException("Could not write to view output.", e);
    }
  }

  /**
   * Prompts the user to enter a command via the terminal.
   */
  @Override
  public void promptCommand() {
    try {
      this.out.append("Enter your command:").append("\n");
      this.out.append("-----------------------").append("\n");
    } catch (IOException e) {
      throw new IllegalStateException("Could not write to view output.", e);
    }
  }
}
