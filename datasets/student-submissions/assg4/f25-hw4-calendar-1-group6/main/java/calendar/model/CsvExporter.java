package calendar.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Writes a Google Calendar-compatible CSV.
 */
public final class CsvExporter {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  private CsvExporter() {
  }

  /**
   * Writes a collection of calendar events to a CSV file compatible with Google Calendar import.
   */
  public static void writeGoogleCsv(Collection<CalendarEvent> events, Path csv) throws IOException {
    try (BufferedWriter w = Files.newBufferedWriter(csv)) {
      w.write(
          "Subject, Start Date, Start Time, End Date, End Time,"
              + "All Day Event, Description, Location, Private");
      w.newLine();
      for (CalendarEvent e : events) {
        String subject = quote(e.subject());
        String sd = e.start().toLocalDate().format(DATE);
        String st = e.start().toLocalTime().format(TIME);
        String ed = e.end().toLocalDate().format(DATE);
        String et = e.end().toLocalTime().format(TIME);
        String allDay = e.isAllDay() ? "True" : "False";
        String desc = quote(e.description().orElse(""));
        String loc = quote(e.location().orElse(""));
        String priv =
            e.status().map(s -> s.equalsIgnoreCase("private") ? "True" : "False").orElse("False");


        w.write(String.join(",", subject, sd, st, ed, et, allDay, desc, loc, priv));
        w.newLine();
      }
    }
  }

  private static String quote(String s) {
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }
}
