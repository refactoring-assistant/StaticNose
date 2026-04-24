package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.LocalDate;
import java.util.List;

/**
 * Command to print events occurring on a specific date.
 */
public class PrintEventsOnDateCommand implements CalendarCommand {
  private final LocalDate date;

  /**
   * Constructs a PrintEventsOnDateCommand for the specified date.
   *
   * @param date the date to query
   */
  public PrintEventsOnDateCommand(LocalDate date) {
    this.date = date;
  }

  @Override
  public String execute(Calendar model) {
    List<Event> events = model.getEventsOnDate(date);

    if (events.isEmpty()) {
      return "No events found on " + date + ".";
    }

    StringBuilder sb = new StringBuilder("Events on " + date + ":\n");
    for (Event e : events) {
      sb.append("- ").append(e.getSubject())
          .append(" from ").append(e.getStartDateTime().toLocalTime())
          .append(" to ").append(e.getEndDateTime().toLocalTime());

      if (e.getLocation() != null && !e.getLocation().isEmpty()) {
        sb.append(" at ").append(e.getLocation());
      }

      sb.append("\n");
    }

    return sb.toString();
  }
}