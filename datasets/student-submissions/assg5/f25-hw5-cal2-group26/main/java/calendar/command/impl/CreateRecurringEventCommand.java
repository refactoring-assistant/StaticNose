package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import calendar.model.Event;
import calendar.model.EventInterface;
import calendar.model.EventStatus;
import calendar.model.RecurringEvent;
import calendar.model.RecurringEventInterface;
import calendar.model.Weekday;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * A command class for creating a recurring timed event in the calendar. Implements
 * the CommandInterface to define the behavior for executing a recurring timed event creation,
 * with recurrence defined by specific weekdays, a number of occurrences, or an end date.
 */
public class CreateRecurringEventCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
  private final List<Weekday> weekdays;
  private final Integer occurrences;
  private final ZonedDateTime until;
  private final EventStatus status;

  /**
   * Constructs a CreateRecurringEventCommand instance with the specified parameters.
   * Initializes a recurring timed event command, assigning a default EventStatus.PUBLIC
   * if the provided status is null.
   *
   * @param subject the subject or title of the recurring event.
   * @param start the ZonedDateTime specifying the start time of the event.
   * @param end the ZonedDateTime specifying the end time of the event.
   * @param weekdays the List of Weekday objects defining the days of the week for recurrence.
   * @param occurrences the Integer specifying the number of occurrences, or null if not
   *                    limited by count.
   * @param until the ZonedDateTime specifying the end date of the recurrence, or null if not
   *              limited by date.
   * @param status the EventStatus of the event, or null to default to EventStatus.PUBLIC.
   */
  public CreateRecurringEventCommand(CalendarControllerInterface controller, String subject,
                                     ZonedDateTime start, ZonedDateTime end,
                                     List<Weekday> weekdays, Integer occurrences,
                                     ZonedDateTime until, EventStatus status) {
    this.controller = controller;
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.weekdays = weekdays;
    this.occurrences = occurrences;
    this.until = until;
    this.status = (status != null) ? status : EventStatus.PUBLIC;
  }

  @Override
  public String execute() {
    try {
      if (start.isAfter(end)) {
        return "Error: Start time must be before end time.";
      }
      if ((occurrences == null || occurrences == 0) && until == null) {
        return "Error: Must provide either occurrences or until date for recurring event.";
      }

      EventInterface template =
          new Event(subject, start, end, null, null, status, false);
      RecurringEventInterface recurring = new RecurringEvent(template, weekdays,
          (occurrences != null) ? occurrences : 0, until);
      controller.createRecurringEvent(recurring);
      return "Recurring event created successfully: " + subject;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Create a recurring event";
  }
}
