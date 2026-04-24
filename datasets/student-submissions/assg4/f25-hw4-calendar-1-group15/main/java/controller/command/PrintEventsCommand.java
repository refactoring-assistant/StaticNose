package controller.command;

import controller.CommandResult;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import model.Icalendar;
import model.Ievent;

/**
 * Command implementation for printing events on a specific date.
 * This command retrieves and formats all events occurring on the specified date,
 * including multi-day events that span the date.
 */
public class PrintEventsCommand implements Command {
  private final LocalDate date;
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("h:mm a", Locale.US);

  /**
   * Constructs the PrintEventsCommand with a specific date.
   *
   * @param date the date
   */
  public PrintEventsCommand(LocalDate date) {
    this.date = date;
  }

  @Override
  public CommandResult execute(Icalendar calendar) {
    try {
      List<Ievent> events = calendar.getEventsOnDate(date);

      if (events.isEmpty()) {
        return new CommandResult(true, "No events on " + date);
      }

      StringBuilder sb = new StringBuilder("Events on " + date + ":\n");
      for (Ievent event : events) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
        LocalDate eventEndDate = event.getEndDateTime().toLocalDate();

        String displayStart;
        String displayEnd;

        if (eventStartDate.equals(date) && eventEndDate.equals(date)) {
          displayStart = event.getStartDateTime().format(TIME_FORMATTER);
          displayEnd = event.getEndDateTime().format(TIME_FORMATTER);
        } else if (eventStartDate.equals(date)) {
          displayStart = event.getStartDateTime().format(TIME_FORMATTER);
          displayEnd = "11:59 PM";
        } else if (eventEndDate.equals(date)) {
          displayStart = "12:00 AM";
          displayEnd = event.getEndDateTime().format(TIME_FORMATTER);
        } else {
          displayStart = "12:00 AM";
          displayEnd = "11:59 PM";
        }

        sb.append(event.getSubject())
            .append(" from ").append(displayStart)
            .append(" to ").append(displayEnd);

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          sb.append(" at ").append(event.getLocation());
        }
        sb.append("\n");
      }

      return new CommandResult(true, sb.toString());
    } catch (Exception e) {
      return new CommandResult(false, "Error printing events: " + e.getMessage());
    }
  }
}