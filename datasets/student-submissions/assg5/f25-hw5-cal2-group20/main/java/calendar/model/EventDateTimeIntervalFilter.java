package calendar.model;

import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * Represents a predicate class for filtering events based on the provided datetime interval. If the
 * event is partially or fully in the interval, then it satisfies the requirement.
 */
public class EventDateTimeIntervalFilter implements Predicate<EventObject> {
  private final LocalDateTime startEventDateTime;
  private final LocalDateTime endEventDateTime;

  /**
   * Constructs an event datetime interval filter.
   *
   * @param startEventDateTime the start date time of the interval.
   * @param endEventDateTime the end date time of the interval.
   */
  public EventDateTimeIntervalFilter(LocalDateTime startEventDateTime,
                                     LocalDateTime endEventDateTime) {
    this.startEventDateTime = startEventDateTime;
    this.endEventDateTime = endEventDateTime;
  }

  @Override
  public boolean test(EventObject event) {
    return event.getEndDateTime().isAfter(startEventDateTime)
      && event.getStartDateTime().isBefore(endEventDateTime);
  }
}
