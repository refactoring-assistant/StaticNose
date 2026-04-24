package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Command to create a single event (timed or all-day).
 */
public class CreateEventCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * Creates command for a timed event.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   */
  public CreateEventCommand(String subject, LocalDateTime start, LocalDateTime end) {
    this.subject = subject;
    this.startDateTime = start;
    this.endDateTime = end;
  }

  /**
   * Creates command for an all-day event.
   *
   * @param subject the event subject
   * @param date the date of the event
   */
  public CreateEventCommand(String subject, LocalDate date) {
    this.subject = subject;
    this.startDateTime = LocalDateTime.of(date, LocalTime.of(8, 0));
    this.endDateTime = LocalDateTime.of(date, LocalTime.of(17, 0));
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      CalendarEvent event = new Event(subject, startDateTime, endDateTime);
      calendar.addEvent(event);
      view.displayMessage("Event created successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}