package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import calendar.model.Event;
import calendar.model.EventInterface;
import calendar.model.EventStatus;
import java.time.ZonedDateTime;

/**
 * A command class for creating a timed event in the calendar. Implements the CommandInterface to
 * define the behavior for executing a timed event creation with a specific start and end time.
 */
public class CreateEventCommand implements CommandInterface {
  private CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
  private final EventStatus status;

  /**
   * Constructs a CreateEventCommand instance with the specified subject, start time,
   * end time, and status. Initializes a timed event command, assigning a default EventStatus.
   * PUBLIC if the provided status is null.
   *
   * @param subject the subject or title of the event.
   * @param start the ZonedDateTime specifying the start time of the event.
   * @param end the ZonedDateTime specifying the end time of the event.
   * @param status the EventStatus of the event, or null to default to EventStatus.PUBLIC.
   */
  public CreateEventCommand(CalendarControllerInterface controller, String subject,
                            ZonedDateTime start, ZonedDateTime end,
                            EventStatus status) {
    this.controller = controller;
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.status = (status != null) ? status : EventStatus.PUBLIC;
  }

  @Override
  public String execute() {
    try {
      EventInterface event = new Event(subject, start, end, null, null, status, false);
      controller.createEvent(event);
      return "Event created: " + subject;
    } catch (Exception e) {
      return "Error creating event: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Create a single event";
  }
}
