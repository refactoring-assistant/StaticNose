package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventImpl;
import java.time.LocalDateTime;

/**
 * Command to add a single event to the calendar.
 */
public class AddEventCommand implements CalendarCommand {

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;

  /**
   * Constructs an AddEventCommand.
   *
   * @param subject      the subject/title of the event
   * @param start        the start date and time
   * @param end          the end date and time
   * @param description  optional description for the event
   */
  public AddEventCommand(String subject, LocalDateTime start,
                         LocalDateTime end, String description) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
  }

  @Override
  public String execute(Calendar model) {
    Event event = new EventImpl(subject, start, end);
    event.setDescription(description);
    model.addEvent(event);
    return "Added event: " + subject;
  }
}