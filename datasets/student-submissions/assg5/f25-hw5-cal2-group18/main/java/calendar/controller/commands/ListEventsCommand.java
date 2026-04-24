package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command to list all events in the calendar with detailed formatting.
 */
public class ListEventsCommand implements CalendarCommand {

  @Override
  public String execute(Calendar model) {
    List<Event> allEvents = model.getAllEvents();

    if (allEvents.isEmpty()) {
      return "No events scheduled.";
    }

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    StringBuilder sb = new StringBuilder("All Scheduled Events:\n");

    for (Event e : allEvents) {
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