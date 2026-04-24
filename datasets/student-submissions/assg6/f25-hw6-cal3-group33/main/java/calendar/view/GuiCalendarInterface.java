package calendar.view;

import calendar.controller.CalendarFeatures;
import calendar.model.event.EventInterface;
import java.awt.Color;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Interface for GUI-based calendar view operations.
 * Defines methods for displaying calendar information and updating the visual interface.
 * This interface decouples the GUI implementation from the controller.
 */
public interface GuiCalendarInterface extends CalendarView {

  /**
   * Sets the features callback handler that responds to user interactions.
   * The view will call methods on this features object when users interact with the GUI.
   *
   * @param features the features handler (typically the controller)
   */
  void setFeatures(CalendarFeatures features);

  /**
   * Makes the view visible or invisible.
   *
   * @param visible true to show the view, false to hide it
   */
  void setVisible(boolean visible);

  /**
   * Displays events for a specific day in a dialog.
   * Converts model events to display format and shows them to the user.
   *
   * @param date the date to display events for
   * @param events list of events from the model for this date
   */
  void showDayEventsFromModel(LocalDate date, List<EventInterface> events);

  /**
   * Updates the event indicators (dots/badges) on the calendar grid.
   * Shows visual indicators for days that have events scheduled.
   *
   * @param eventsPerDay map of dates to number of events on that day
   * @param calendarColor the color to use for event indicators
   */
  void updateEventIndicators(Map<LocalDate, Integer> eventsPerDay, Color calendarColor);

  /**
   * Updates the list of available calendars in the left panel.
   * Rebuilds the calendar selector with current calendars.
   *
   * @param calendarNames list of all calendar names
   * @param selectedCalendarName the currently selected calendar name
   */
  void updateCalendarList(List<String> calendarNames, String selectedCalendarName);

  /**
   * Displays a success message after calendar export.
   * Shows the file path where the calendar was saved.
   *
   * @param filePath the absolute path to the exported file
   */
  void showExportSuccess(String filePath);

  /**
   * Gets the currently displayed month in the calendar view.
   *
   * @return the current YearMonth being displayed
   */
  YearMonth getCurrentMonth();

  /**
   * Gets the color associated with a specific calendar.
   *
   * @param calendarName the name of the calendar
   * @return the color for this calendar, or default blue if not found
   */
  Color getCalendarColor(String calendarName);
}