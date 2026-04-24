package calendar.controller;

import calendar.model.CalendarModelImpl;
import java.io.IOException;

/**
 * Interface for calendar controller.
 * Controller coordinating with the {@link CalendarModelImpl}
 */
public interface Icontroller {

  /**
   * Start the calendar application in interactive mode.
   *
   * @param input the input stream to read commands from
   */
  void run(Readable input) throws IOException;

  /**
   * Execute a single command and return success/failure.
   *
   * @param command the command tokens
   * @return true if command executed successfully, false otherwise
   */
  boolean executeCommand(String command) throws IOException;
}
