package calendar.view;

import calendar.model.InEvent;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Provides real-time command-line interaction.
 * Reads from input stream and writes to output stream.
 */
public class InteractiveView implements InCalendarView {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");

  private final Scanner scanner;
  private final PrintStream out;

  /**
   * Constructs an InteractiveView with default streams.
   */
  public InteractiveView() {
    this(System.in, System.out);
  }

  /**
   * Constructs an InteractiveView with custom streams.
   *
   * @param input  the input stream
   * @param output the output stream
   */
  public InteractiveView(InputStream input, PrintStream output) {
    this.scanner = new Scanner(input);
    this.out = output;
  }

  @Override
  public void displayMessage(String message) {
    out.println(message);
  }

  @Override
  public void displayError(String error) {
    out.println("ERROR: " + error);
  }

  @Override
  public void displaySuccess(String message) {
    out.println("SUCCESS: " + message);
  }

  @Override
  public void displayEvents(List<InEvent> events) {
    if (events == null || events.isEmpty()) {
      out.println("No events found.");
      return;
    }

    for (InEvent event : events) {
      StringBuilder sb = new StringBuilder();
      sb.append("- ").append(event.getSubject());
      sb.append(" starting on ");
      sb.append(event.getStartDateTime().format(DATE_FORMAT));
      sb.append(" at ");
      sb.append(event.getStartDateTime().format(TIME_FORMAT));
      sb.append(", ending on ");
      sb.append(event.getEndDateTime().format(DATE_FORMAT));
      sb.append(" at ");
      sb.append(event.getEndDateTime().format(TIME_FORMAT));

      event.getLocation().ifPresent(loc -> sb.append(", Location: ").append(loc));

      out.println(sb.toString());
    }
  }

  /**
   * Gets the next command from the user.
   *
   * @return the command string
   */
  public String getNextCommand() {
    out.print("> ");
    if (scanner.hasNextLine()) {
      return scanner.nextLine().trim();
    }
    return "exit";
  }

  /**
   * Closes the scanner.
   */
  public void close() {
    scanner.close();
  }
}
