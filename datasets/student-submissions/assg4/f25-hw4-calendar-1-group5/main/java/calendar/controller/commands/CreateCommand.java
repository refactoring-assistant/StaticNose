package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDateTime;

/**
 * This class represents the command used to create a single event by the controller.
 */
public class CreateCommand implements Command {
  private final String subject;
  private final LocalDateTime startsAt;
  private final LocalDateTime endsAt;

  /**
   * Event subject, start timestamp and end timestamp are required to initialize this command.
   *
   * @param subject The subject of the event to be created.
   * @param startsAt The start timestamp of the event to be created.
   * @param endsAt The end timestamp of the event to be created.
   */
  public CreateCommand(String subject, LocalDateTime startsAt, LocalDateTime endsAt) {
    this.subject = subject;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }

  @Override
  public void execute(Model model, View view) {
    model.createEvent(builder -> {
      builder.subject(subject).startsAt(startsAt).endsAt(endsAt);
    });
  }
}
