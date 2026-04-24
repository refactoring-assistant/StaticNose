package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks series of repeating events.
 * Maintains information about which events a series contains.
 * Handles generation and editing of repeating events.
 */
public class RecurringEventSeries implements EventSeries {
  private final String seriesId;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final List<DayOfWeek> weekdays;
  private final Integer repeatCount;
  private final LocalDate repeatUntil;
  private final String description;
  private final String location;
  private final Status status;
  private List<Event> eventsList;

  /**
   * Private constructor to create a recurring event series.
   *
   * @param builder the builder used to construct the event series
   */
  private RecurringEventSeries(EventSeriesBuilder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.weekdays = new ArrayList<>(builder.weekdays);
    this.repeatCount = builder.repeatCount;
    this.repeatUntil = builder.repeatUntil;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = UUID.randomUUID().toString();
    generateEvents();
  }

  /**
   * Generates all events for this series based on the repeat count or repeat until date.
   */
  @Override
  public void generateEvents() {
    if (repeatUntil == null) {
      generateEventsByCount();
    } else {
      generateEventsByRepeatUntil();
    }
  }

  private void generateEventsByCount() {
    eventsList = new ArrayList<>();
    LocalDate currentDate = startDateTime.toLocalDate();
    int count = 0;

    while (count < repeatCount) {
      for (DayOfWeek dayofWeek : weekdays) {
        if (count >= repeatCount) {
          break;
        }

        LocalDate eventDate = getDateForDayInWeek(currentDate, dayofWeek);

        if (!eventDate.isBefore(startDateTime.toLocalDate())) {
          CalendarEvent event = createEvent(eventDate);
          eventsList.add(event);
          count++;
        }
      }
      currentDate = currentDate.plusWeeks(1);
    }
  }

  private void generateEventsByRepeatUntil() {
    eventsList = new ArrayList<>();
    LocalDate currentDate = startDateTime.toLocalDate();

    while (!currentDate.isAfter(repeatUntil)) {
      for (DayOfWeek dayofWeek : weekdays) {
        LocalDate eventDate = getDateForDayInWeek(currentDate, dayofWeek);

        if (!eventDate.isAfter(repeatUntil) && !eventDate.isBefore(startDateTime.toLocalDate())) {
          CalendarEvent event = createEvent(eventDate);
          eventsList.add(event);
        }
      }
      currentDate = currentDate.plusWeeks(1);
    }
  }

  private LocalDate getDateForDayInWeek(LocalDate currentDate, DayOfWeek dayofWeek) {
    return currentDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(dayofWeek));
  }

  private CalendarEvent createEvent(LocalDate date) {
    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    LocalDateTime start = LocalDateTime.of(date, startTime);
    LocalDateTime end = LocalDateTime.of(date, endTime);

    return new CalendarEvent.EventBuilder()
        .subject(subject)
        .startDateTime(start)
        .endDateTime(end)
        .description(description)
        .location(location)
        .status(status)
        .seriesId(seriesId)
        .build();
  }

  /**
   * Gets the series id for this recurring event series.
   *
   * @return the unique series ID
   */
  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  @Override
  public List<DayOfWeek> getWeekdays() {
    return weekdays;
  }

  @Override
  public Integer getRepeatCount() {
    return repeatCount;
  }

  @Override
  public LocalDate getRepeatUntil() {
    return repeatUntil;
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
  public Status getStatus() {
    return status;
  }

  /**
   * Gets all the events in this recurring event series.
   *
   * @return a list of all events in the series
   */
  @Override
  public List<Event> getAllEvents() {
    return eventsList;
  }

  @Override
  public List<Event> getEventsStartingFrom(Event fromEvent) {
    List<Event> result = new ArrayList<>();
    LocalDateTime fromStart = fromEvent.getStartDateTime();

    for (Event e : eventsList) {
      if (!e.getStartDateTime().isBefore(fromStart)) {
        result.add(e);
      }
    }
    return result;
  }

  /**
   * Builder class for creating a RecurringEventSeries.
   */
  public static class EventSeriesBuilder extends AbstractEventBuilder<EventSeriesBuilder> {
    private List<DayOfWeek> weekdays;
    private Integer repeatCount;
    private LocalDate repeatUntil;

    /**
     * Sets the weekdays on which this event series repeats.
     *
     * @param weekdays the set of days of the week for the series
     * @return this builder for method chaining
     */
    public EventSeriesBuilder weekdays(Set<DayOfWeek> weekdays) {
      this.weekdays = new ArrayList<>(weekdays);
      this.weekdays.sort(Comparator.comparingInt(DayOfWeek::getValue));
      return this;
    }

    /**
     * Sets the number of times this event series will repeat.
     *
     * @param repeatCount the number of times the event series should repeat
     * @return this builder for method chaining
     * @throws IllegalArgumentException if repeatCount is null or less than 1
     */
    public EventSeriesBuilder repeatCount(Integer repeatCount) {
      if (repeatCount == null) {
        throw new IllegalArgumentException("repeatCount cannot be null");
      } else if (repeatCount < 1) {
        throw new IllegalArgumentException("repeatCount must be positive");
      }
      this.repeatCount = repeatCount;
      return this;
    }

    /**
     * Sets the date until which the event series will repeat.
     *
     * @param repeatUntil the last date the event series should repeat
     * @return this builder for method chaining
     */
    public EventSeriesBuilder repeatUntil(LocalDate repeatUntil) {
      this.repeatUntil = repeatUntil;
      return this;
    }

    /**
     * Builds and returns a new RecurringEventSeries instance.
     * Performs necessary validation before building the series.
     *
     * @return a new RecurringEventSeries instance
     * @throws IllegalArgumentException if validation fails
     */
    public RecurringEventSeries build() {
      applyAllDayCheck();
      validate();
      validateEventSeriesChecks();
      return new RecurringEventSeries(this);
    }

    private void validateEventSeriesChecks() {
      if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
        throw new IllegalArgumentException(
            "Start and end must be on the same date for a recurring event");
      }
      if ((repeatCount == null || repeatCount < 1) && repeatUntil == null) {
        throw new IllegalArgumentException("Need to specify either RepeatUntil or RepeatCount");
      }
      if (weekdays == null || weekdays.isEmpty()) {
        throw new IllegalArgumentException("Need to specify the weekdays the event should repeat");
      }
      if (repeatUntil != null) {
        if (!repeatUntil.isAfter(endDateTime.toLocalDate())) {
          throw new IllegalArgumentException("repeatUntil must be after the event's end date");
        }
      }
    }
  }
}
