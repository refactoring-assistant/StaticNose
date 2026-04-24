package calendar.service;

import calendar.model.CalendarEvent;
import calendar.model.Event;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Event Series Builder Class.
 */
public class EventSeriesBuilder {

  private String subject;
  private LocalDate startDate;
  private LocalTime startTime;
  private LocalDate endDate;
  private LocalTime endTime;
  private String description = "";
  private String location = "";
  private boolean isPrivate = false;
  private boolean isMonthly = false;
  private Set<DayOfWeek> days = null;
  private LocalDate untilDate = null;
  private Integer occurrences = null;
  private ZoneId zone;

  /**
   * Constructs a new, empty EventSeriesBuilder.
   */
  public EventSeriesBuilder() {
  }

  /**
   * Sets the subject (title) of the event. This is required.
   *
   * @param subject The event subject.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Sets the start date of the event or the first event in a series. This is
   * required.
   *
   * @param date The local start date.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder startDate(LocalDate date) {
    this.startDate = date;
    return this;
  }

  /**
   * Sets the start time of the event.
   * If not provided, defaults to 8:00 AM.
   *
   * @param time The local start time.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder startTime(LocalTime time) {
    this.startTime = time;
    return this;
  }

  /**
   * Sets the end date of the event. For a single event, this can be different
   * from the start date. For a series, this is ignored (series events must
   * start and end on the same day).
   * If not provided for a single event, defaults to the start date.
   *
   * @param date The local end date.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder endDate(LocalDate date) {
    this.endDate = date;
    return this;
  }

  /**
   * Sets the end time of the event.
   * If not provided, defaults to 1 hour after the start time, or 5:00 PM if
   * start time is also not provided.
   *
   * @param time The local end time.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder endTime(LocalTime time) {
    this.endTime = time;
    return this;
  }

  /**
   * Sets the optional description for the event.
   * If null, defaults to an empty string.
   *
   * @param description The event description.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder description(String description) {
    this.description = (description == null) ? "" : description;
    return this;
  }

  /**
   * Sets the optional location for the event.
   * If null, defaults to an empty string.
   *
   * @param location The event location.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder location(String location) {
    this.location = (location == null) ? "" : location;
    return this;
  }

  /**
   * Sets the privacy flag for the event.
   * Defaults to false (public).
   *
   * @param isPrivate True to mark the event as private, false for public.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder isPrivate(boolean isPrivate) {
    this.isPrivate = isPrivate;
    return this;
  }

  /**
   * Configures the event to repeat on specific days of the week.
   * This enables series creation.
   *
   * @param daysStr A string containing day codes (M, T, W, R, F, S, U) or
   *                "MONTHLY".
   * @return This builder instance for chaining.
   * @throws IllegalArgumentException if an invalid day code is provided.
   */
  public EventSeriesBuilder repeats(String daysStr) {
    if (daysStr == null || daysStr.isEmpty()) {
      this.days = null;
      this.isMonthly = false;
      return this;
    }
    if ("MONTHLY".equalsIgnoreCase(daysStr)) {
      this.isMonthly = true;
      this.days = null;
      return this;
    }
    this.isMonthly = false;
    this.days = daysStr.toUpperCase().chars().mapToObj(c -> (char) c).map(c -> {
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
          throw new IllegalArgumentException("Invalid day code: " + c);
      }
    }).collect(Collectors.toSet());
    return this;
  }

  /**
   * Sets the end condition for a repeating series. The series will include all
   * valid occurrences on or before this date.
   * This setting is mutually exclusive with {@link #forTimes(int)}.
   *
   * @param date The last possible date for an event in the series.
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder until(LocalDate date) {
    this.untilDate = date;
    this.occurrences = null;
    return this;
  }

  /**
   * Sets the end condition for a repeating series. The series will contain
   * exactly
   * this many occurrences.
   * This setting is mutually exclusive with {@link #until(LocalDate)}.
   *
   * @param n The total number of occurrences (must be positive).
   * @return This builder instance for chaining.
   */
  public EventSeriesBuilder forTimes(int n) {
    this.occurrences = n;
    this.untilDate = null;
    return this;
  }

  /**
   * Sets the timezone for converting local times to Instants.
   *
   * @param zone The calendar's timezone.
   * @return this builder.
   */
  public EventSeriesBuilder zone(ZoneId zone) {
    this.zone = zone;
    return this;
  }

  /**
   * Creates a single event instance as part of a series, calculating its
   * start/end {@link Instant} values based on the series template.
   *
   * @param template     The base event to copy properties from.
   * @param currentStart The specific {@link LocalDateTime} for this new
   *                     instance's start.
   * @param duration     The duration of the event (calculated from the template).
   * @param seriesId     The unique ID for the entire series.
   * @return A new {@link Event} instance for a specific date in the series.
   */
  private Event createSeriesEvent(Event template, LocalDateTime currentStart, Duration duration,
                                  String seriesId) {
    Instant currentStartInstant = currentStart.atZone(this.zone).toInstant();
    Instant currentEnd = currentStartInstant.plus(duration);

    Event seriesEvent = template.copy();
    seriesEvent.setStart(currentStartInstant);
    seriesEvent.setEnd(currentEnd);
    seriesEvent.setSeriesId(seriesId);
    return seriesEvent;
  }

  /**
   * Builds the list of {@link Event} objects based on the configured parameters.
   * If {@link #repeats(String)} was called, this will generate a list of events
   * according to the recurrence rules (either {@link #forTimes(int)} or
   * {@link #until(LocalDate)}).
   *
   * @return A non-null list of created {@link Event} objects.
   * @throws IllegalArgumentException if required fields (subject, startDate) are
   *                                  missing,
   *                                  if time inputs are invalid, or if recurrence
   *                                  rules
   *                                  are contradictory or incomplete.
   * @throws IllegalStateException    if the {@link #zone(ZoneId)} is not set, as
   *                                  it is
   *                                  required for {@link Instant} conversion.
   */
  public List<Event> build() {

    validateInputs();
    if (this.zone == null) {
      throw new IllegalStateException("ZoneId is required for event creation.");
    }

    LocalTime finalStartTime;
    LocalTime finalEndTime;
    if (this.startTime == null && this.endTime == null) {
      finalStartTime = LocalTime.of(8, 0);
      finalEndTime = LocalTime.of(17, 0);
    } else if (this.startTime != null && this.endTime == null) {
      finalStartTime = this.startTime;
      finalEndTime = this.startTime.plusHours(1);
    } else {
      finalStartTime = this.startTime;
      finalEndTime = this.endTime;
    }
    LocalDate finalEndDate = (this.endDate == null) ? this.startDate : this.endDate;

    LocalDateTime startLdt = this.startDate.atTime(finalStartTime);
    LocalDateTime endLdt = finalEndDate.atTime(finalEndTime);

    Instant startInstant = startLdt.atZone(this.zone).toInstant();
    Instant endInstant = endLdt.atZone(this.zone).toInstant();

    Event template = new CalendarEvent(this.subject, startInstant, endInstant, this.description,
        this.location, this.isPrivate, null);

    boolean isSeries = (this.days != null && !this.days.isEmpty()) || this.isMonthly;

    List<Event> newEvents = new ArrayList<>();
    if (isSeries) {
      buildSeries(template, newEvents, startLdt);
    } else {
      buildSingleEvent(template, newEvents);
    }
    return newEvents;
  }

  private void validateInputs() {
    if (this.subject == null || this.subject.isBlank()) {
      throw new IllegalArgumentException("Subject is required.");
    }
    if (this.startDate == null) {
      throw new IllegalArgumentException("Start date is required.");
    }
    if (this.startTime == null && this.endTime != null) {
      throw new IllegalArgumentException("Cannot specify end time without start time.");
    }
  }

  private void buildSingleEvent(Event template, List<Event> newEvents) {
    if (this.occurrences != null || this.untilDate != null) {
      throw new IllegalArgumentException("Cannot specify 'forTimes' or 'until' for a "
          + "non-repeating event.");
    }

    newEvents.add(template);
  }

  private void buildSeries(Event template, List<Event> newEvents, LocalDateTime localStart) {
    if (this.occurrences == null && this.untilDate == null) {
      throw new IllegalArgumentException("Repeating event must specify either 'forTimes(n)' "
          + "or 'until(date)'.");
    }

    if (!localStart.toLocalDate().equals(template.getEnd().atZone(zone).toLocalDate())) {
      throw new IllegalArgumentException("Events in a series must start and end on the "
          + "same day.");
    }

    String seriesId = UUID.randomUUID().toString();
    Duration duration = Duration.between(template.getStart(), template.getEnd());
    LocalDateTime currentStart = localStart;

    if (this.occurrences != null) {
      if (this.occurrences <= 0) {
        throw new IllegalArgumentException("Occurrences must be a positive number.");
      }
      for (int i = 0; i < this.occurrences; i++) {
        if (!this.isMonthly) {
          while (!this.days.contains(currentStart.getDayOfWeek())) {
            currentStart = currentStart.plusDays(1);
          }
        }
        Event seriesEvent = createSeriesEvent(template, currentStart, duration, seriesId);
        newEvents.add(seriesEvent);

        if (this.isMonthly) {
          currentStart = currentStart.plusMonths(1);
        } else {
          currentStart = currentStart.plusDays(1);
        }
      }
    } else {
      if (this.untilDate.isBefore(template.getStart().atZone(zone).toLocalDate())) {
        throw new IllegalArgumentException("Series 'until' date cannot be before the start "
            + "date.");
      }
      while (!currentStart.toLocalDate().isAfter(this.untilDate)) {
        if (!this.isMonthly) {
          while (!this.days.contains(currentStart.getDayOfWeek())) {
            currentStart = currentStart.plusDays(1);
            if (currentStart.toLocalDate().isAfter(this.untilDate)) {
              break;
            }
          }
        }

        if (currentStart.toLocalDate().isAfter(this.untilDate)) {
          break;
        }

        Event seriesEvent = createSeriesEvent(template, currentStart, duration, seriesId);
        newEvents.add(seriesEvent);

        if (this.isMonthly) {
          currentStart = currentStart.plusMonths(1);
        } else {
          currentStart = currentStart.plusDays(1);
        }
      }
    }
  }
}