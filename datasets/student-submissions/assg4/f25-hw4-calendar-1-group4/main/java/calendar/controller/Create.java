package calendar.controller;

import calendar.model.Icalendar;

/**
 * Handles the create command for adding new events to the calendar.
 * Supports both single and recurring events, including timed and all-day formats.
 */
public class Create implements Command {

  private final String[] args;
  private final Icalendar model;

  /**
   * Constructs a Create command with the provided arguments and model.
   *
   * @param args the parsed command-line arguments
   * @param model the calendar model used to create events
   */
  public Create(String[] args, Icalendar model) {
    this.args = args;
    this.model = model;
  }

  /**
   * Executes the create command.
   * Determines whether the event is timed or all-day based on syntax
   * and delegates to the appropriate handler method.
   *
   * @return a status message indicating success or the type of error encountered
   */
  @Override
  public String execute() {
    if (args.length < 3 || !args[1].equalsIgnoreCase("event")) {
      return "Invalid create syntax. Use: create event <...>\n";
    }

    String subject = args[2];

    if (args.length >= 6 && args[3].equalsIgnoreCase("from")
        && args[5].equalsIgnoreCase("to")) {
      return handleTimedEvent(subject);
    } else if (args.length >= 5 && args[3].equalsIgnoreCase("on")) {
      return handleAllDayEvent(subject);
    } else {
      return "Unrecognized create command format.\n";
    }
  }

  /**
   * Creates a timed event, optionally with recurrence.
   *
   * @param subject the subject of the event
   * @return a confirmation or error message based on the result
   */
  private String handleTimedEvent(String subject) {
    String startDateTime = args[4];
    String endDateTime = args[6];

    String weekdays = null;
    Integer occurrences = null;
    String untilDate = null;

    if (args.length > 7 && args[7].equalsIgnoreCase("repeats")) {
      weekdays = args[8];
      if (args.length > 9) {
        if (args[9].equalsIgnoreCase("for") && args.length > 10) {
          occurrences = Integer.parseInt(args[10]);
          if (occurrences <= 0) {
            throw new IllegalArgumentException("Invalid occurrences count.");
          }
        } else if (args[9].equalsIgnoreCase("until") && args.length > 10) {
          untilDate = args[10];
        } else {
          return "Invalid recurrence syntax after 'repeats'.\n";
        }
      }
    }

    int eventId = model.createEvent(subject, startDateTime, endDateTime);
    if (eventId == -1) {
      return "Time slot conflict. Event not created.\n";
    }

    if (weekdays == null) {
      return "Single timed event created successfully (Event ID: " + eventId + ")\n";
    } else {
      String seriesId = model.updateEventDetails(eventId, weekdays, occurrences, untilDate);
      if (seriesId == null) {
        return "Failed to create recurring events.\n";
      }
      return "Recurring timed event series created successfully (Series ID: " + seriesId + ")\n";
    }
  }

  /**
   * Creates an all-day event or a recurring all-day series.
   *
   * @param subject the subject of the event
   * @return a confirmation message or error if creation fails
   * @throws IllegalArgumentException if recurrence syntax is invalid
   * @throws IllegalStateException if recurring events fail to create
   */
  private String handleAllDayEvent(String subject) {
    String date = args[4];
    String startDateTime = date + "T08:00";
    String endDateTime = date + "T17:00";
    String weekdays = null;
    Integer occurrences = null;
    String untilDate = null;
    if (args.length > 5) {
      if (args.length > 8 && args[5].equalsIgnoreCase("repeats")) {
        weekdays = args[6];
        if (args[7].equalsIgnoreCase("for")) {
          occurrences = Integer.parseInt(args[8]);
        } else if (args[7].equalsIgnoreCase("until")) {
          untilDate = args[8];
        } else {
          throw new IllegalArgumentException("Invalid recurrence syntax after 'repeats'.\n");
        }
      } else {
        throw new  IllegalArgumentException("Invalid syntax.\n");
      }
    }
    int eventId = model.createEvent(subject, startDateTime, endDateTime);
    String seriesId = model.updateEventDetails(eventId, weekdays, occurrences, untilDate);
    if (seriesId == null && weekdays != null) {
      throw new IllegalStateException("Failed to create recurring all-day events.\n");
    }
    return "All-day event created successfully.\n";
  }
}