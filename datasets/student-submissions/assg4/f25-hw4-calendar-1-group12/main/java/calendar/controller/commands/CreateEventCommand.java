package calendar.controller.commands;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Command to create a single event in the calendar.
 */
public class CreateEventCommand extends AbstractCommand {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs a CreateEventCommand with the specified event details.
   *
   * @param subject the subject of the event
   * @param start   the start date and time
   * @param end     the end date and time
   */
  public CreateEventCommand(String subject, LocalDateTime start, LocalDateTime end) {
    this.subject = subject;
    this.start = start;
    this.end = end;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    try {
      CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder()
          .subject(subject)
          .startDateTime(start);

      if (end != null) {
        builder.endDateTime(end);
      }

      Event event = builder.build();
      model.addEvent(event);
      view.displayMessage("Event created successfully.");
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}