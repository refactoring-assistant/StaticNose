package calendar.view.gui;

import calendar.controller.gui.GuiFeatures;
import calendar.model.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for the GUI view of the calendar application.
 */
public interface GuiView {
  /**
   * Set the controller features (callbacks) so the view can call into the controller.
   */
  void setFeatures(GuiFeatures features);

  /**
   * Update the UI to show the given month/year and highlight days that have events.
   * monthStart is any LocalDate inside the month to show.
   */
  void showMonth(LocalDate monthStart);

  /**
   * Show events for a particular day (in the calendar's timezone).
   *
   * @param day the day to show events for
   * @param eventSummaries list of event summary strings
   * @param isSeriesMap map from summary to boolean indicating if event is part of a series
   */
  void showEventsForDay(LocalDate day, List<String> eventSummaries,
                        Map<String, Boolean> isSeriesMap);

  /**
   * Set the list of calendar names (for the calendar chooser dropdown).
   */
  void setCalendarNames(java.util.List<String> calendars);

  /**
   * Show an error message to the user (friendly).
   */
  void showError(String message);

  /**
   * Refresh/redraw UI after model changes.
   */
  void refresh();

  /**
   * Highlight days that have events.
   *
   * @param daysWithEvents list of days (day-of-month) that have events
   */
  void highlightDaysWithEvents(java.util.Set<Integer> daysWithEvents);

  /**
   * Update the display to show the current calendar name.
   *
   * @param calendarName the name of the currently active calendar
   */
  void setCurrentCalendarName(String calendarName);

  /**
   * Show a dialog to create a new event starting on the given date.
   * Returns the event data if user confirms, empty if cancelled.
   *
   * @param date the date for the new event
   * @return Optional containing EventData if confirmed, empty if cancelled
   */
  Optional<EventData> showCreateEventDialog(LocalDate date);

  /**
   * Show a dialog to edit an existing event.
   * Returns the modified event data if user confirms, empty if cancelled.
   *
   * @param existing the event to edit
   * @return Optional containing EventData if confirmed, empty if cancelled
   */
  Optional<EventData> showEditEventDialog(Event existing);
}

