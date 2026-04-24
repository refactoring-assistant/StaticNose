package calendar.view;

import calendar.model.Event;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Text-based implementation of CalendarView.
 * Displays output to console and reads input from console.
 */
public class MyTextView implements MyCalendarView {
  private final Scanner scanner;
  private final PrintStream output;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Creates a new TextView with specified input/output streams.
   *
   * @param input the input stream (usually System.in)
   * @param output the output stream (usually System.out)
   */
  public MyTextView(InputStream input, PrintStream output) {
    this.scanner = new Scanner(input);
    this.output = output;
  }

  /**
   * Creates a new TextView using standard input/output.
   */
  public MyTextView() {
    this(System.in, System.out);
  }

  @Override
  public void displayEvents(List<Event> events) {
    if (events.isEmpty()) {
      output.println("No events found");
      return;
    }

    for (Event event : events) {
      output.println("- " + formatEvent(event));
    }
  }

  @Override
  public void displayMessage(String message) {
    output.print(message);
    if (!message.endsWith("\n")) {
      if (!message.endsWith("> ") && !message.isEmpty()) {
        output.println();
      }
    }
  }

  @Override
  public void displayError(String error) {
    output.println("ERROR: " + error);
  }

  @Override
  public String readCommand() {
    if (scanner.hasNextLine()) {
      return scanner.nextLine();
    }
    return null;
  }

  private String formatEvent(Event event) {
    StringBuilder sb = new StringBuilder();
    sb.append(event.getSubject());

    String startDate = event.getStart().format(DATE_FORMATTER);
    String startTime = event.getStart().format(TIME_FORMATTER);
    String endDate = event.getEnd().format(DATE_FORMATTER);
    String endTime = event.getEnd().format(TIME_FORMATTER);

    if (event.isAllDay()) {
      sb.append(" on ").append(startDate);
      sb.append(" (All Day)");
    } else {
      sb.append(" starting on ").append(startDate);
      sb.append(" at ").append(startTime);

      if (!startDate.equals(endDate)) {
        sb.append(", ending on ").append(endDate);
        sb.append(" at ").append(endTime);
      } else {
        sb.append(", ending at ").append(endTime);
      }
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append(" at ").append(event.getLocation());
    }

    return sb.toString();
  }
}