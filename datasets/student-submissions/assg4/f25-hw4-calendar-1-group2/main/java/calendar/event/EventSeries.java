package calendar.event;

import calendar.enums.EventStatus;
import calendar.model.EventKey;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class represents a series of recurring calendar events,
 * and generates the individual event keys based on weekdays, occurrences/end date.
 */
public class EventSeries implements EventInterface {
  private final String seriesId;
  private final String subject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final String weekdays;
  private final Integer occurrences;
  private final LocalDate endDate;
  private final LocalDate firstOccurrence;
  private String description;
  private String location;
  private EventStatus status;
  private final boolean isAllDay;
  private static final LocalTime START_TIME = LocalTime.of(8, 0);
  private static final LocalTime END_TIME = LocalTime.of(17, 0);

  private final Set<EventKey> eventKeys;
  private final Set<LocalDate> removeDates;

  /**
   * Constructor used by the EventSeriesBuilder for initializing.
   *
   * @param builder the builder instance containing given details
   */
  private EventSeries(EventSeriesBuilder builder) {
    if (builder.existingSeriesId != null) {
      this.seriesId = builder.existingSeriesId;
    } else {
      this.seriesId = UUID.randomUUID().toString();
    }
    this.subject = builder.subject;
    this.weekdays = builder.weekdays;
    this.occurrences = builder.occurrences;
    this.endDate = builder.endDate;
    this.firstOccurrence = builder.firstOccurrence;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.isAllDay = builder.isAllDay;
    this.eventKeys = new HashSet<>();
    this.removeDates = new HashSet<>(builder.existingRemovedDates);
    if (this.isAllDay) {
      this.startTime = START_TIME;
      this.endTime = END_TIME;
    } else {
      this.startTime = builder.startTime;
      this.endTime = builder.endTime;
    }
    generateKeys();
  }

  /**
   * Factory method to obtain a new builder instance.
   *
   * @param subject  the subject of the event series
   * @param start    the start date and time of the first occurrence
   * @param weekdays a string representing the recurrence days (e.g., "MWR")
   * @return a new EventSeriesBuilder instance
   */
  public static EventSeriesBuilder builder(String subject, LocalDateTime start, String weekdays) {
    return new EventSeriesBuilder(subject, start, weekdays);
  }

  /**
   * Creates a new builder initialized with the current EventSeries's data.
   * For modifying an existing series.
   *
   * @return a EventSeriesBuilder with given details
   */
  public EventSeriesBuilder toBuilder() {
    EventSeriesBuilder builder = new EventSeriesBuilder(
        this.subject,
        LocalDateTime.of(this.firstOccurrence, this.startTime),
        this.weekdays);
    if (this.isAllDay) {
      builder.setAllDay();
    } else if (this.endTime != null) {
      builder.end(LocalDateTime.of(this.firstOccurrence, this.endTime));
    }

    builder.description(this.description)
        .location(this.location)
        .status(this.status == EventStatus.PRIVATE ? "private" : "public")
        .withExistingId(this.seriesId)
        .withExistingRemovedDates(this.removeDates);

    if (this.occurrences != null) {
      builder.occurrences(this.occurrences);
    } else if (this.endDate != null) {
      builder.setEndDate(this.endDate);
    }
    return builder;
  }

  /**
   * Builder class for creating and reset instances.
   */
  public static class EventSeriesBuilder {
    private String subject;
    private LocalTime startTime;
    private LocalDate firstOccurrence;
    private String weekdays;
    private LocalTime endTime;
    private Integer occurrences;
    private LocalDate endDate;
    private String description;
    private String location;
    private EventStatus status = EventStatus.PUBLIC;
    private boolean isAllDay = false;
    private String existingSeriesId = null;
    private Set<LocalDate> existingRemovedDates = new HashSet<>();

    /**
     * Constructs a new builder with mandatory parameters.
     *
     * @param subject  the subject of the series
     * @param start    the start date and time of the first occurrence
     * @param weekdays a string representing the recurrence days
     */
    public EventSeriesBuilder(String subject, LocalDateTime start, String weekdays) {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }
      if (start == null) {
        throw new IllegalArgumentException("Start time cannot be null");
      }
      if (weekdays == null || weekdays.trim().isEmpty()) {
        throw new IllegalArgumentException("Weekdays cannot be null or empty");
      }
      if (!isValidWeekdays(weekdays)) {
        throw new IllegalArgumentException("Invalid weekdays provided");
      }
      this.subject = subject.trim();
      this.startTime = start.toLocalTime();
      this.firstOccurrence = start.toLocalDate();
      this.weekdays = weekdays.toUpperCase();
    }

