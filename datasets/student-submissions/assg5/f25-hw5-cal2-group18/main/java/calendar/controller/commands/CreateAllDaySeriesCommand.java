package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventImpl;
import calendar.model.WeekDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command that creates a recurring all-day event series in the calendar.
 */
public class CreateAllDaySeriesCommand implements CalendarCommand {

  private final String subject;
  private final LocalDateTime date;
  private final List<WeekDay> days;
  private final Integer occurrences;
  private final LocalDate endDate;   

  /**
   * Constructs a CreateAllDaySeriesCommand with occurrences.
   *
   * @param subject     event subject
   * @param date        start date
   * @param days        weekdays to repeat on
   * @param occurrences number of times to repeat
   */
  public CreateAllDaySeriesCommand(String subject, LocalDateTime date,
                                   List<WeekDay> days, int occurrences) {
    this.subject = subject;
    this.date = date;
    this.days = days;
    this.occurrences = occurrences;
    this.endDate = null;
  }

  /**
   * Constructs a CreateAllDaySeriesCommand with end date.
   *
   * @param subject event subject
   * @param date    start date
   * @param days    weekdays to repeat on
   * @param endDate last date to create event (inclusive)
   */
  public CreateAllDaySeriesCommand(String subject, LocalDateTime date,
                                   List<WeekDay> days, LocalDate endDate) {
    this.subject = subject;
    this.date = date;
    this.days = days;
    this.occurrences = null;
    this.endDate = endDate;
  }

  @Override
  public String execute(Calendar model) {
    Event baseEvent = new EventImpl(subject, date);
    String seriesId;

    if (occurrences != null) {
      seriesId = model.addEventSeries(baseEvent, days, occurrences);
    } else {
      seriesId = model.addEventSeriesUntil(baseEvent, days, endDate);
    }

    return "Created all-day event series \"" + subject + "\" with ID " + seriesId;
  }
}