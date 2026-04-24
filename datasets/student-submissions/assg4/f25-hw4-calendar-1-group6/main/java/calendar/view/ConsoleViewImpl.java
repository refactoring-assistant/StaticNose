package calendar.view;

import calendar.model.CalendarEvent;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The implementation of the ConsoleView interface.
 * Handles all output by writing directly to System.out or System.err.
 */
public final class ConsoleViewImpl implements ConsoleView {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  @Override
  public void println(String s) {
    System.out.println(s);
  }

  @Override
  public void printError(String s) {
    System.err.println("ERROR: " + s);
  }

  @Override
  public void printBulletedEvents(List<CalendarEvent> events) {
    for (CalendarEvent e : events) {
      String line = String.format("• %s: %s %s → %s %s%s",
          e.subject(),
          e.start().toLocalDate().format(DATE),
          e.start().toLocalTime().format(TIME),
          e.end().toLocalDate().format(DATE),
          e.end().toLocalTime().format(TIME),
          e.location().map(l -> " @ " + l).orElse(""));
      System.out.println(line);
    }
  }

  @Override
  public void printOneLineInterval(List<CalendarEvent> events) {
    for (CalendarEvent e : events) {
      String line = String.format("%s starting on %s at %s, ending on %s at %s%s",
          e.subject(),
          e.start().toLocalDate().format(DATE),
          e.start().toLocalTime().format(TIME),
          e.end().toLocalDate().format(DATE),
          e.end().toLocalTime().format(TIME),
          e.location().map(l -> " @ " + l).orElse(""));
      System.out.println(line);
    }
  }

  @Override
  public void printBusy(LocalDateTime when, boolean busy) {
    System.out.println(busy ? "busy" : "available");
  }

  @Override
  public void printExportPath(Path path) {
    System.out.println("Exported CSV: " + path.toString());
  }
}
