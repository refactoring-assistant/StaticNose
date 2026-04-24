package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface for Calendar GUI View extending the base CalendarView.
 */
public interface CalendarGuiView extends CalendarView {
  /**
   * Displays the Gui window.
   * This method should be called to show the calendar Gui.
   */
  void display();

  /**
   * Updates the calendar list in the view.
   *
   * @param calendarNames      list of all calendar names
   * @param activeCalendarName the currently active calendar name
   */
  void setCalendars(List<String> calendarNames, String activeCalendarName);

  /**
   * Updates the month view with events.
   *
   * @param month  the month being displayed
   * @param events map of date to list of events for that date
   */
  void setMonthEvents(LocalDate month, Map<LocalDate, List<ViewEvent>> events);

  /**
   * Updates the day view with events.
   *
   * @param date   the date being displayed
   * @param events list of events for that day
   */
  void setDayEvents(LocalDate date, List<ViewEvent> events);

  /**
   * Adds a ViewListener to receive events from the view.
   * Implementations should store listeners and emit events to them.
   *
   * @param listener the ViewListener to add
   */
  void addViewListener(ViewListener listener);

  /**
   * Gets the current month being displayed by the view.
   *
   * @return the current month
   */
  LocalDate getCurrentMonth();

  /**
   * Gets the selected date in the view.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate();

  /**
   * Updates the timezone displayed in the view.
   *
   * @param timezone the timezone string
   */
  void updateTimezone(String timezone);

  /**
   * Updates the current month displayed in the view.
   *
   * @param month the current month
   */
  void updateCurrentMonth(LocalDate month);

  /**
   * Updates and keep track of the color of the calendar.
   *
   * @param oldName of the calendar.
   * @param newName new name of the calendar.
   */
  void calendarRename(String oldName, String newName);
}
