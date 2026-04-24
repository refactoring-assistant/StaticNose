package calendar.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a predicate class for filtering events based on the start date.
 */
public class EventStartDateFilter implements Predicate<EventObject> {
  private final LocalDate startDate;

  /**
   * Constructs a filter based on the given event start date.
   *
   * @param startDate the start date to filter on.
   */
  public EventStartDateFilter(LocalDate startDate) {
    this.startDate = Objects.requireNonNull(startDate,
      "Start date cannot be null");
  }

  @Override
  public boolean test(EventObject event) {
    return event.getStartDateTime().toLocalDate().equals(this.startDate);
  }
}
