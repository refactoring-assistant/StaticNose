package calendar.view;

import calendar.controller.Features;
import calendar.model.InterfaceEvent;
import java.util.List;

/**
 * The interface for the Graphical User Interface (GUI) view component.
 * Defines methods for updating the visual state of the application based on controller actions.
 */
public interface GuiView {

  /**
   * Wires the controller to this view.
   * Establishes the connection allowing the View to delegate user actions to the Controller.
   *
   * @param features the controller instance implementing the Features interface
   */
  void setFeatures(Features features);

  /**
   * Updates the main calendar grid display.
   * Refreshes the header and populates grid cells with day numbers.
   *
   * @param monthYear the text for the header
   * @param days      an array of strings representing day numbers
   */
  void updateMonthDisplay(String monthYear, String[] days);

  /**
   * Displays a list of events in the side panel.
   * Clears the current list and repopulates it with the provided events.
   *
   * @param events the list of events to display
   */
  void displayEventList(List<InterfaceEvent> events);

  /**
   * Updates the dropdown list of available calendars.
   *
   * @param calendarNames a list of all available calendar names
   */
  void updateCalendarList(List<String> calendarNames);

  /**
   * Updates the UI to indicate the active calendar.
   * Updates the dropdown selection and the current calendar label.
   *
   * @param calendarName the name of the calendar to highlight
   */
  void highlightActiveCalendar(String calendarName);

  /**
   * Displays a pop-up error message.
   *
   * @param message the error message to display
   */
  void showErrorPopup(String message);

  /**
   * Displays a pop-up success message.
   *
   * @param message the message to display
   */
  void showMessagePopup(String message);

  /**
   * Controls the visibility of the main window.
   *
   * @param visible true to show the window, false to hide it
   */
  void setVisible(boolean visible);
}