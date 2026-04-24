package calendar.view;

import calendar.model.event.Ievent;
import java.util.List;

/**
 * Interface for CalendarView.
 */
public interface IcalendarView {

  /**
   * Display a single event.
   *
   * @param event the event to display to user
   */
  void displayEvent(Ievent event);

  /**
   * Display multiple events.
   *
   * @param events the list of events to display to user
   */
  void displayEvents(List<Ievent> events);

  /**
   * Display a bulleted list of events on a specific date.
   * Format: Subject (time - time) [Location].
   *
   * @param events the list of events to display
   */
  void displayEventsOnDate(List<Ievent> events);

  /**
   * Display a message to user.
   *
   * @param message the message to be displayed to user
   */
  void displayMessage(String message);

  /**
   * Display if user is busy or not.
   *
   * @param isBusy false if available else true
   */
  void displayStatus(boolean isBusy);

  /**
   * Display an error message to user.
   *
   * @param error the error to be displayed to user
   */
  void displayError(String error);

  /**
   * Display the absolute path of an exported file.
   *
   * @param filePath the absolute path to display
   */
  void displayExportPath(String filePath);

}


