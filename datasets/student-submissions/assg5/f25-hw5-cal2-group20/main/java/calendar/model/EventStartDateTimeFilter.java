package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a predicate class for filtering events based on the start datetime.
 */
public class EventStartDateTimeFilter implements Predicate<EventObject> {
  private final LocalDateTime startDateTime;

  /**
   * Constructs a filter based on the given event start datetime.
   *
   * @param startDateTime the start datetime to filter on.
   */
  public EventStartDateTimeFilter(LocalDateTime startDateTime) {
    this.startDateTime = Objects.requireNonNull(startDateTime,
      "Start datetime cannot be null");
  }

  @Override
  public boolean test(EventObject event) {
    return event.getStartDateTime().equals(this.startDateTime);
  }
}

