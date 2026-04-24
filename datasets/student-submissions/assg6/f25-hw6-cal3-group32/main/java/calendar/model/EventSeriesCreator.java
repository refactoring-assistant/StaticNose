package calendar.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Creates recurring event series with timezone support.
 */
public class EventSeriesCreator {

  /**
   * Creates series repeating on specific weekdays for N occurrences.
   *
   * @param subject the event subject
   * @param start the first occurrence start time
   * @param end the first occurrence end time
   * @param weekdays the weekday pattern (e.g., MWF)
   * @param occurrences the number of occurrences to create
   * @return list of events in the series
   * @throws IllegalArgumentException if inputs are invalid
   */
  public List<Event> createSeriesForOccurrences(String subject,
                                                ZonedDateTime start,
                                                ZonedDateTime end,
                                                String weekdays,
                                                int occurrences) {
    validateSeriesInputs(subject, start, end, weekdays, occurrences);

    Set<DayOfWeek> repeatDays = parseWeekdays(weekdays);
    Duration duration = Duration.between(start, end);
    String seriesId = UUID.randomUUID().toString();
    return generateOccurrences(subject, start, repeatDays, duration,
        seriesId, occurrences, null);
  }

  /**
   * Creates series repeating until a specific end date.
   *
   * @param subject the event subject
   * @param start the first occurrence start time
   * @param end the first occurrence end time
   * @param weekdays the weekday pattern (e.g., MWF)
   * @param untilDate the last date to include (inclusive)
   * @return list of events in the series
   * @throws IllegalArgumentException if inputs are invalid
   */
  public List<Event> createSeriesUntilDate(String subject,
                                           ZonedDateTime start,
                                           ZonedDateTime end,
                                           String weekdays,
                                           LocalDate untilDate) {
    validateUntilDateInputs(subject, start, end, weekdays, untilDate);
    Set<DayOfWeek> repeatDays = parseWeekdays(weekdays);
    Duration duration = Duration.between(start, end);
    String seriesId = UUID.randomUUID().toString();
    return generateOccurrences(subject, start, repeatDays, duration,
        seriesId, Integer.MAX_VALUE, untilDate);
  }

  /**
   * Generates event occurrences based on criteria.
   *
   * @param subject the event subject
   * @param start the first occurrence start time
   * @param repeatDays the set of weekdays to repeat on
   * @param duration the duration of each event
   * @param seriesId the series identifier
   * @param maxOccurrences the maximum number of occurrences
   * @param untilDate the end date (inclusive) or null
   * @return list of generated events
   */
  List<Event> generateOccurrences(String subject,
                                  ZonedDateTime start,
                                  Set<DayOfWeek> repeatDays,
                                  Duration duration,
                                  String seriesId,
                                  int maxOccurrences,
                                  LocalDate untilDate) {
    List<Event> events = new ArrayList<>();
    ZonedDateTime current = start;
    int count = 0;

    int maxi;
    if (untilDate != null) {
      long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
          start.toLocalDate(), untilDate);
      maxi = (int) Math.min(daysBetween + 100, 50000);
    } else {
      maxi = Math.min(maxOccurrences * 7 + 365, 50000);
    }

    int daysChecked = 0;
    while (shouldContinueGenerating(count, maxOccurrences, current, untilDate)) {
      daysChecked++;
      if (daysChecked > maxi) {
        break;
      }

      if (repeatDays.contains(current.getDayOfWeek())) {
        Event event = createSingleOccurrence(subject, current, duration, seriesId);
        events.add(event);
        count++;
      }
      current = current.plusDays(1);
    }

