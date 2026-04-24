package calendar.view;

import calendar.model.Events;
import java.util.List;

/**
 * Interface to view calendar output.
 * */
public interface InteractiveView {

  /**
   * Function to display list of events to user.
   *
   * @param events the list of calendar events
   * */
  void showEvents(List<Events> events);

  /**
   * Function to display error message to user.
   *
   * @param error the error message to display.
   * */
  void dispError(String error);

  /**
   * Function to show event status (busy or available).
   *
   * @param status the event's status
   * */
  void showStatus(String status);

  /**
   * Function to show success status on successful completion of a task.
   *
   * @param msg the success message
   * */
  void dispSuccess(String msg);
}
