package calendar.controller;

import java.io.IOException;

/**
 * Represents the controller for the calendar program.
 * Handles user input and coordinates between the model and the view.
 */
public interface CalendarController {

  /**
   * Runs the calendar program.
   * This method should keep reading input from the user,
   * execute the corresponding commands, and update the view.
   *
   * @return true if the controller exited normally with an exit command
   * @throws IOException if an input or output error occurs
   */
  boolean run() throws IOException;
}