package calendar.controller;

import calendar.model.Icalendar;

/**
 * Handles the show status command, which checks the user's
 * availability at a specific date and time. Displays whether the
 * calendar is marked as busy or available.
 */
public class ShowStatus implements Command {

  private final String[] args;
  private final Icalendar model;

  /**
   * Constructs a ShowStatus command with the specified arguments and model.
   *
   * @param args the parsed user command tokens
   * @param model the calendar model used to check user availability
   */
  public ShowStatus(String[] args, Icalendar model) {
    this.args = args;
    this.model = model;
  }

  /**
   * Executes the show status command by validating syntax and
   * querying the calendar model to determine whether the user is busy
   * or available at the specified date-time.
   *
   * @return "busy" if an event exists at the specified time,
   *         "available" otherwise, or an error message for invalid input
   */
  @Override
  public String execute() {
    if (args.length != 4
        || !args[0].equalsIgnoreCase("show")
        || !args[1].equalsIgnoreCase("status")
        || !args[2].equalsIgnoreCase("on")) {
      return "Error: Invalid syntax. Use: show status on <dateTime>";
    }

    String dateTime = args[3];

    try {
      boolean isBusy = model.checkBusyStatus(dateTime);
      return isBusy ? "busy" : "available";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}
