package calendar.model;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recurrence rule defining weekly repetition with either a count or an until date.
 */
public class RecurrenceRule {

  private final List<DayOfWeek> days;
  private final int count;
  private final String untilDate;
  private final boolean isCountBased;
  private final boolean isUntilBased;

  /**
   * Creates a count-based rule on specific days.
   *
   * @param days  days of the week
   * @param count number of occurrences
   */
  public RecurrenceRule(List<DayOfWeek> days, int count) {
    this.days = new ArrayList<>(days);
    this.count = count;
    this.untilDate = null;
    this.isCountBased = true;
    this.isUntilBased = false;
  }

  /**
   * Creates an until-based rule on specific days.
   *
   * @param days      days of the week
   * @param untilDate date like YYYY-MM-DD
   */
  public RecurrenceRule(List<DayOfWeek> days, String untilDate) {
    this.days = new ArrayList<>(days);
    this.count = 0;
    this.untilDate = untilDate;
    this.isCountBased = false;
    this.isUntilBased = true;
  }

  /**
   * Returns the days on which the event repeats.
   *
   * @return list of days
   */
  public List<DayOfWeek> getDays() {
    return Collections.unmodifiableList(days);
  }

  /**
   * Returns the number of occurrences if count based.
   *
   * @return count value
   */
  public int getCount() {
    return count;
  }

  /**
   * Returns the until date string if until based.
   *
   * @return ISO date string
   */
  public String getUntilDate() {
    return untilDate;
  }

  /**
   * True when this rule is limited by count.
   *
   * @return true for count-based
   */
  public boolean isCountBased() {
    return isCountBased;
  }

  /**
   * True when this rule is limited by until date.
   *
   * @return true for until-based
   */
  public boolean isUntilBased() {
    return isUntilBased;
  }
}