    /**
     * Sets the end time for the event series.
     *
     * @param end the end date and time
     * @return the builder instance
     */
    public EventSeriesBuilder end(LocalDateTime end) {
      if (end != null) {
        if (end.toLocalTime().isBefore(this.startTime)) {
          throw new IllegalArgumentException("End time cannot be before start time");
        }
        this.endTime = end.toLocalTime();
      }
      return this;
    }

    /**
     * Marks the event series as an all-day event.
     *
     * @return the builder instance.
     */
    public EventSeriesBuilder setAllDay() {
      this.isAllDay = true;
      this.endTime = null;
      return this;
    }

    /**
     * Sets an existing series ID for updating an existing series.
     *
     * @param seriesId the existing unique ID of the series
     * @return the builder instance
     */
    public EventSeriesBuilder withExistingId(String seriesId) {
      this.existingSeriesId = seriesId;
      return this;
    }

    /**
     * Includes a set of dates that have been removed from the series.
     *
     * @param removedDates the set of removed dates
     * @return the builder instance
     */
    public EventSeriesBuilder withExistingRemovedDates(Set<LocalDate> removedDates) {
      this.existingRemovedDates = new HashSet<>(removedDates);
      return this;
    }

    /**
     * Sets the maximum number of occurrences for the series.
     *
     * @param occurrences the number of times the event occurs
     * @return the builder instance
     */
    public EventSeriesBuilder occurrences(Integer occurrences) {
      if (occurrences <= 0) {
        throw new IllegalArgumentException("Occurrences must be greater than zero");
      }
      this.occurrences = occurrences;
      this.endDate = null;
      return this;
    }

    /**
     * Sets the end date of the event series.
     *
     * @param endDate the end date of the series
     * @return the builder instance
     */
    public EventSeriesBuilder setEndDate(LocalDate endDate) {
      if (endDate == null) {
        throw new IllegalArgumentException("End date cannot be null");
      }
      if (endDate.isBefore(firstOccurrence)) {
        throw new IllegalArgumentException("End date cannot be before first occurrence");
      }
      this.endDate = endDate;
      this.occurrences = null;
      return this;
    }

    /**
     * Sets the descriptive text for the event series.
     *
     * @param description the description
     * @return the builder instance
     */
    public EventSeriesBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location for the event series.
     *
     * @param location the location string
     * @return the builder instance
     */
    public EventSeriesBuilder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status of the event series.
     *
     * @param status the status string ("private" or "public")
     * @return the builder instance
     */
    public EventSeriesBuilder status(String status) {
      if (status == null || status.trim().isEmpty()) {
        return this;
      }
      if (status.equals("private")) {
        this.status = EventStatus.PRIVATE;
      } else {
        this.status = EventStatus.PUBLIC;
      }
      return this;
    }

    /**
     * Builds and returns the event series object.
     *
     * @return an EventSeries instance.
     */
    public EventSeries build() {
      if (occurrences == null && endDate == null) {
        throw new IllegalArgumentException("Must input either occurrences or end date");
      }
      if (!isAllDay && endTime == null) {
        throw new IllegalArgumentException("Must be all-day event or input end date");
      }
      return new EventSeries(this);
    }

