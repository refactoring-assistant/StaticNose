package calendar.controller.commands;

import calendar.model.CalendarInterface;
import java.io.IOException;

/**
 * Command interface for all calendar operations.
 * Command encapsulates one operation (Create, Edit, Print, etc.).
 * This follows the Command Pattern:
 * <ul><li>Each operation is a separate class
 * <li>All commands implement this common interface
 * <li>Controller creates command objects and calls {@code go()}</ul>
 */
public interface Icommands {
  /**
   * Execute this command on the given model.
   *
   * @param model the calendar model to operate on
   * @throws IllegalArgumentException if command execution fails
   */
  void go(CalendarInterface model) throws IllegalArgumentException, IOException;

}
