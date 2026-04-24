package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Manages the creation and generation of recurring event series.
 * This class handles the logic for creating multiple SeriesEvent instances
 * based on recurrence patterns.
 */
public class EventSeriesManager {
  private final String seriesId;
  private final String subject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final LocalDate firstDate;
  private final Set<DayOfWeek> repeatDays;
  private final Integer occurences;
  private final LocalDate untilDate;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Private constructor - instances are created through the Builder pattern.
   *
   * @param builder the builder containing all series properties
   */
  private EventSeriesManager(Builder builder) {
    this.seriesId = UUID.randomUUID().toString();
    this.subject = builder.subject;
    this.firstDate = builder.startDateTime.toLocalDate();
    this.startTime = builder.startDateTime.toLocalTime();
    this.endTime = builder.endDateTime != null ? builder.endDateTime.toLocalTime() : null;
    this.repeatDays = new HashSet<>(builder.repeatDays);
    this.occurences = builder.occurrences;
    this.untilDate = builder.untilDate;
    this.description = builder.description;
    this.location = builder.location;
    this.isPublic = builder.isPublic;
  }

  /**
   * Returns a new Builder instance for constructing EventSeriesManager objects.
   *
   * @return a new Builder instance
   */
  public static Builder getBuilder() {
    return new Builder();
  }

