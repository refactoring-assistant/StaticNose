package calendar.view;

import calendar.model.CalendarEvent;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for displaying information to the user.
 * It handles all the output,  printing event lists, error messages, and success confirmations.
 */
public interface ConsoleView {
  /**
   * Prints a standard message followed by a newline.
   *
   * @param s The string message to display.
   */
  void println(String s);

  /**
   * Prints an error message.
   * Used whenever a command fails or an invalid argument is given.
   *
   * @param s The error message string to display.
   */
  void printError(String s);

  /**
   * Prints a list of events in a simple, bulleted list format.
   *
   * @param events List of CalendarEvent objects to format and print.
   */
  void printBulletedEvents(List<CalendarEvent> events);

  /**
   * Prints a list of events where each event is formatted into a single, detailed line.
   *
   * @param events List of CalendarEvent objects to format and print.
   */
  void printOneLineInterval(List<CalendarEvent> events);

  /**
   * Prints the user's status at a specific date and time.
   * Should print "busy" or "available".
   *
   * @param when The date and time that was checked.
   * @param busy True if the user is busy, false otherwise.
   */
  void printBusy(LocalDateTime when, boolean busy);

  /**
   * Prints the file path where the calendar was successfully exported.
   *
   * @param path The absolute path to the generated CSV file.
   */
  void printExportPath(Path path);
}
