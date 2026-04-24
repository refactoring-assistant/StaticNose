package calendar.view;

import calendar.exceptions.InvalidDateTimeException;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.event.EventInterface;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Console implementation of CalendarView.
 * Displays all output to terminal (out and err).
 */
public class ConsoleView implements CalendarView {

  private final PrintStream out;
  private final PrintStream err;

  /**
   * Creates a ConsoleView with custom output streams.
   * Useful for testing or redirecting output.
   *
   * @param out the output stream for normal messages
   * @param err the output stream for error messages
   */
  public ConsoleView(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  /**
   * Creates a ConsoleView using standard console streams.
   */
  public ConsoleView() {
    this(System.out, System.err);
  }

  @Override
  public void displayWelcome() {
    out.println("Starting Calendar: (Interactive Mode)");
  }

  @Override
  public void displayGoodbye() {
    out.println("Exiting Calendar");
  }

  @Override
  public void displayPrompt() {
    out.print("> ");
    out.flush();
  }

  @Override
  public void displayCommandOptions() {
    out.println("\n=== Available Commands ===\n");

    out.println("Calendar Management:");
    out.println("------------------------------------------------------------------------");
    out.println("  create calendar --name <calendarName> --timezone <timezone>");
    out.println("  edit calendar --name <calendarName> --property <name|timezone> <newValue>");
    out.println("  use calendar --name <calendarName>");
    out.println("  Note: Use quotes for multi-word calendar names (e.g., \"Work Calendar\")");
    out.println("  Timezone examples: America/New_York, Europe/London, Asia/Tokyo\n");

    out.println("Creating Events:");
    out.println("------------------------------------------------------------------------");
    out.println("  create event <eventSubject> from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm>");
    out.println("  create event <eventSubject> from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm> "
        + "repeats <weekdays> for <N> times");
    out.println("  create event <eventSubject> from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm> "
        + "repeats <weekdays> until <YYYY-MM-DD>");
    out.println("  create event <eventSubject> on <YYYY-MM-DD>");
    out.println("  create event <eventSubject> on <YYYY-MM-DD> repeats <weekdays> for "
        + "<N> times");
    out.println("  create event <eventSubject> on <YYYY-MM-DD> repeats <weekdays> "
        + "until <YYYY-MM-DD>");
    out.println("  Note: Use quotes for multi-word subjects. Weekdays: M,T,W,R,F,S,U\n");

    out.println("Editing Events:");
    out.println("------------------------------------------------------------------------");
    out.println("  edit event <property> <eventSubject> from <YYYY-MM-DDThh:mm> "
        + "to <YYYY-MM-DDThh:mm> with <newValue>");
    out.println("  edit events <property> <eventSubject> from <YYYY-MM-DDThh:mm> "
        + "with <newValue>");
    out.println("  edit series <property> <eventSubject> from <YYYY-MM-DDThh:mm> "
        + "with <newValue>");
    out.println("  Properties: subject, start, end, description, location, status\n");

    out.println("Copying Events:");
    out.println("------------------------------------------------------------------------");
    out.println("  copy event <eventSubject> on <YYYY-MM-DDThh:mm> --target <calendarName> "
        + "to <YYYY-MM-DDThh:mm>");
    out.println("  copy events on <YYYY-MM-DD> --target <calendarName> to <YYYY-MM-DD>");
    out.println("  copy events between <YYYY-MM-DD> and <YYYY-MM-DD> --target <calendarName> "
        + "to <YYYY-MM-DD>");
    out.println("  Note: Copies from current calendar to target calendar\n");

    out.println("Queries:");
    out.println("------------------------------------------------------------------------");
    out.println("  print events on <YYYY-MM-DD>");
    out.println("  print events from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm>");
    out.println("  show status on <YYYY-MM-DDThh:mm>\n");

    out.println("Export:");
    out.println("------------------------------------------------------------------------");
    out.println("  export cal <fileName.Csv/.ics>");
    out.println("  Supported formats: .Csv (Google Calendar), .ics (iCalendar)\n");

    out.println("Other:");
    out.println("------------------------------------------------------------------------");
    out.println("  help  - Display this command list");
    out.println("  exit  - Exit the program\n");
  }

  @Override
  public void displayMessage(String message) {
    out.println(message);
  }

  @Override
  public void displayError(String message) {
    err.println("Error: " + message);
  }

  @Override
  public void displayFileNotFound(String filePath) {
    err.println("Error: File not found: " + filePath);
  }

  @Override
  public void displayFileReadError(String message) {
    err.println("Error: Error reading input: " + message);
  }

  @Override
  public void displayNoExitCommand() {
    err.println("Error: File ended without exit command.");
  }

  @Override
  public void close() {
    // Nothing to close for console output
  }

  @Override
  public void displayEvents(ReadOnlyCalendar calendar, String date)
      throws InvalidDateTimeException {

    List<EventInterface> events = calendar.getEvents(date);
    out.println("\n=== Calendar: " + calendar.getCalendarName() + " ("
        + calendar.getCalendarTimeZone() + ") ===");

    if (events.isEmpty()) {
      out.println("No events on " + date);
      return;
    }
    out.println("Events on " + date + ":");
    for (EventInterface event : events) {
      out.println("- " + event.getSubject()
          + " from " + formatTime(event.getStartDateTime())
          + " to " + formatTime(event.getEndDateTime()));

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        out.println("  Location: " + event.getLocation());
      }
    }
  }

  @Override
  public void displayEventsInRange(ReadOnlyCalendar calendar, String startDate, String endDate)
      throws InvalidDateTimeException {
    List<EventInterface> events = calendar.getEvents(startDate, endDate);
    out.println("\n=== Calendar: " + calendar.getCalendarName() + " ("
        + calendar.getCalendarTimeZone() + ") ===");

    if (events.isEmpty()) {
      out.println("No events from " + startDate + " to " + endDate);
      return;
    }

    out.println("Events from " + startDate + " to " + endDate + ":");
    for (EventInterface event : events) {
      out.print("• " + event.getSubject()
          + " starting on " + formatDate(event.getStartDateTime())
          + " at " + formatTime(event.getStartDateTime())
          + ", ending on " + formatDate(event.getEndDateTime())
          + " at " + formatTime(event.getEndDateTime()));

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        out.print(", Location: " + event.getLocation());
      }

      out.println();
    }
  }

  /**
   * Displays whether the calendar is busy or available at a specific date and time.
   *
   * @param calendar true if there are events at the specified time, false if available
   * @param dateTime the date and time being queried (formatted string)
   */
  public void displayBusyStatus(ReadOnlyCalendar calendar, String dateTime)
      throws InvalidDateTimeException {
    boolean isBusy = calendar.busyStatus(dateTime);
    String status = isBusy ? "busy" : "available";
    out.println("Status on " + dateTime + ": " + status);
  }


  private String formatTime(ZonedDateTime dt) {
    return dt.toLocalTime().toString();
  }

  private String formatDate(ZonedDateTime dateTime) {
    return dateTime.toLocalDate().toString();
  }

  @Override
  public void displayFatalError(String message) {
    err.println("Fatal Error: " + message);
  }

  @Override
  public void displayUsageInformation() {
    err.println("\nUsage:");
    err.println("  Interactive mode: java -jar build/libs/JARNAME.jar --mode interactive");
    err.println("  Headless mode:    java -jar build/libs/JARNAME.jar --mode headless <file_path>");
    err.println("\nModes are case-insensitive.");
  }
}
