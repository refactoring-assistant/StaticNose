package calendar.controller.exporter;

import calendar.model.event.CalendarEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * An abstract exporter which will be used to export the calendar in different formats.
 */
public abstract class AbstractExportCalendar implements ExporterInterface {
  protected final String filename;

  /**
   * Initializes the filename to export in the correct format.
   *
   * @param filename path of the file where the calendar is exported to including file name
   */
  protected AbstractExportCalendar(String filename) {
    this.filename = filename;
  }

  @Override
  public abstract Boolean exportHelper(List<CalendarEvent> eventList) throws RuntimeException;

  /**
   * Sends back either a "null" string if value does not exist or the existing string value.
   *
   * @param s the string to check
   * @return string value of s
   */
  protected static String nullToString(String s) {
    if (s == null) {
      return "null";
    }
    return s;
  }
}
