package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventImpl;
import java.time.LocalDateTime;

/**
 * Command to add a single all-day event to the calendar.
 */
public class CreateAllDayEventCommand implements CalendarCommand {

  private final String subject;
  private final LocalDateTime date;

  /**
   * Constructs a CreateAllDayEventCommand.
   *
   * @param subject the subject/title of the event
   * @param date    the date of the all-day event
   */
  public CreateAllDayEventCommand(String subject, LocalDateTime date) {
    this.subject = subject;
    this.date = date;
  }

  @Override
  public String execute(Calendar model) {
    Event event = new EventImpl(subject, date);
    model.addEvent(event);
    return "Added all-day event: " + subject;
  }
}