package calendar.view;

import calendar.controller.IntViewRequestHandler;
import java.util.List;

/**
 * An interface describing the functionality of a Gui View for a calendar application.
 */
public interface IntGuiView {
  /**
   * Starts the Gui view rendering to the user.
   */
  void go();

  /**
   * Creates an error message dialog box to give feedback to the user on any error states.
   *
   * @param message the message shown to the user in the error dialog box
   * @param messageType the JOptionPane type such as JOptionPane.ERROR_MESSAGE
   */
  void createMessagePopup(Object message, String title, int messageType);

  /**
   * A request to add a calendar to the Gui view.
   *
   * @param calendarName the name of the calendar which should be added
   */
  void addCalendar(String calendarName);

  /**
   * Refreshes the calendar display.
   */
  void refreshCalendarDisplay();

  /**
   * Changes the current used calendar.
   *
   * @param calendarName the name of the calendar which should be used
   */
  void changeCalendar(String calendarName);

  /**
   * Makes a request to the requestHandler to collect events in a certain range from the model.
   *
   * @param calendarName the name of the calendar used
   * @param startYear the start year of the request
   * @param startMonth the start month of the request
   * @param startDay the start day of the request
   * @param endYear the end year of the request
   * @param endMonth the end month of the request
   * @param endDay the end day of the request
   * @return a list of ViewEvents which the view can now render appropriately
   */
  List<ViewEvent> getEventsInRange(String calendarName,
                                   int startYear, int startMonth, int startDay,
                                   int endYear, int endMonth, int endDay);

  /**
   * Sets the request handler which is the object that interfaces with the model for the view.
   *
   * @param requestHandler the object which will handle requests
   */
  void setRequestHandler(IntViewRequestHandler requestHandler);

  /**
   * Gets the request handler which is the object that interfaces with the model for the view.
   *
   * @return the request handler
   */
  IntViewRequestHandler getRequestHandler();
}
