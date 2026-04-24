package controller.command;

import controller.CommandResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import model.Icalendar;
import model.Ievent;

/**
 * Command implementation for printing events within a date/time range.
 * This command retrieves all events that partially or completely fall
 * within the specified range.
 */
public class PrintEventsRangeCommand implements Command {
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("h:mm a", Locale.US);

  /**
   * Constructs a PrintEventsRangeCommand for the specified date/time range.
   *
   * @param startDateTime the start of the range (inclusive)
   * @param endDateTime   the end of the range (inclusive)
   */
  public PrintEventsRangeCommand(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public CommandResult execute(Icalendar calendar) {
    try {
      List<Ievent> events = calendar.getEventsInRange(
          startDateTime.toLocalDate(),
          endDateTime.toLocalDate()
      );

      if (events.isEmpty()) {
        return new CommandResult(true, "No events in the specified range");
      }

      StringBuilder sb = new StringBuilder("Events:\n");
      for (Ievent event : events) {
        sb.append(". ").append(event.getSubject())
            .append(" starting on ").append(event.getStartDateTime().format(DATE_FORMATTER))
            .append(" at ").append(event.getStartDateTime().format(TIME_FORMATTER))
            .append(", ending on ").append(event.getEndDateTime().format(DATE_FORMATTER))
            .append(" at ").append(event.getEndDateTime().format(TIME_FORMATTER));

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