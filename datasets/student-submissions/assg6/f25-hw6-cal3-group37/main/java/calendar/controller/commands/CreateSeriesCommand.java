package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Command to create a recurring event series.
 */
public class CreateSeriesCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final Set<DayOfWeek> daysOfWeek;
  private final Integer occurrences;
  private final LocalDate untilDate;

  /**
   * Constructs a CreateSeriesCommand.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @param days the days of the week to repeat on
   * @param occurrences the number of occurrences (null for date-based end)
   * @param until the end date for recurrence (null for count-based end)
   */
  public CreateSeriesCommand(String subject, LocalDateTime start,
                             LocalDateTime end, Set<DayOfWeek> days,
                             Integer occurrences, LocalDate until) {
    this.subject = subject;
    this.startDateTime = start;
    this.endDateTime = end;
    this.daysOfWeek = days;
    this.occurrences = occurrences;
    this.untilDate = until;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      calendar.addEventSeries(subject, startDateTime, endDateTime,
          daysOfWeek, occurrences, untilDate);
      view.displayMessage("Event series created successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}