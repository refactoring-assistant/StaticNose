package calendarcontroller;

/**
 * Interface for the Calendar Controller in an MVC pattern.
 *
 * <p>The controller is responsible for handling user input,
 * interacting with the model, and directing the view to update.</p>
 */
public interface CalendarController {

  /**
   * Starts the controller, beginning the main application loop
   * to read input and process commands.
   */
  void run();

}
