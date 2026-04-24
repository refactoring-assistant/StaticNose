package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.model.EventSeries;
import calendar.model.RecurringEventSeries;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Command to create a recurring event series in the calendar.
 */
public class EventSeriesCommand extends AbstractCommand {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final Set<DayOfWeek> weekdays;
  private final Integer repeatCount;
  private final LocalDate repeatUntil;

  /**
   * Constructs an EventSeriesCommand to create a recurring event series.
   *
   * @param subject the subject of the event series
   * @param startDateTime the start date and time for events in the series
   * @param endDateTime the end date and time for events in the series
   * @param weekdays the days of the week on which events should occur
   * @param repeatCount the number of times to repeat, or null if using repeatUntil
   * @param repeatUntil the date until which to repeat, or null if using repeatCount
   */
  public EventSeriesCommand(String subject, LocalDateTime startDateTime,
                            LocalDateTime endDateTime, Set<DayOfWeek> weekdays,
                            Integer repeatCount, LocalDate repeatUntil) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.weekdays = weekdays;
    this.repeatCount = repeatCount;
    this.repeatUntil = repeatUntil;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    try {
      RecurringEventSeries.EventSeriesBuilder builder =
          new RecurringEventSeries.EventSeriesBuilder()
              .subject(subject)
              .startDateTime(startDateTime)
              .endDateTime(endDateTime)
              .weekdays(weekdays);

      if (repeatCount != null) {
        builder.repeatCount(repeatCount);
      } else if (repeatUntil != null) {
        builder.repeatUntil(repeatUntil);
      }

      EventSeries series = builder.build();
      model.addEventSeries(series);
      view.displayMessage("Event series created successfully with "
          + series.getAllEvents().size() + " events.");
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}