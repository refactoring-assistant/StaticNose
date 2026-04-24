package calendar.controller;

import calendar.model.CalenderManager;
import calendar.view.ViewConsole;

/**
 * Command interface for all calendar operations to implement Command Design Pattern.
 * */
public interface CommandInterface {

  /**
   * Execute the command.
   *
   * @param calenderManager instance of calendar manager
   * @param view the view for displaying results
   * @throws IllegalArgumentException if command execution fails
   * */
  void execute(CalenderManager calenderManager, ViewConsole view) throws IllegalArgumentException;
}
