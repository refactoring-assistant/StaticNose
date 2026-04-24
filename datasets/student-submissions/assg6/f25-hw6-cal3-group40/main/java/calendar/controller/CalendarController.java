package calendar.controller;

/**
 * This interface represents all the operations offered by the Controller in a Calendar application.
 * The Controller is responsible for managing the application's flow, receiving user input,
 * and coordinating the necessary actions between the Model (CalendarService) and the View
 * (CalendarView) components.
 */
public interface CalendarController {

  /**
   * Runs the application.
   * It handles the main application loop, reading commands
   * and processing the until the "exit" command is received.
   */
  void run();

}
