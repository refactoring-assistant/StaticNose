package calendar.controller;

import java.io.Reader;

/**
 * Interface for the calendar controller.
 */
public interface CalendarController {

  /**
   * Runs the controller with the provided input source.
   *
   * @param input the input source (Reader for files, InputStreamReader for System.in)
   */
  void run(Reader input);
}