package calendar.controller;

import calendar.model.CalendarSystemModel;

/**
 * Represents a command from user.
 * Works on the system model.
 */
public interface Command {

  /**
   * Command execution using the model provided.
   *
   * @param model the system model
   * @return a output string.
   */
  String execute(CalendarSystemModel model);
}
