package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Command to print all events on a specific date.
 */
public class PrintEventsOnCommand extends AbstractCommand {
  private final LocalDate date;

  /**
   * Creates a PrintEventsOnCommand to print events on a specific date.
   *
   * @param date the date to query for events
   */
  public PrintEventsOnCommand(LocalDate date) {
    this.date = date;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    List<Event> events = model.getEventsOn(date);
    view.displayEvents(events);
  }
}