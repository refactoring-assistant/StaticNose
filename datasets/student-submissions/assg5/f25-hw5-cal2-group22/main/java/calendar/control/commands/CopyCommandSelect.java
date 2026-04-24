package calendar.control.commands;

import calendar.model.database.IcalendarDatabase;

/**
 * Class to create the appropriate copy command based on input.
 */

public class CopyCommandSelect {

  /**
   * Creates the appropriate copy command based on the input string.
   *
   * @param multipleCalendar the calendar database
   * @param input            the raw command input
   * @return the appropriate copy command, or null if input doesn't match
   */
  public static Icommand createCopyCommand(IcalendarDatabase multipleCalendar,
                                           String input) {
    String lowerInput = input.toLowerCase().trim();

    if (lowerInput.startsWith("copy event ")) {
      return new CopySingleEventCommand(multipleCalendar, input);
    } else if (lowerInput.startsWith("copy events on ")) {
      return new CopyEventsOnDateCommand(multipleCalendar, input);
    } else if (lowerInput.startsWith("copy events between")) {
      return new CopyEventsBetweenCommand(multipleCalendar, input);
    }

    return null;
  }
}