    return events;
  }

  /**
   * Checks if should continue generating occurrences.
   *
   * @param count the current occurrence count
   * @param maxOccurrences the maximum allowed occurrences
   * @param current the current date being checked
   * @param untilDate the end date (inclusive) or null
   * @return true if should continue generating
   */
  private boolean shouldContinueGenerating(int count,
                                           int maxOccurrences,
                                           ZonedDateTime current,
                                           LocalDate untilDate) {
    if (count >= maxOccurrences) {
      return false;
    }
    return untilDate == null || !current.toLocalDate().isAfter(untilDate);
  }

  /**
   * Creates a single event occurrence.
   *
   * @param subject the event subject
   * @param start the event start time
   * @param duration the event duration
   * @param seriesId the series identifier
   * @return the created event
   * @throws IllegalArgumentException if event spans multiple days
   */
  private Event createSingleOccurrence(String subject,
                                       ZonedDateTime start,
                                       Duration duration,
                                       String seriesId) {
    ZonedDateTime eventEnd = start.plus(duration);
    if (!eventEnd.toLocalDate().equals(start.toLocalDate())) {
      throw new IllegalArgumentException(
          "Events in series cannot span multiple days");
    }
    return new Event.Builder(subject, start)
        .end(eventEnd)
        .seriesId(seriesId)
        .build();
  }

  /**
   * Parses weekday string into set of DayOfWeek.
   * M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday.
   *
   * @param weekdays the weekday string (e.g., MWF)
   * @return set of parsed weekdays
   * @throws IllegalArgumentException if weekdays are invalid
   */
  public Set<DayOfWeek> parseWeekdays(String weekdays) {
    Set<DayOfWeek> days = new HashSet<>();
    List<Character> invalidChars = new ArrayList<>();
    for (char c : weekdays.toUpperCase().toCharArray()) {
      if (Character.isWhitespace(c)) {
        continue;
      }
      try {
        DayOfWeek day = parseSingleWeekday(c);
        days.add(day);
      } catch (IllegalArgumentException e) {
        invalidChars.add(c);
      }
    }
    if (!invalidChars.isEmpty()) {
      throw new IllegalArgumentException(
          "Invalid weekday characters: " + invalidChars);
    }
    if (days.isEmpty()) {
      throw new IllegalArgumentException("Must specify at least one weekday");
    }
    return days;
  }

  /**
   * Parses a single weekday character.
   *
   * @param c the weekday character
   * @return the corresponding DayOfWeek
   * @throws IllegalArgumentException if character is invalid
   */
  private DayOfWeek parseSingleWeekday(char c) {
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
        throw new IllegalArgumentException("Invalid weekday: " + c);
    }
  }

  /**
   * Validates inputs for occurrence-based series.
   *
   * @param subject the event subject
   * @param start the start time
   * @param end the end time
   * @param weekdays the weekday pattern
   * @param occurrences the number of occurrences
   * @throws IllegalArgumentException if any input is invalid
   */
  private void validateSeriesInputs(String subject,
                                    ZonedDateTime start,
                                    ZonedDateTime end,
                                    String weekdays,
                                    int occurrences) {
    validateCommonInputs(subject, start, end, weekdays);
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
  }

  /**
   * Validates inputs for date-based series.
   *
   * @param subject the event subject
   * @param start the start time
   * @param end the end time
   * @param weekdays the weekday pattern
   * @param untilDate the end date
   * @throws IllegalArgumentException if any input is invalid
   */
  private void validateUntilDateInputs(String subject,
                                       ZonedDateTime start,
                                       ZonedDateTime end,
                                       String weekdays,
                                       LocalDate untilDate) {
    validateCommonInputs(subject, start, end, weekdays);
    if (untilDate == null) {
      throw new IllegalArgumentException("Until date cannot be null");
    }
    if (untilDate.isBefore(start.toLocalDate())) {
      throw new IllegalArgumentException(
          "Until date cannot be before start date");
    }
  }

  /**
   * Validates common inputs for all series types.
   *
   * @param subject the event subject
   * @param start the start time
   * @param end the end time
   * @param weekdays the weekday pattern
   * @throws IllegalArgumentException if any input is invalid
   */
  private void validateCommonInputs(String subject,
                                    ZonedDateTime start,
                                    ZonedDateTime end,
                                    String weekdays) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("End time cannot be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("End cannot be before start");
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }
  }
}