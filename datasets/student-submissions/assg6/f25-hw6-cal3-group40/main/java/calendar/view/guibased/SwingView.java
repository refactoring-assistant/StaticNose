package calendar.view.guibased;

import calendar.controller.UiFeatures;
import calendar.model.CalendarInterface;
import calendar.view.EventViewData;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for the Swing-based Graphical User Interface view.
 */
public interface SwingView {

  /**
   * Refreshes the view to reflect the current state of the model.
   */
  void refresh();

  /**
   * Displays an error message to the user.
   *
   * @param message the error message
   */
  void showError(String message);

  /**
   * Sets the features (controller callbacks) for the view.
   *
   * @param features the features implementation
   */
  void addFeatures(UiFeatures features);

  /**
   * Makes the view visible.
   */
  void display();

  /**
   * Updates the list of available calendars in the view.
   *
   * @param calendars the list of calendars
   */
  void updateCalendarList(List<CalendarInterface> calendars);

  /**
   * Updates the view with the events for a specific day.
   *
   * @param date   the date
   * @param events the list of events on that date
   */
  void showEventsForDay(LocalDate date, List<EventViewData> events);

  /**
   * Sets the current calendar name in the view.
   *
   * @param name the name of the current calendar
   */
  void setCurrentCalendar(String name);

  /**
   * Sets the current timezone for display.
   *
   * @param zone the timezone
   */
  void setTimezone(java.time.ZoneId zone);
}
