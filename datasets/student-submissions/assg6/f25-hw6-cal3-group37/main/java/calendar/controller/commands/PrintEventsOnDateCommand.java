package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.util.List;

/**
 * Command to print all events on a specific date.
 */
public class PrintEventsOnDateCommand implements Command {
  private final LocalDate date;

  /**
   * Constructs a PrintEventsOnDateCommand.
   *
   * @param date the date to query events for
   */
  public PrintEventsOnDateCommand(LocalDate date) {
    this.date = date;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      List<CalendarEvent> events = calendar.getEventsOnDate(date);
      view.displayEvents(events, false);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}