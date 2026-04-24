package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import calendar.model.Event;
import calendar.model.EventInterface;
import calendar.model.EventStatus;
import java.time.ZonedDateTime;

/**
 * A command class for creating an all-day event in the calendar. Implements the
 * CommandInterface to define the behavior for executing an all-day event creation.
 */
public class CreateAllDayEventCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime date;
  private final EventStatus status;

  /**
   * Constructs a CreateAllDayEventCommand instance with the specified subject, date, and status.
   * Initializes an all-day event command, assigning a default EventStatus.
   * PUBLIC if the provided status is null.
   *
   * @param subject the subject or title of the all-day event
   * @param date the ZonedDateTime specifying the date of the event
   * @param status the EventStatus of the event, or null to default to EventStatus.PUBLIC
   */
  public CreateAllDayEventCommand(CalendarControllerInterface controller, String subject,
                                  ZonedDateTime date, EventStatus status) {
    this.controller = controller;
    this.subject = subject;
    this.date = date;
    this.status = (status != null) ? status : EventStatus.PUBLIC;
  }

  @Override
  public String execute() {
    try {
      EventInterface event = new Event(subject, date, null, null, null, status, true);
      controller.createEvent(event);
      return "All-day event created successfully: " + subject;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Create an all-day event";
  }
}
