package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.Event;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the command used to print all events in a given range by the controller.
 */
public class PrintCommand implements Command {
  private final LocalDateTime startsAt;
  private final LocalDateTime endsAt;

  /**
   * Initializes the print command with the observation window.
   *
   * @param startsAt The beginning timestamp of the observation window.
   * @param endsAt The ending timestamp of the observation window.
   */
  public PrintCommand(LocalDateTime startsAt, LocalDateTime endsAt) {
    this.startsAt = Objects.requireNonNull(startsAt);
    this.endsAt = Objects.requireNonNull(endsAt);
  }

  @Override
  public void execute(Model model, View view) {
    List<Event> events = model.filterEvents(
        event -> (event.endsAt().equals(startsAt) || event.endsAt().isAfter(startsAt))
            && (event.startsAt().equals(endsAt) || event.startsAt().isBefore(endsAt)));

    sortEvents(events);

    for (Event event : events) {
      view.render(event.subject() + " - " + event.startsAt() + " - " + event.endsAt() + "\n");
    }
  }

  private void sortEvents(List<Event> events) {
    events.sort((o1, o2) -> {
      if (o1.startsAt().isBefore(o2.startsAt())) {
        return -1;
      } else if (o1.startsAt().equals(o2.startsAt())) {
        if (o1.endsAt().isBefore(o2.endsAt())) {
          return -1;
        } else if (o1.endsAt().equals(o2.endsAt())) {
          return o1.subject().compareTo(o2.subject());
        } else {
          return 1;
        }
      } else {
        return 1;
      }
    });
  }
}
