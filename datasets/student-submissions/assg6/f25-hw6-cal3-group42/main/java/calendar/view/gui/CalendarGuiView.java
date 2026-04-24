package calendar.view.gui;

import calendar.controller.gui.Features;
import calendar.view.CalendarViewInterface;

/**
 * Interface for the graphical user interface view of the calendar application.
 * Extends the basic CalendarViewInterface to support GUI-specific operations.
 */
public interface CalendarGuiView extends CalendarViewInterface {

  /**
   * Sets the features callback interface that this view will use to
   * communicate with the controller.
   *
   * @param features the Features implementation, the new GUI controller
   */
  void setFeatures(Features features);

  /**
   * Makes the GUI visible to the user.
   */
  void displayGui();

  /**
   * Refreshes the calendar display to show current month and events.
   * Called after events are created/edited/deleted or when month changes.
   */
  void refreshCalendarView();

  /**
   * Updates the calendar list in the UI when a new calendar is created
   * or the active calendar changes.
   */
  void updateCalendarList();

  /**
   * Displays an information message dialog to the user.
   *
   * @param message the message to display
   */
  void showMessage(String message);

  /**
   * Displays an error message dialog to the user.
   *
   * @param errorMessage the error message to display
   */
  void showError(String errorMessage);

  /**
   * Shows a confirmation dialog to the user.
   *
   * @param message the confirmation message
   * @return true if user confirms, false otherwise
   */
  boolean showConfirmation(String message);
}