package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

/**
 * Class that represents the rules to be followed for a recurring series of events.
 */
public class RecurrenceRule {
  private final Set<DayOfWeek> daysOfWeek;
  private final int occurences;
  private final LocalDate endDate;

  /**
   * Private constructor that uses a builder to construct an instance of recurring series.
   *
   * @param builder Builder object that is used to initialize the variables of this class
   */
  private RecurrenceRule(Builder builder) {
    this.daysOfWeek = Collections.unmodifiableSet(builder.daysOfWeek);
    this.occurences = builder.occurences;
    this.endDate = builder.endDate;
  }

  /**
   * Getter method to get the days of the week on which the event is repeating.
   *
   * @return set of days of week
   */
  public  Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Getter method to get the total number of occurences for the series.
   *
   * @return the number of occurences
   */
  public int getOccurences() {
    return occurences;
  }

  /**
   * Getter method to get the end date for the series.
   *
   * @return end date
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Boolean Method to check if the recurring series is defined by a fixed number of occurences
   * or not.
   *
   * @return true is the recurring series is rules by count
   */
  public boolean isCountBased() {
    return occurences > 0;
  }

  /**
   * Static builder class to create and instantiate objects of RecurrenceRule class.
   */
  public static class Builder {
    private Set<DayOfWeek> daysOfWeek;
    private int occurences = 0;
    private LocalDate endDate = null;

    /**
     * Constructor to construct a builder object when days of week for recurrence is passed as
     * an argument.
     *
     * @param daysOfWeek set of days on which the event repeats
     * @throws IllegalArgumentException when no value is passed to the constructor
     */
    public Builder(Set<DayOfWeek> daysOfWeek) throws IllegalArgumentException {
      if (daysOfWeek == null || daysOfWeek.isEmpty()) {
        throw new IllegalArgumentException("Recurrence must specify days of the week.");
      }
      this.daysOfWeek = daysOfWeek;
    }

    /**
     * Constructor to construct a builder object when the fixed number of occurences for repetition
     * is passed as an argument.
     *
     * @param occurences the fixed number of times an event repeats
     * @return Builder object
     * @throws IllegalArgumentException when non-positive repetition count is entered or
     *                                  end date of the event is null
     */
    public Builder repeatsFor(int occurences) throws IllegalArgumentException {
      if (occurences <= 0) {
        throw new IllegalArgumentException("Occurences count must be positive.");
      }
      if (this.endDate != null) {
        throw new IllegalArgumentException("Cannot set both occurences and end date.");
      }
      this.occurences = occurences;
      return this;
    }

    /**
     * Constructor to construct a builder object when end date is passed as an argument.
     *
     * @param endDate end date of the recurrence
     * @return Builder object
     * @throws IllegalArgumentException when no end date is passed or if the recurrence is also
     *                                  ruled by number of occurences
     */
    public Builder repeatsUntil(LocalDate endDate) throws IllegalArgumentException {
      if (endDate == null) {
        throw new IllegalArgumentException("End date cannot be null.");
      }
      if (this.occurences > 0) {
        throw new IllegalArgumentException("Cannot set both occurences and end date.");
      }
      this.endDate = endDate;
      return this;
    }

    /**
     * Method to build the RecurrenceRule object using the builder.
     *
     * @return new RecurrenceRule object
     * @throws IllegalStateException when neither the occurences nor end date for recurrence are set
     */
    public RecurrenceRule build() throws IllegalStateException {
      if (occurences <= 0 && endDate == null) {
        throw new IllegalStateException("Recurrence must be defined by occurrences or end date.");
      }
      return new RecurrenceRule(this);
    }
  }
}