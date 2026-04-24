package calendar.controller;

import calendar.model.Model;
import calendar.view.View;

/**
 * This interface templates the set of methods used by the controller to execute the command.
 */
public interface Command {
  /**
   * A method to run the command.
   *
   * @param model The model the command should use.
   * @param view  The view to which the command must render, if any.
   */
  void execute(Model model, View view);
}
