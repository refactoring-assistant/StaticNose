package calendar.view.textbased;

import calendar.view.EventViewData;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/**
 * A simple implementation of CalendarView that prints to the console.
 * Enhanced with calendar context awareness.
 */
public class CalView implements CalendarView {

  private final PrintStream out;
  private String currentCalendarContext = null;

  /**
   * Creates a view that prints to the specified stream.
   *
   * @param out The stream to print to (e.g., System.out).
   */
  public CalView(PrintStream out) {
    this.out = out;
  }

  /**
   * Sets the current calendar context for display in the prompt.
   * This can be called by commands to update the prompt.
   *
   * @param calendarName The active calendar name, or null to clear.
   */
  public void setCurrentCalendarContext(String calendarName) {
    this.currentCalendarContext = calendarName;
  }

  @Override
  public void showMessage(String message) {
    out.println(" " + message);
  }

  @Override
  public void showError(String errorMessage) {
    System.err.println("Oh no! An error occurred: " + errorMessage);
  }

  @Override
  public void showPrompt() {
    if (currentCalendarContext != null) {
      out.print("[" + currentCalendarContext + "] > ");
    } else {
      out.print("> ");
    }
  }

  @Override
  public void showEvents(List<EventViewData> events) {
    if (events.isEmpty()) {
      out.println("Your schedule is clear! No events for this day.");
      return;
    }

    out.println("Here are the events for this day:");
    for (EventViewData event : events) {
      String location =
          !Objects.equals(event.getLocation(), "") ? " at " + event.getLocation() : "";
      out.printf("%s (from %s to %s)%s%n",
          event.getSubject(),
          event.getStart().toString(),
          event.getEnd().toString(),
          location);
    }
  }

  @Override
  public void showEventSchedule(List<EventViewData> events) {
    if (events.isEmpty()) {
      out.println("No events found in that time range. Your schedule is open!");
      return;
    }

    out.println("Found the following events in that range:");
    for (EventViewData event : events) {
      out.printf(" Event: %s%n   (Starts: %s, Ends: %s)%n",
          event.getSubject(),
          event.getStart().toString(),
          event.getEnd().toString());
    }
  }

  @Override
  public void showExportResult(String path) {
    out.println("✅ Success! Your calendar was exported to:");
    out.println(path);
  }

  @Override
  public void showStatus(boolean isBusy) {
    if (isBusy) {
      out.println("🏃 You are BUSY at this time.");
    } else {
      out.println("👍 You are AVAILABLE at this time.");
    }
  }

  @Override
  public void showHelp() {

    out.println("📅 --- Calendar Command Reference --- 📅");

    out.println("--- Managing Calendars ---");
    out.println("1. Creates a new calendar with a unique name and timezone.");
    out.println("   create calendar <calendarName> --timezone <timezone>");
    out.println("   Example: create calendar Work America/New_York");
    out.println("2. Switches to a different calendar for subsequent operations.");
    out.println("   use calendar <calendarName>");
    out.println("   Example: use calendar Personal");
    out.println("3. Lists all available calendars (active calendar marked with *).");
    out.println("   list calendars");
    out.println("4. Edits a calendar's name or timezone.");
    out.println("   edit calendar --name <name> --property <prop> <value>");
    out.println("   <prop> = name | timezone");
    out.println("   Example: edit calendar timezone Work with America/Los_Angeles");

    out.println("--- Creating Events ---");
    out.println("Note: All events are created in the currently active calendar.");
    out.println("1. Creates a single event.");
    out.println("   create event <eventSubject> from <dateStringTtimeString> to "
        + "<dateStringTtimeString>");
    out.println("   Format: <dateStringTtimeString> = \"YYYY-MM-DDThh:mm\"");
    out.println("2. Creates a series repeating N times on specific weekdays.");
    out.println("   create event <eventSubject> from <start> to <end> repeats <weekdays> for <N> "
        + "times");
    out.println("   <weekdays> = sequence of M, T, W, R, F, S, U (e.g., MRU)");
    out.println("3. Creates a series repeating until a specific date (inclusive).");
    out.println("   create event <eventSubject> from <start> to <end> repeats <weekdays> until "
        + "<dateString>");
    out.println("4. Creates a single all-day event.");
    out.println("   create event <eventSubject> on <dateString>");
    out.println("Note: For all 'create' commands, subjects with multiple words");
    out.println("      must be enclosed in \"double quotes\".");

    out.println("--- Editing Events ---");
    out.println("1. Edits a single instance of an event.");
    out.println("   edit event <property> <subject> from <start> to <end> with <newValue>");
    out.println("2. Edits the event at <start> and all future instances in its series.");
    out.println("   edit events <property> <subject> from <start> with <newValue>");
    out.println("3. Edits all instances in a series (past, present, future).");
    out.println("   edit series <property> <subject> from <start> with <newValue>");
    out.println("   <property> can be one of:");
    out.println("     - subject (string)");
    out.println("     - start (dateStringTtimeString)");
    out.println("     - end (dateStringTtimeString)");
    out.println("     - description (string)");
    out.println("     - location (string)");
    out.println("     - status (string, e.g., \"private\")");

    out.println("--- Queries ---");
    out.println("1. Prints a bulleted list of all events on that day.");
    out.println("   print events on <dateString>");
    out.println("2. Prints events that (partly or fully) lie in the given interval.");
    out.println("   print events from <dateStringTtimeString> to <dateStringTtimeString>");

    out.println("--- Copying Events ---");
    out.println("Note: Copies events from the ACTIVE calendar to a target calendar.");
    out.println("1. Copies a single event to a new date/time in a target calendar.");
    out.println("   copy event <name> on <datetime> --target <calName> to <datetime>");
    out.println("   Example:"
        + " copy event \"Meeting\" on 2025-11-20T10:00 --target \"Home\" to 2025-12-01T14:00");
    out.println("2. Copies all events on one date to a new date (with timezone conversion).");
    out.println("   copy events on <date> --target <calName> to <date>");
    out.println("   Example: copy events on 2025-11-20 --target \"Home\" to 2025-12-01");
    out.println("3. Copies all events in a date range to a new timeline "
        + "(preserves series & timezone).");
    out.println("   copy events between <date> and <date> --target <calName> to <date>");
    out.println("   Example:"
        + " copy events between 2025-11-20 and 2025-11-30 --target \"Home\" to 2025-12-01");

    out.println("--- Miscellaneous ---");
    out.println("1. Exports the current calendar to a file (CSV or iCal format).");
    out.println("   export cal fileName.csv ");
    out.println("   export cal fileName.ical ");
    out.println("2. Prints if you are 'busy' or 'available' at the given time.");
    out.println("   show status on <dateStringTtimeString>");
    out.println("3. Displays this help menu.");
    out.println("   help");
    out.println("4. Exits the application.");
    out.println("   exit");
    out.println("----------------------------------------");
  }
}