    /**
     * Internal helper to validate the weekdays string.
     *
     * @param weekdays the string to validate
     * @return true if all characters are valid (M, T, W, R, F, S, U)
     */
    private boolean isValidWeekdays(String weekdays) {
      if (weekdays == null || weekdays.trim().isEmpty()) {
        throw new IllegalArgumentException("Weekdays cannot be null or empty");
      }
      String validWeekdays = "MTWRFSU";
      for (char c : weekdays.toUpperCase().toCharArray()) {
        if (validWeekdays.indexOf(c) == -1) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Generates the set of EventKey objects for all events in the series.
   *
   * @return an unmodifiable set of EventKeys
   * @throws IllegalArgumentException if no events are found
   */
  public Set<EventKey> generateKeys() {
    this.eventKeys.clear();
    Set<DayOfWeek> targetDays = parseWeekdays(weekdays);
    LocalDate currentDate = this.firstOccurrence;
    int count = 0;
    int maxDays = 3650;
    int daysSearched = 0;
    LocalTime currentTime = this.startTime;
    LocalTime endTime = this.endTime;

    while (daysSearched < maxDays) {
      if (occurrences != null && count >= occurrences) {
        break;
      }
      if (endDate != null && currentDate.isAfter(endDate)) {
        break;
      }
      if (targetDays.contains(currentDate.getDayOfWeek())) {
        if (!removeDates.contains(currentDate)) {
          LocalDateTime startDate = LocalDateTime.of(currentDate, currentTime);
          LocalDateTime endDate = LocalDateTime.of(currentDate, endTime);
          EventKey eventKey = new EventKey(subject, startDate, endDate);
          this.eventKeys.add(eventKey);
        }
        count++;
      }
      currentDate = currentDate.plusDays(1);
      daysSearched++;
    }
    if (this.eventKeys.isEmpty()) {
      throw new IllegalArgumentException("No events found");
    }
    return Collections.unmodifiableSet(this.eventKeys);
  }

  /**
   * Marks a date removed from key generation.
   *
   * @param date the date to be removed from the series
   */
  public void markDateRemoved(LocalDate date) {
    this.removeDates.add(date);
  }

  /**
   * Parses the weekday string (e.g., "MWR") into a Set of DayOfWeek enums.
   *
   * @param weekdays the string representation of weekdays
   * @return a Set of DayOfWeek enums
   */
  private Set<DayOfWeek> parseWeekdays(String weekdays) {
    Set<DayOfWeek> targetDays = new HashSet<>();
    for (char c : weekdays.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          targetDays.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          targetDays.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          targetDays.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          targetDays.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          targetDays.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          targetDays.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          targetDays.add(DayOfWeek.SUNDAY);
          break;
        default:
          break;
      }
    }
    return targetDays;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Get a copy of the current set of event keys.
   *
   * @return a copy of the set of EventKeys
   */
  public Set<EventKey> getEventKeys() {
    return new HashSet<>(eventKeys);
  }

  /**
   * Removes an EventKey from the series keys.
   *
   * @param eventKey the key to remove
   */
  public void removeEventKey(EventKey eventKey) {
    this.eventKeys.remove(eventKey);
  }

  /**
   * Retrieves the set of keys for this series.
   *
   * @return an unmodifiable set of EventKey instances
   */
  public Set<EventKey> getSeriesKeys() {
    return Collections.unmodifiableSet(this.eventKeys);
  }

  /**
   * Retrieves the set of dates that have been removed.
   *
   * @return an unmodifiable set of removed dates
   */
  public Set<LocalDate> getRemovedDates() {
    return Collections.unmodifiableSet(removeDates);
  }

  /**
   * Retrieves the date of the first event in the series.
   *
   * @return the first occurrence date
   */
  public LocalDate getFirstOccurrence() {
    return firstOccurrence;
  }

  /**
   * Retrieves the recurrence string (e.g., "MWR").
   *
   * @return a weekday string.
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Retrieves the number of occurrences of this series.
   *
   * @return the number of occurrences
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Retrieves the end date of this series.
   *
   * @return the end date
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Checks if the series is defined as an all-day event.
   *
   * @return true if the event series is all-day
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return LocalDateTime.of(firstOccurrence, startTime);
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return LocalDateTime.of(firstOccurrence, endTime);
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public EventStatus getEventStatus() {
    return status;
  }

  @Override
  public void editDescription(String newDescription) {
    this.description = newDescription;
  }

  @Override
  public void editLocation(String newLocation) {
    this.location = newLocation;
  }

  @Override
  public void editEventStatus(String newEventStatus) {
    if (newEventStatus.equals("private")) {
      this.status = EventStatus.PRIVATE;
    } else {
      this.status = EventStatus.PUBLIC;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(seriesId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventSeries that = (EventSeries) o;
    return Objects.equals(seriesId, that.seriesId);
  }
}
