package calendar.util;

import calendar.event.Event;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A utility class responsible for converting a list of event objects
 * into a CSV formatted string, which is suitable for exporting and viewing.
 */
public class CsvExporter {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Converts a list of events into a complete CSV formatted string.
   *
   * @param events the list of event objects to export
   * @return a String representing the full CSV content.
   */
  public static String exportToCsv(List<Event> events) {
    StringBuilder csv = new StringBuilder();
    csv.append("Subject,Start Date,Start Time,End Date,End Time,");
    csv.append("All Day Event,Description,Location,Private\n");
    for (Event event : events) {
      csv.append(formatEventAsCsv(event));
    }
    return csv.toString();
  }

  /**
   * Formats a single event object into a single line of CSV.
   *
   * @param event the event object to format
   * @return a CSV-formatted string line for the event
   */
  private static String formatEventAsCsv(Event event) {
    StringBuilder line = new StringBuilder();
    // Subject
    line.append(escapeCsv(event.getSubject())).append(",");
    // Start Date
    line.append(event.getStartDateTime().format(DATE_FORMATTER)).append(",");
    // Start Time
    if (event.isAllDayEvent()) {
      line.append(",");
    } else {
      line.append(event.getStartDateTime().format(TIME_FORMATTER)).append(",");
    }
    // End Date
    if (event.getEndDateTime() != null) {
      line.append(event.getEndDateTime().format(DATE_FORMATTER)).append(",");
    } else {
      line.append(",");
    }
    // End Time
    if (event.isAllDayEvent() || event.getEndDateTime() == null) {
      line.append(",");
    } else {
      line.append(event.getEndDateTime().format(TIME_FORMATTER)).append(",");
    }
    // All Day Event
    line.append(event.isAllDayEvent() ? "True" : "False").append(",");
    // Description
    line.append(escapeCsv(event.getDescription())).append(",");
    // Location
    line.append(escapeCsv(event.getLocation())).append(",");
    // Private
    line.append(event.getEventStatus().toString().equalsIgnoreCase("PRIVATE") ? "True" : "False");
    line.append("\n");
    return line.toString();
  }

  /**
   * Escapes a string value according to standard CSV rules.
   * Enclose the value contains a comma, double quote, or newline in double quotes.
   * Any existing double quotes within the value are doubled.
   *
   * @param value the string value to escape
   * @return the escaped string
   */
  private static String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}

