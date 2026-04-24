package calendar.controller;

import calendar.model.Icalendar;

/**
 * Handles the edit command for modifying existing events.
 * Supports editing a single event, all future events in a series,
 * or an entire recurring series based on the command scope.
 */
public class Edit implements Command {

  private final String[] args;
  private final Icalendar model;

  /**
   * Constructs a Edit command with the specified arguments and model.
   *
   * @param args the parsed user command tokens
   * @param model the calendar model used to update events
   */
  public Edit(String[] args, Icalendar model) {
    this.args = args;
    this.model = model;
  }

  /**
   * Executes the edit command by determining whether the
   * edit applies to a single event, all future events, or the entire series.
   *
   * @return a message describing the result of the operation
   */
  @Override
  public String execute() {
    try {
      if (!isValidCommand()) {
        return "Not Valid Command\n";
      }
      String scope = args[1].toLowerCase();
      switch (scope) {
        case "event":
          return handleSingleEventEdit();
        case "events":
          return handleSeriesEdit(true);
        case "series":
          return handleSeriesEdit(false);
        default:
          return "Error: Unknown edit scope. Use 'event', 'events', or 'series'.\n";
      }
    } catch (Exception e) {
      return "Error: " + e.getMessage() + "\n";
    }
  }

  /**
   * Validates the edit command structure to ensure correct syntax.
   *
   * @return true if the command follows the expected format; false otherwise
   */
  private boolean isValidCommand() {
    if (args.length < 7 || !args[0].equalsIgnoreCase("edit")) {
      return false;
    }
    if (!args[4].equalsIgnoreCase("from")) {
      return false;
    }
    return true;
  }

  /**
   * Handles editing of a single event identified by subject and time range.
   *
   * @return a message indicating success or failure of the edit
   */
  private String handleSingleEventEdit() {
    if (args.length < 9 || !args[6].equalsIgnoreCase("to")
        || !args[8].equalsIgnoreCase("with")) {
      return "Error: Expected format: edit event <property> <subject> from"
          + " <start> to <end> with <value>\n";
    }
    String property = args[2];
    String subject = args[3];
    String startDateTime = args[5];
    String endDateTime = args[7];
    String newValue = args[9];
    boolean success = model.editSingleEventInstance(subject, startDateTime, endDateTime, property,
        newValue) != -1;
    return success
        ? "Event updated successfully.\n"
        : "No matching event found to edit.\n";
  }

  /**
   * Handles editing of all future events or an entire recurring series.
   * Determines the edit scope using the provided flag.
   *
   * @param isFullSeries true if the entire recurring series should be updated,
   *                     false to update only future events
   * @return a message indicating success or failure of the edit
   */
  private String handleSeriesEdit(boolean isFullSeries) {
    if (args.length < 8 || !args[6].equalsIgnoreCase("with")) {
      return "Error: Expected format: edit [events|series] <property> <subject>"
          + " from <start> with <value>\n";
    }
    String property = args[2];
    String subject = args[3];
    String startDateTime = args[5];
    String newValue = args[7];
    boolean success = model.editSeriesEvents(subject, startDateTime, property,
        newValue, isFullSeries) != -1;

    return success
        ? (isFullSeries ? "Entire series updated.\n" : "Future events in series updated.\n")
        : "No matching event found to edit.\n";
  }
}
