package calendar;

import java.util.List;

/**
 * Calendar View Interface.
 */
public interface IcalenderView {
  /**
   * Shows the options to the user.
   */
  void showOptions();

  /**
   * Displays the events to the user.
   *
   * @param dayEvents All the possible events.
   */
  void showEvents(List<Event> dayEvents);

  /**
   * Shows the format error.
   */
  void showError();

  /**
   * Shows that event already exists.
   */
  void eventExistsError();

  /**
   * The status of the person on a date.
   *
   * @param a true or false from the controller.
   */
  void isBusy(boolean a);

  /**
   * Displays the path of the exported csv file.
   *
   * @param s the absolute path to display.
   */
  void displayAbsolutePath(String s);

  /**
   * Event/Series creation success.
   */
  void addEventSucess();

  /**
   * Edition of series,event success.
   */
  void editSucess();
}
