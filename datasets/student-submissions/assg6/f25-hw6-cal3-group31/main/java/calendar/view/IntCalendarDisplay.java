package calendar.view;

import java.awt.Container;
import java.awt.GridBagConstraints;

/**
 * Interface for calendar display components.
 * Defines methods for setting up the display area,
 * adding events, and refreshing the view.
 */
public interface IntCalendarDisplay {

  /**
   * Sets up the calendar display within the given container
   * using the specified GridBagConstraints.
   *
   * @param pane the container to set up the display in
   * @param c    the GridBagConstraints for layout management
   */
  void setup(Container pane, GridBagConstraints c);

  /**
   * Adds an event to the calendar display.
   *
   * @param event the ViewEvent to add
   */
  void addEvent(ViewEvent event);

  /**
   * Refreshes the calendar display to reflect any changes.
   */
  void refresh();

  /**
   * Updates the calendar grid contents for a given calendar name.
   *
   * @param calendarName the name of the calendar with which to update the contents of the grid
   */
  void updateCalendarGridContents(String calendarName);

  /**
   * Sets the current calendar name.
   *
   * @param calendarName the calendar name to use
   */
  void setCalendarName(String calendarName);
}
