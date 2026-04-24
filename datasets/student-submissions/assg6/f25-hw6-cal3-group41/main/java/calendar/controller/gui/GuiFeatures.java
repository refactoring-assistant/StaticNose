package calendar.controller.gui;


import java.time.LocalDate;
import java.util.List;

/**
 * Methods the GUI can call into the controller to perform operations.
 *
 * <p>The controller implements these and translates to model operations.
 */
public interface GuiFeatures {
  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name the calendar name
   * @param zoneId the timezone ID
   */
  void createCalendar(String name, String zoneId);

  /**
   * Switches to the specified calendar.
   *
   * @param calendarName the name of the calendar to switch to
   */
  void switchCalendar(String calendarName);

  /**
   * Navigates to the previous month.
   */
  void previousMonth();

  /**
   * Navigates to the next month.
   */
  void nextMonth();

  /**
   * User requested to view events for a particular day.
   */
  void requestViewDay(LocalDate date);

  /**
   * User requested to create an event starting on the given date.
   */
  void requestCreateEvent(LocalDate date);

  /**
   * User clicked "Edit" for an event summary shown for the given day.
   * Controller should decide how to find the actual event(s) and open editor.
   */
  void requestEditEvent(LocalDate day, String eventSummary);

  /**
   * User requested to bulk edit multiple events with the same name.
   *
   * @param day the day where the events are displayed
   * @param eventName the name/subject of the events to edit
   * @param selectedSummaries list of event summaries that were selected for bulk edit
   */
  void requestBulkEditEvents(LocalDate day, String eventName, List<String> selectedSummaries);

  /**
   * User requested to edit an entire event series.
   *
   * @param day the day where the event is displayed
   * @param eventSummary the summary of the event (any event in the series)
   */
  void requestEditSeries(LocalDate day, String eventSummary);
}
