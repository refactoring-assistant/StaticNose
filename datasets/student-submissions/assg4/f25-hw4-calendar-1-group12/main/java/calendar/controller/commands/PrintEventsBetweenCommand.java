package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to print all events between two date/times.
 */
public class PrintEventsBetweenCommand extends AbstractCommand {
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs a PrintEventsBetweenCommand to print events in a time range.
   *
   * @param start the start of the time range
   * @param end the end of the time range
   */
  public PrintEventsBetweenCommand(LocalDateTime start, LocalDateTime end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    List<Event> events = model.getEventsInRange(start, end);
    view.displayEvents(events);
  }
}