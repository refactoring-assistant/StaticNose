package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a series of recurring events with Builder pattern for flexible configuration.
 * EventSeries objects are immutable once created.
 */
public class EventSeries {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final List<DayOfWeek> weekdays;
  private final Integer occurrences;
  private final LocalDate untilDate;
  private final boolean allDay;
  private final String location;
  private final String description;
  private final String status;
  private final String seriesId;

  /**
   * Private constructor that takes a Builder.
   */
  private EventSeries(Builder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime != null
        ?
        builder.endDateTime : calculateDefaultEndTime(builder.startDateTime, builder.allDay);
    this.weekdays = new ArrayList<>(builder.weekdays);
    this.occurrences = builder.occurrences;
    this.untilDate = builder.untilDate;
    this.allDay = builder.allDay;
    this.location = builder.location;
    this.description = builder.description != null ? builder.description : "";
    this.status = builder.status;
    this.seriesId = builder.seriesId != null ? builder.seriesId : UUID.randomUUID().toString();

    validateSeries();
  }

  /**
   * Validates the event series for consistency and business rules.
   */
  private void validateSeries() {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDateTime == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("At least one weekday must be specified");
    }
    if (occurrences != null && occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    if (untilDate != null && untilDate.isBefore(startDateTime.toLocalDate())) {
      throw new IllegalArgumentException("Until date cannot be before start date");
    }
    if (occurrences != null && untilDate != null) {
      throw new IllegalArgumentException("Cannot specify both occurrences and until date");
    }
    if (occurrences == null && untilDate == null) {
      throw new IllegalArgumentException("Must specify either occurrences or until date");
    }

    if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException("Events in a series cannot span multiple days");
    }
  }

  /**
   * Calculates default end time based on start time and whether it's all-day.
   */
  private LocalDateTime calculateDefaultEndTime(LocalDateTime start, boolean allDay) {
    if (allDay) {
      return start.withHour(17).withMinute(0).withSecond(0).withNano(0);
    } else {
      return start.plusHours(1);
    }
  }

  /**
   * Builder class for constructing EventSeries objects.
   */
  public static class Builder {
    private final String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<DayOfWeek> weekdays = new ArrayList<>();
    private Integer occurrences;
    private LocalDate untilDate;
    private boolean allDay = false;
    private String location;
    private String description;
    private String status = "public";
    private String seriesId;

    /**
     * Creates a new EventSeries builder with required subject.
     *
     * @param subject the series subject (required)
     */
    public Builder(String subject) {
      this.subject = subject;
    }

    /**
     * Sets the start date and time for the series.
     *
     * @param startDateTime the start date and time
     * @return this builder for method chaining
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date and time for each event in the series.
     *
     * @param endDateTime the end date and time
     * @return this builder for method chaining
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the series for all-day events on the specified date.
     *
     * @param date the date for all-day events
     * @return this builder for method chaining
     */
    public Builder allDayOn(LocalDate date) {
      this.allDay = true;
      this.startDateTime = date.atTime(8, 0);
      this.endDateTime = date.atTime(17, 0);
      return this;
    }

    /**
     * Sets custom all-day hours.
     *
     * @param date      the date
     * @param startHour start hour (0-23)
     * @param endHour   end hour (0-23)
     * @return this builder for method chaining
     */
    public Builder allDayOn(LocalDate date, int startHour, int endHour) {
      this.allDay = true;
      this.startDateTime = date.atTime(startHour, 0);
      this.endDateTime = date.atTime(endHour, 0);
      return this;
    }

    /**
     * Adds a weekday to the recurrence pattern.
     *
     * @param weekday the day of week
     * @return this builder for method chaining
     */
    public Builder onWeekday(DayOfWeek weekday) {
      this.weekdays.add(weekday);
      return this;
    }

    /**
     * Sets weekdays from a string pattern (e.g., "MWF", "MTWRF").
     *
     * @param weekdayString string containing weekday codes
     * @return this builder for method chaining
     */
    public Builder onWeekdays(String weekdayString) {
      this.weekdays.clear();
      for (char c : weekdayString.toUpperCase().toCharArray()) {
        switch (c) {
          case 'M':
            this.weekdays.add(DayOfWeek.MONDAY);
            break;
          case 'T':
            this.weekdays.add(DayOfWeek.TUESDAY);
            break;
          case 'W':
            this.weekdays.add(DayOfWeek.WEDNESDAY);
            break;
          case 'R':
            this.weekdays.add(DayOfWeek.THURSDAY);
            break;
          case 'F':
            this.weekdays.add(DayOfWeek.FRIDAY);
            break;
          case 'S':
            this.weekdays.add(DayOfWeek.SATURDAY);
            break;
          case 'U':
            this.weekdays.add(DayOfWeek.SUNDAY);
            break;
          default:
            throw new IllegalArgumentException("Invalid weekday code: " + c);
        }
      }
      return this;
    }

    /**
     * Sets weekdays from a list.
     *
     * @param weekdays list of weekdays
     * @return this builder for method chaining
     */
    public Builder onWeekdays(List<DayOfWeek> weekdays) {
      this.weekdays = new ArrayList<>(weekdays);
      return this;
    }

    /**
     * Sets the number of occurrences for the series.
     *
     * @param occurrences number of events to create
     * @return this builder for method chaining
     */
    public Builder forOccurrences(int occurrences) {
      this.occurrences = occurrences;
      this.untilDate = null;
      return this;
    }

    /**
     * Sets the end date for the series (alternative to occurrences).
     *
     * @param untilDate the last possible date for events
     * @return this builder for method chaining
     */
    public Builder untilDate(LocalDate untilDate) {
      this.untilDate = untilDate;
      this.occurrences = null;
      return this;
    }

    /**
     * Sets the location for all events in the series.
     *
     * @param location the event location
     * @return this builder for method chaining
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the description for all events in the series.
     *
     * @param description the event description
     * @return this builder for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the status for all events in the series.
     *
     * @param status the event status ("public" or "private")
     * @return this builder for method chaining
     */
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Sets a custom series ID (auto-generated if not specified).
     *
     * @param seriesId the series identifier
     * @return this builder for method chaining
     */
    public Builder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds and returns the EventSeries with all specified parameters.
     *
     * @return a new immutable EventSeries object
     * @throws IllegalArgumentException if any validation fails
     */
    public EventSeries build() {
      return new EventSeries(this);
    }
  }

  /**
   * Generates the actual Event objects for this series based on the recurrence pattern.
   *
   * @return list of Event objects created from this series
   */
  public List<Event> generateEvents() {
    List<Event> events = new ArrayList<>();

    LocalDate currentDate = startDateTime.toLocalDate();
    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();

    int generatedCount = 0;
    while (shouldContinueGenerating(currentDate, generatedCount)) {
      if (weekdays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentDate, startTime);
        LocalDateTime eventEnd = LocalDateTime.of(currentDate, endTime);

        Event.Builder eventBuilder = new Event.Builder(subject, eventStart)
            .endDateTime(eventEnd)
            .seriesId(seriesId)
            .status(status);

        if (location != null) {
          eventBuilder.location(location);
        }
        if (description != null) {
          eventBuilder.description(description);
        }
        if (allDay) {
          eventBuilder.allDay();
        }

        events.add(eventBuilder.build());
        generatedCount++;
      }

      currentDate = currentDate.plusDays(1);
      if (currentDate.isAfter(startDateTime.toLocalDate().plusYears(2))) {
        throw new IllegalArgumentException(
            "Could not generate series - invalid weekday pattern or date range too large");
      }
    }

    return events;
  }

  /**
   * Determines if we should continue generating events.
   */
  private boolean shouldContinueGenerating(LocalDate currentDate, int generatedCount) {
    if (occurrences != null) {
      return generatedCount < occurrences;
    } else if (untilDate != null) {
      return !currentDate.isAfter(untilDate);
    }
    return false;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public List<DayOfWeek> getWeekdays() {
    return new ArrayList<>(weekdays);
  }

  public Integer getOccurrences() {
    return occurrences;
  }

  public LocalDate getUntilDate() {
    return untilDate;
  }

  public boolean isAllDay() {
    return allDay;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Creates a builder from this event series (for modification).
   */
  public Builder toBuilder() {
    Builder builder = new Builder(subject)
        .startDateTime(startDateTime)
        .endDateTime(endDateTime)
        .onWeekdays(weekdays)
        .location(location)
        .description(description)
        .status(status)
        .seriesId(seriesId);

    if (occurrences != null) {
      builder.forOccurrences(occurrences);
    } else if (untilDate != null) {
      builder.untilDate(untilDate);
    }

    return builder;
  }

  @Override
  public String toString() {
    return "EventSeries{"
        + "subject='" + subject + '\''
        + ", startDateTime=" + startDateTime
        + ", weekdays=" + weekdays
        + ", occurrences=" + occurrences
        + ", untilDate=" + untilDate
        + ", allDay=" + allDay
        + '}';
  }
}