package calendar.model;

import java.time.ZonedDateTime;

/**
 * Represents different recurrence patterns for events.
 */
public enum RecurrencePattern {
  DAILY {
    @Override
    public ZonedDateTime getNext(ZonedDateTime current) {
      return current.plusDays(1);
    }
  },
  WEEKLY {
    @Override
    public ZonedDateTime getNext(ZonedDateTime current) {
      return current.plusWeeks(1);
    }
  },
  MONTHLY {
    @Override
    public ZonedDateTime getNext(ZonedDateTime current) {
      return current.plusMonths(1);
    }
  },
  YEARLY {
    @Override
    public ZonedDateTime getNext(ZonedDateTime current) {
      return current.plusYears(1);
    }
  };

  /**
   * Gets the next occurrence after the given time.
   */
  public abstract ZonedDateTime getNext(ZonedDateTime current);
}
