package calendar.controller;

import calendar.view.EventDetailsPanel;
import calendar.view.ViewEvent;
import calendar.view.dialog.CalendarDialogResult;
import calendar.view.dialog.EventDialogResult;
import java.util.List;
import java.util.Set;

/**
 * Interface for handling requests from the view.
 */
public interface IntViewRequestHandler {
  /**
   * Gets events in a date range for display.
   *
   * @param startYear  start year
   * @param startMonth start month
   * @param startDay   start day
   * @param endYear    end year
   * @param endMonth   end month
   * @param endDay     end day
   * @return list of view events
   */
  List<ViewEvent> handleGetViewEventsInRange(String calendarName,
                                             int startYear, int startMonth, int startDay,
                                             int endYear, int endMonth, int endDay);

  /**
   * Handles a request to create a new calendar.
   * Shows a dialog, validates input, and creates the calendar.
   *
   * @return the result of the dialog, or null if cancelled
   */
  CalendarDialogResult handleCreateCalendarRequest();

  /**
   * Handles a request to create a new event.
   * Shows a dialog, validates input, and creates the event.
   *
   * @param initialYear  the initially selected year
   * @param initialMonth the initially selected month
   * @param initialDay   the initially selected day
   * @return the result of the dialog, or null if cancelled
   */
  EventDialogResult handleCreateEventRequest(int initialYear, int initialMonth, int initialDay);

  /**
   * Gets the set of existing calendar names for validation.
   *
   * @return set of calendar names
   */
  Set<String> getExistingCalendarNames();

  /**
   * Handles a request to view event details.
   * Shows a details pane with an edit button.
   *
   * @param event the event to display details for
   * @return the event details panel
   */
  EventDetailsPanel handleViewEventDetailsRequest(ViewEvent event);

  /**
   * Handles a request to edit an existing event.
   * Shows an edit dialog, validates input, and updates the event.
   *
   * @param event the event to edit
   * @return the updated event data, or null if cancelled
   */
  EventDialogResult handleEditEventRequest(ViewEvent event);

  /**
   * Handles a request to switch to a different calendar.
   * Updates the active calendar in the model.
   *
   * @param calendarName the name of the calendar to switch to
   */
  void handleSwitchCalendarRequest(String calendarName);
}