  /**
   * Generates all events in the series based on the configured recurrence pattern.
   *
   * @return a list of SeriesEvent instances representing all occurrences
   */
  public List<SeriesEvent> generateAllEvents() {
    List<SeriesEvent> events = new ArrayList<>();
    LocalDate currentDate = firstDate;
    int count = 0;

    while (shouldContinue(currentDate, count)) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        SeriesEvent event = createEventForDate(currentDate);
        events.add(event);
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }
    return events;
  }

  /**
   * Generates events in the series that fall within the specified date range.
   *
   * @param startDate the start of the range (inclusive)
   * @param endDate   the end of the range (inclusive)
   * @return a list of SeriesEvent instances within the range
   */
  public List<SeriesEvent> generateEventsInRange(LocalDate startDate, LocalDate endDate) {
    List<SeriesEvent> events = new ArrayList<>();
    LocalDate currentDate = startDate.isBefore(firstDate) ? firstDate : startDate;
    int count = countEventsBeforeDate(currentDate);

    while (shouldContinue(currentDate, count) && !currentDate.isAfter(endDate)) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        SeriesEvent event = createEventForDate(currentDate);
        events.add(event);
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }
    return events;
  }

  /**
   * Determines whether to continue generating events based on the recurrence pattern.
   *
   * @param currentDate the current date being evaluated
   * @param count       the number of events generated so far
   * @return true if more events should be generated, false otherwise
   */
  private boolean shouldContinue(LocalDate currentDate, int count) {
    if (occurences != null) {
      return count < occurences;
    } else {
      return !currentDate.isAfter(untilDate);
    }
  }

  /**
   * Counts the number of events that would occur before the specified date.
   *
   * @param date the date to count events before
   * @return the number of events before the date
   */
  private int countEventsBeforeDate(LocalDate date) {
    int count = 0;
    LocalDate current = firstDate;
    while (current.isBefore(date)) {
      if (repeatDays.contains(current.getDayOfWeek())) {
        count++;
      }
      current = current.plusDays(1);
    }
    return count;
  }

  /**
   * Creates a SeriesEvent instance for the specified date.
   *
   * @param date the date for the event
   * @return a new SeriesEvent instance
   */
  private SeriesEvent createEventForDate(LocalDate date) {
    LocalDateTime start = date.atTime(startTime);
    LocalDateTime end = endTime != null ? date.atTime(endTime) : null;

    return new SeriesEvent(subject, start, end, description, location, isPublic,
        seriesId);
  }

  /**
   * Gets the unique identifier for this series.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Builder class for constructing EventSeriesManager instances.
   * This builder ensures all required fields are set and validates
   * the series properties before construction.
   */
  public static class Builder {
    private String subject;
    private String description = "";
    private String location = "";
    private boolean isPublic = true;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Set<DayOfWeek> repeatDays = new HashSet<>();
    private Integer occurrences;
    private LocalDate untilDate;

    private Builder() {
    }

    /**
     * Sets the subject for all events in the series.
     *
     * @param subject the event subject (required)
     * @return this builder instance for method chaining
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the description for all events in the series.
     *
     * @param description the event description (optional)
     * @return this builder instance for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location for all events in the series.
     *
     * @param location the event location (optional)
     * @return this builder instance for method chaining
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the visibility for all events in the series.
     *
     * @param isPublic true for public events, false for private
     * @return this builder instance for method chaining
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Sets the start date and time for the first event and the time for all events.
     *
     * @param startDateTime the start date and time (required)
     * @return this builder instance for method chaining
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date and time for all events in the series.
     *
     * @param endDateTime the end date and time (optional)
     * @return this builder instance for method chaining
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the days of the week on which the series repeats.
     *
     * @param repeatDays the set of weekdays (required)
     * @return this builder instance for method chaining
     */
    public Builder repeatDays(Set<DayOfWeek> repeatDays) {
      this.repeatDays = repeatDays;
      return this;
    }

    /**
     * Sets the days of the week on which the series repeats.
     *
     * @param days the weekdays on which to repeat
     * @return this builder instance for method chaining
     */
    public Builder repeatOn(DayOfWeek... days) {
      this.repeatDays.addAll(Arrays.asList(days));
      return this;
    }

    /**
     * Sets the number of occurrences for the series.
     *
     * @param occurrences the number of times to repeat (mutually exclusive with untilDate)
     * @return this builder instance for method chaining
     */
    public Builder occurrences(Integer occurrences) {
      this.occurrences = occurrences;
      return this;
    }

    /**
     * Sets the recurring days from a weekday string representation.
     *
     * @param weekdayString string containing weekday characters (M,T,W,R,F,S,U)
     * @return this builder instance for method chaining
     */
    public Builder recurringDays(String weekdayString) {
      Set<DayOfWeek> days = new HashSet<>();
      for (char c : weekdayString.toCharArray()) {
        days.add(charToDayOfWeek(c));
      }
      this.repeatDays = days;
      return this;
    }

    /**
     * Converts a character to its corresponding day of week.
     *
     * @param c the character representing a weekday
     * @return the corresponding DayOfWeek
     * @throws IllegalArgumentException if the character is invalid
     */
    private DayOfWeek charToDayOfWeek(char c) {
      switch (c) {
        case 'M':
          return DayOfWeek.MONDAY;
        case 'T':
          return DayOfWeek.TUESDAY;
        case 'W':
          return DayOfWeek.WEDNESDAY;
        case 'R':
          return DayOfWeek.THURSDAY;
        case 'F':
          return DayOfWeek.FRIDAY;
        case 'S':
          return DayOfWeek.SATURDAY;
        case 'U':
          return DayOfWeek.SUNDAY;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }

    /**
     * Sets the end date for the series.
     *
     * @param untilDate the last date on which the series can occur (mutually exclusive
     *                  with occurrences)
     * @return this builder instance for method chaining
     */
    public Builder untilDate(LocalDate untilDate) {
      this.untilDate = untilDate;
      return this;
    }

    /**
     * Builds and returns an EventSeriesManager instance with the configured properties.
     *
     * @return a new EventSeriesManager instance
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public EventSeriesManager build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }
      if (startDateTime == null) {
        throw new IllegalArgumentException("startDateTime cannot be null");
      }
      if (repeatDays.isEmpty()) {
        throw new IllegalArgumentException("must specify at least one day");
      }
      if (occurrences == null && untilDate == null) {
        throw new IllegalArgumentException("must specify either until date or occurrences");
      }
      if (occurrences != null && untilDate != null) {
        throw new IllegalArgumentException("cannot specify both occurrences and until date");
      }
      if (occurrences != null && occurrences <= 0) {
        throw new IllegalArgumentException("Occurrences must be greater than zero");
      }
      if (untilDate != null && untilDate.isBefore(startDateTime.toLocalDate())) {
        throw new IllegalArgumentException("Until date cannot be before start date");
      }
      if (endDateTime != null && !endDateTime.isAfter(startDateTime)) {
        throw new IllegalArgumentException("End time must be after start date");
      }

      if (endDateTime != null && !startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
        throw new IllegalArgumentException("Series events must start and end on the same day");
      }
      return new EventSeriesManager(this);


    }
  }


}
