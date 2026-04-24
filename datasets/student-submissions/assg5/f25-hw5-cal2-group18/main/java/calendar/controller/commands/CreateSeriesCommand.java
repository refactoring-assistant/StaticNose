package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventImpl;
import calendar.model.WeekDay;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command that creates a recurring event series in the calendar.
 */
public class CreateSeriesCommand implements CalendarCommand {

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final List<WeekDay> days;
  private final int occurrences;

  /**
   * Constructs a CreateSeriesCommand with the given details.
   *
   * @param subject     event subject
   * @param start       start date and time
   * @param end         end date and time
   * @param days        weekdays to repeat on
   * @param occurrences number of times to repeat
   */
  public CreateSeriesCommand(String subject, LocalDateTime start, LocalDateTime end,
                             List<WeekDay> days, int occurrences) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.days = days;
    this.occurrences = occurrences;
  }

  @Override
  public String execute(Calendar model) {
    Event baseEvent = new EventImpl(subject, start, end);
    String seriesId = model.addEventSeries(baseEvent, days, occurrences);
    return "Created event series \"" + subject + "\" with ID " + seriesId;
  }
}