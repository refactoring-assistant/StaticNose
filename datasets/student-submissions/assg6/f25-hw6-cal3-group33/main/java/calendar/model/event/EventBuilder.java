package calendar.model.event;

import calendar.exceptions.InvalidDateTimeException;
import calendar.model.calendar.CalendarInterface;
import calendar.model.util.DateTimeParser;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder for creating calendar events with a fluent API.
 * Supports both single events and recurring event series.
 *
 * <p>USAGE:
 * Single event:
 * calendar.newEvent("Meeting", "2025-05-05T10:00")
 * .end("2025-05-05T11:00")
 * .location("Room 301")
 * .create(calendar);
 *
 * <p>All-day event:
 * calendar.newEvent("Conference", "2025-05-05")
 * .description("Annual conference")
 * .create(calendar);
 *
 * <p>Recurring event:
 * calendar.newEvent("Standup", "2025-05-05T09:00")
 * .end("2025-05-05T09:15")
 * .weekdays("MTWRF")
 * .forTimes(10)
 * .create(calendar);
 *
 * <p>All events use America/New_York timezone.
 * All-day events default to 8:00 AM - 5:00 PM.
 */
public class EventBuilder {

  private String subject;
  private String startDateTime;
  private String endDateTime;
  private String description;
  private String location;
  private EventStatus status;
  private String weekdays;
  private Integer times;
  private String until;
  private boolean isAllDay;
  private ZoneId timezone;

