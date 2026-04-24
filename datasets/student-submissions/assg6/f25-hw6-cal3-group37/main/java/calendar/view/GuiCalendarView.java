package calendar.view;

import calendar.model.CalendarEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Interface for GUI-based calendar view operations.
 * Extends base CalendarView with GUI-specific methods.
 */
public interface GuiCalendarView extends CalendarView {

  /**
   * Makes the GUI visible to the user.
   */
  void display();

  /**
   * Updates the month view with events for the given month.
   *
   * @param yearMonth the year and month to display
   * @param events the events to show
   * @param calendarName the current calendar name
   */
  void updateMonthView(YearMonth yearMonth, List<CalendarEvent> events, String calendarName);

  /**
   * Updates the events list for a selected day.
   *
   * @param date the selected date
   * @param events the events on that date
   */
  void updateDayEvents(LocalDate date, List<CalendarEvent> events);

  /**
   * Shows an error dialog to the user.
   *
   * @param title the dialog title
   * @param message the error message
   */
  void showErrorDialog(String title, String message);

  /**
   * Shows an information dialog to the user.
   *
   * @param title the dialog title
   * @param message the information message
   */
  void showInfoDialog(String title, String message);

  /**
   * Refreshes the entire view.
   */
  void refresh();
}