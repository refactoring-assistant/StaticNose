package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command to print events in a date/time range.
 */
public class PrintEventsInRangeCommand implements Command {
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs a PrintEventsInRangeCommand.
   *
   * @param start the start of the date range
   * @param end the end of the date range
   */
  public PrintEventsInRangeCommand(LocalDateTime start, LocalDateTime end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      List<CalendarEvent> events = calendar.getEventsInRange(start, end);
      view.displayEvents(events, true);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}