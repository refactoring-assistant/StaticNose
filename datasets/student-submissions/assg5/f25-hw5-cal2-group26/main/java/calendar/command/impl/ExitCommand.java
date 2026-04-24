package calendar.command.impl;

import calendar.command.CommandInterface;

/**
 * Command to exit both the interactive and the headless modes.
 */
public class ExitCommand implements CommandInterface {

  @Override
  public String execute() {
    return "EXIT";
  }

  @Override
  public String getDescription() {
    return "Exit the application";
  }
}