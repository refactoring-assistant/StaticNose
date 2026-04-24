package calendar.view;

import calendar.model.Ievent;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Console-based implementation of the calendar view.
 * This class handles all output formatting for the console.
 */
public class CalendarConsoleView implements IcalendarView {
  private final PrintStream out;
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm");

  /**
   * Constructs a console view with the given output stream.
   *
   * @param out the output stream
   */
  public CalendarConsoleView(PrintStream out) {
    if (out == null) {
      throw new IllegalArgumentException("Output stream cannot be null");
    }
    this.out = out;
  }

  /**
   * Constructs a console view with System.out.
   */
  public CalendarConsoleView() {
    this(System.out);
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
  public void displayPrompt() {
    out.print("> ");
    out.flush();
  }

  @Override
  public void displayEventsOnDate(LocalDate date, List<Ievent> events) {
    if (events.isEmpty()) {
      out.println("No events on " + date.format(DATE_FORMATTER));
      return;
    }

    out.println("Events on " + date.format(DATE_FORMATTER) + ":");
    for (Ievent event : events) {
      out.print("- " + event.getSubject());
      out.print(" from " + event.getStart().format(TIME_FORMATTER));
      out.print(" to " + event.getEnd().format(TIME_FORMATTER));

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        out.print(" at " + event.getLocation());
      }

      out.println();
    }
  }

  @Override
  public void displayEventsInRange(LocalDateTime start, LocalDateTime end,
                                   List<Ievent> events) {
    if (events.isEmpty()) {
      out.println("No events in the specified range");
      return;
    }

    out.println("Events from " + start.format(DATETIME_FORMATTER)
        + " to " + end.format(DATETIME_FORMATTER) + ":");

    for (Ievent event : events) {
      out.print("- " + event.getSubject());
      out.print(" starting on " + event.getStart().format(DATE_FORMATTER));
      out.print(" at " + event.getStart().format(TIME_FORMATTER));
      out.print(", ending on " + event.getEnd().format(DATE_FORMATTER));
      out.print(" at " + event.getEnd().format(TIME_FORMATTER));

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        out.print(" at " + event.getLocation());
      }

      out.println();
    }
  }

  @Override
  public void displayStatus(LocalDateTime dateTime, boolean busy) {
    out.print("Status on " + dateTime.format(DATETIME_FORMATTER) + ": ");
    out.println(busy ? "busy" : "available");
  }
}