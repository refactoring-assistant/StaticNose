package calendarview;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for the Calendar View in an MVC pattern.
 *
 * <p>The view is responsible for all user-facing output, such as
 * displaying messages, errors, and formatted event lists. It does not
 * contain any business logic.</p>
 */
public interface CalendarView {

  /**
   * Displays a standard informational message to the user.
   *
   * @param message The message to display.
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param errorMessage The error message to display.
   */
  void displayError(String errorMessage);

  /**
   * Displays a list of events formatted for a specific day.
   *
   * @param date            The date for which events are being displayed.
   * @param formattedEvents A list of strings, each representing one event.
   */
  void displayEventsOn(LocalDate date, List<String> formattedEvents);

  /**
   * Displays a list of events formatted for a specific time range.
   *
   * @param formattedEvents A list of strings, each representing one event.
   */
  void displayEventsFromTo(List<String> formattedEvents);

  /**
   * Displays the user's busy/free status at a specific time.
   *
   * @param isBusy true if the user has an event at that time, false otherwise.
   */
  void displayBusyStatus(boolean isBusy);

  /**
   * Displays a success message after exporting the calendar,
   * showing the location of the saved file.
   *
   * @param absolutePath The absolute file path where the calendar was saved.
   */
  void displayExportSuccess(String absolutePath);

}
