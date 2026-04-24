package calendar.controller.commands;

import calendar.model.IntCalendar;

/**
 * Interface for a command from user that executes logic to produce the command in a Calendar.
 */
public interface IntCommand {
  /**
   * Executes the logic to parse an input and request the input command's effects in the
   * Calendar's model.
   *
   * @param input the remaining input from the user for the given command.
   */
  void go(String input, IntCalendar calendar);
}