  /**
   * Private constructor. Use newEvent() factory method.
   *
   * @param subject       the event subject (required)
   * @param startDateTime the start date/time (required)
   *                      Format: YYYY-MM-DDThh:mm for timed events
   *                      YYYY-MM-DD for all-day events
   * @param timezone      the timezone for the event
   */
  private EventBuilder(String subject, String startDateTime, ZoneId timezone) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.timezone = timezone;
    this.isAllDay = !startDateTime.contains("T");
  }

  /**
   * Creates a new event builder with the given subject, start time, and timezone.
   * Automatically detects timed vs all-day events based on format.
   *
   * @param subject       the event subject, must not be null or empty
   * @param startDateTime start time (YYYY-MM-DDThh:mm) or date (YYYY-MM-DD)
   * @param timezone      the timezone for creating the event
   * @return new EventBuilder instance for method chaining
   */
  public static EventBuilder newEvent(String subject, String startDateTime, ZoneId timezone) {
    return new EventBuilder(subject, startDateTime, timezone);
  }

  /**
   * Sets end datetime for timed event (format: YYYY-MM-DDThh:mm).
   * Not needed for all-day events (defaults to 8:00 AM - 5:00 PM).
   *
   * @param endDateTime the end datetime string
   * @return this builder for method chaining
   */
  public EventBuilder end(String endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  /**
   * Sets description for the event.
   *
   * @param description the description text
   * @return this builder for method chaining
   */
  public EventBuilder description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets location for the event (physical address or online link).
   *
   * @param location the location text
   * @return this builder for method chaining
   */
  public EventBuilder location(String location) {
    this.location = location;
    return this;
  }

  /**
   * Sets status for the event.
   *
   * @param status the event status (PUBLIC or PRIVATE)
   * @return this builder for method chaining
   */
  public EventBuilder status(EventStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Makes event repeat on specific weekdays.
   * Weekday codes: M=Monday, T=Tuesday, W=Wednesday, R=Thursday,
   * F=Friday, S=Saturday, U=Sunday
   *
   * @param weekdays string of weekday codes (e.g., "MWF" for Mon/Wed/Fri)
   * @return this builder for method chaining
   */
  public EventBuilder weekdays(String weekdays) {
    this.weekdays = weekdays;
    return this;
  }

  /**
   * Makes event repeat for a specific number of occurrences.
   * Must be used with weekdays(). Cannot be combined with until().
   *
   * @param times number of occurrences (must be positive)
   * @return this builder for method chaining
   */
  public EventBuilder forTimes(Integer times) {
    this.times = times;
    return this;
  }

  /**
   * Makes event repeat until a specific date (inclusive).
   * Must be used with weekdays(). Cannot be combined with forTimes().
   *
   * @param until end date in format YYYY-MM-DD
   * @return this builder for method chaining
   */
  public EventBuilder until(String until) {
    this.until = until;
    return this;
  }

  /**
   * Builds the event(s) and returns a list.
   * Single event returns list with one element.
   * Recurring event returns list with all occurrences.
   *
   * @return list of built events (never null or empty)
   * @throws InvalidDateTimeException if date/time format is invalid
   * @throws IllegalArgumentException if required fields missing or invalid combinations
   */
  public List<EventInterface> build() throws InvalidDateTimeException {
    validate();

    ZonedDateTime start;
    ZonedDateTime end;

    if (isAllDay) {
      start = parseDate(startDateTime, timezone).withHour(8).withMinute(0).withSecond(0);
      end = parseDate(startDateTime, timezone).withHour(17).withMinute(0).withSecond(0);
    } else {
      start = parseDateTime(startDateTime, timezone);
      if (endDateTime == null) {
        throw new IllegalArgumentException("End datetime is required for timed events");
      }
      end = parseDateTime(endDateTime, timezone);
    }

    if (start.isAfter(end) || start.isEqual(end)) {
      throw new IllegalArgumentException("Start date/time must be before end date/time");
    }

    if (weekdays != null && !start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException(
          "Events in a series must start and end on the same day"
      );
    }

    // Single event
    if (weekdays == null) {
      Event event = new Event(subject, start, end);
      applyOptionalFields(event);
      return List.of(event);
    }

    // Event series
    String seriesId = UUID.randomUUID().toString();
    List<EventInterface> events = new ArrayList<>();
    List<ZonedDateTime> dates = calculateDates(start, end);
    Duration duration = Duration.between(start, end);

    for (ZonedDateTime occurrenceStart : dates) {
      ZonedDateTime occurrenceEnd = occurrenceStart.plus(duration);
      Event event = new Event(subject, occurrenceStart, occurrenceEnd);
      event.setSeriesId(seriesId);
      applyOptionalFields(event);
      events.add(event);
    }
    return events;
  }

  /**
   * Builds and stores events in the given calendar.
   * Convenience method combining build() and storeEvents().
   *
   * @param calendar the calendar to store events in
   * @throws InvalidDateTimeException                    if date/time format is invalid
   * @throws calendar.exceptions.DuplicateEventException if event already exists
   */
  public void create(CalendarInterface calendar)
      throws InvalidDateTimeException, calendar.exceptions.DuplicateEventException {
    List<EventInterface> events = build();
    calendar.storeEvents(events);
  }

  /**
   * Applies optional fields to an event.
   */
  private void applyOptionalFields(EventInterface event) {
    if (description != null) {
      event.setDescription(description);
    }
    if (location != null) {
      event.setLocation(location);
    }
    if (status != null) {
      event.setStatus(status);
    }
  }

  /**
   * Validates builder state before building.
   */
  private void validate() {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }

    if (startDateTime == null || startDateTime.trim().isEmpty()) {
      throw new IllegalArgumentException("Start date/time is required");
    }

    if (weekdays != null) {
      validateWeekdays();
      if (times == null && until == null) {
        throw new IllegalArgumentException("Series must specify either times or until");
      }
      if (times != null && until != null) {
        throw new IllegalArgumentException("Series cannot have both times and until");
      }
    }
  }

  /**
   * Calculates occurrence dates for repeating events.
   */
  private List<ZonedDateTime> calculateDates(ZonedDateTime start, ZonedDateTime end)
      throws InvalidDateTimeException {
    List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime current = start;

    if (times != null) {
      int occurrences = 0;
      while (occurrences < times) {
        if (matchesWeekdays(current)) {
          dates.add(current);
          occurrences++;
        }
        current = current.plusDays(1);
      }
    } else {
      ZonedDateTime untilDate = parseDate(until, timezone);
      while (!current.toLocalDate().isAfter(untilDate.toLocalDate())) {
        if (matchesWeekdays(current)) {
          dates.add(current);
        }
        current = current.plusDays(1);
      }
    }
    return dates;
  }


  /**
   * Checks if a date matches the specified weekdays.
   */
  private boolean matchesWeekdays(ZonedDateTime date) {
    DayOfWeek day = date.getDayOfWeek();
    char dayChar = dayOfWeekToChar(day);
    return weekdays.contains(String.valueOf(dayChar));
  }

  /**
   * Converts DayOfWeek to weekday character code.
   */
  private char dayOfWeekToChar(DayOfWeek day) {
    switch (day) {
      case MONDAY:
        return 'M';
      case TUESDAY:
        return 'T';
      case WEDNESDAY:
        return 'W';
      case THURSDAY:
        return 'R';
      case FRIDAY:
        return 'F';
      case SATURDAY:
        return 'S';
      case SUNDAY:
        return 'U';
      default:
        throw new IllegalArgumentException("Invalid day: " + day);
    }
  }

  /**
   * Validates weekdays string contains only valid characters.
   */
  private void validateWeekdays() {
    String validChars = "MTWRFSU";
    for (char c : weekdays.toCharArray()) {
      if (validChars.indexOf(c) == -1) {
        throw new IllegalArgumentException("Invalid weekday character: " + c
            + ". Valid: M, T, W, R, F, S, U");
      }
    }
  }

  /**
   * Parses datetime string in format YYYY-MM-DDThh:mm to specific zone time.
   */
  private ZonedDateTime parseDateTime(String dateTimeStr, ZoneId timezone)
      throws InvalidDateTimeException {
    return DateTimeParser.parseDateTime(dateTimeStr, timezone);
  }

  /**
   * Parses date string in format YYYY-MM-DD to Eastern Time at midnight.
   */
  private ZonedDateTime parseDate(String dateStr, ZoneId timezone)
      throws InvalidDateTimeException {
    return DateTimeParser.parseDate(dateStr, timezone);
  }
}