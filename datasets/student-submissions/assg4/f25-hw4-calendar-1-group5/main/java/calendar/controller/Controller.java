package calendar.controller;

import calendar.model.Model;
import calendar.view.View;

/**
 * Interface for a controller used by the calendar application.
 */
public interface Controller {
  /**
   * A method to start the controller.
   *
   * @param model The model controller will use for the Model layer.
   * @param view  The view controller will use for the View layer.
   */
  void go(Model model, View view);
}
