package calendar.controller;

import calendar.model.Icalendar;
import java.util.List;

/**
 * Handles the print command for displaying events from the calendar.
 * Supports printing events occurring on a specific date or within a given
 * date-time interval.
 */
public class Print implements Command {

  private final String[] args;
  private final Icalendar model;

  /**
   * Constructs a Print command with the specified arguments and model.
   *
   * @param args the parsed user command tokens
   * @param model the calendar model used to retrieve event data
   */
  public Print(String[] args, Icalendar model) {
    this.args = args;
    this.model = model;
  }

  /**
   * Executes the print command.
   * Determines whether to display events for a specific date
   * or within a time interval based on the user’s command syntax.
   *
   * @return formatted event details or an error message if no events are found
   */
  @Override
  public String execute() {
    try {
      if (args.length < 3 || !args[0].equalsIgnoreCase("print")
          || !args[1].equalsIgnoreCase("events")) {
        return "Error: Invalid print command.\n";
      }

      if (args[2].equalsIgnoreCase("on") && args.length == 4) {
        String dateString = args[3];
        List<String> results = model.printEventsOn(dateString);
        if (results.isEmpty()) {
          return "No events found on " + dateString + ".\n";
        } else {
          StringBuilder output = new StringBuilder();
          for (String line : results) {
            output.append(line).append("\n");
          }
          return output.toString();
        }
      } else if (args[2].equalsIgnoreCase("from") && args.length == 6
          && args[4].equalsIgnoreCase("to")) {
        String start = args[3];
        String end = args[5];
        List<String> results = model.printEventsInInterval(start, end);
        if (results.isEmpty()) {
          return "No events found between " + start + " and " + end + ".\n";
        } else {
          StringBuilder output = new StringBuilder();
          for (String line : results) {
            output.append(line).append("\n");
          }
          return output.toString();
        }

      } else {
        return "Error: Invalid print syntax.\n";
      }

    } catch (Exception e) {
      return "Error while printing events: " + e.getMessage() + "\n";
    }
  }
}
