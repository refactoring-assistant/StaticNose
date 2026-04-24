package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.Model;
import calendar.view.View;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * This class represents the command used to create event series that repeats given number of times.
 */
public class CreateSeriesWithRepetitionCommand implements Command {
  private final String subject;
  private final LocalDateTime startsAt;
  private final LocalDateTime endsAt;
  private final HashSet<DayOfWeek> weekdays;
  private final int repetition;

  /**
   * This constructor initialize the class with all the required parameters for creating
   * a series of events.
   *
   * @param subject The subject of the event to be created.
   * @param startsAt The start timestamp of the event to be created.
   * @param endsAt The end timestamp of the event to be created.
   * @param weekdays The weekdays the event should repeat.
   * @param repetition The number of times the event should repeat.
   */
  public CreateSeriesWithRepetitionCommand(String subject, LocalDateTime startsAt,
                                           LocalDateTime endsAt, HashSet<DayOfWeek> weekdays,
                                           int repetition) {
    this.subject = subject;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.weekdays = weekdays;
    this.repetition = repetition;
  }

  @Override
  public void execute(Model model, View view) {
    model.createEventSeries(builder -> {
      builder.subject(subject).startsAt(startsAt).endsAt(endsAt);
    }, weekdays, repetition);
  }
}
