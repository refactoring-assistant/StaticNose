package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command to print events occurring within a specific date and time range.
 */
public class PrintEventsCommand implements CalendarCommand {
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Constructs a PrintEventsCommand for a given date and time range.
   *
   * @param start the start date and time of the range.
   * @param end the end date and time of the range.
   */
  public PrintEventsCommand(LocalDateTime start, LocalDateTime end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public String execute(Calendar model) {
    List<Event> events = model.getEventsInRange(start, end);

    if (events.isEmpty()) {
      return "No events found in the given range.";
    }

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    StringBuilder sb = new StringBuilder("Events:\n");

    for (Event e : events) {
      sb.append(String.format("- %s starting on %s at %s, ending on %s at %s\n",
          e.getSubject(),
          e.getStartDateTime().format(dateFormatter),
          e.getStartDateTime().format(timeFormatter),
          e.getEndDateTime().format(dateFormatter),
          e.getEndDateTime().format(timeFormatter)));
    }

    return sb.toString();
  }
}