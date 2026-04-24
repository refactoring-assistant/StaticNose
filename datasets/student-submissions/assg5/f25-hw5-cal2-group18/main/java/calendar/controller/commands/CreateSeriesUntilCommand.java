package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventImpl;
import calendar.model.WeekDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command that creates a recurring event series until a specific date.
 */
public class CreateSeriesUntilCommand implements CalendarCommand {

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final List<WeekDay> days;
  private final LocalDate endDate;

  /**
   * Constructs a CreateSeriesUntilCommand with the given details.
   *
   * @param subject event subject
   * @param start   start date and time
   * @param end     end date and time
   * @param days    weekdays to repeat on
   * @param endDate last date to create event (inclusive)
   */
  public CreateSeriesUntilCommand(String subject, LocalDateTime start, LocalDateTime end,
                                  List<WeekDay> days, LocalDate endDate) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.days = days;
    this.endDate = endDate;
  }

  @Override
  public String execute(Calendar model) {
    Event baseEvent = new EventImpl(subject, start, end);
    String seriesId = model.addEventSeriesUntil(baseEvent, days, endDate);
    return "Created event series \"" + subject + "\" with ID " + seriesId;
  }
}