package calendar.controller;

/**
 * Represents the calendar.Controller.ControllerImpl in the MVC structure for the calendar app.
 * The controller reads commands from an input source (interactive or headless),
 * coordinates with the model to perform operations, and uses the view to display results.
 */
public interface Controller {

  /**
   * Starts the controller loop and processes user commands until 'exit' or EOF.
   */
  void go();

}
