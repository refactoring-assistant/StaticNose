package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recurring event.
 */
public class RecurringEvent implements Ievent {
  private final String name;
  private final ZonedDateTime startDateTime;
  private final ZonedDateTime endDateTime;
  private final String description;
  private final RecurrencePattern pattern;
  private final ZonedDateTime recurrenceEndDateTime;

  /**
   * Creates a recurring event.
   */
  public RecurringEvent(String name, ZonedDateTime startDateTime,
                        ZonedDateTime endDateTime, String description,
                        RecurrencePattern pattern,
                        ZonedDateTime recurrenceEndDateTime) {
    if (name == null || startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Name and times cannot be null");
    }
    this.name = name;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description == null ? "" : description;
    this.pattern = pattern;
    this.recurrenceEndDateTime = recurrenceEndDateTime;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ZonedDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public ZonedDateTime getEndDateTime() {
    return endDateTime;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public RecurrencePattern getPattern() {
    return pattern;
  }

  public ZonedDateTime getRecurrenceEndDateTime() {
    return recurrenceEndDateTime;
  }

  @Override
  public boolean occursOn(LocalDate date) {
    return !getOccurrencesInRange(date, date).isEmpty();
  }

  @Override
  public List<ZonedDateTime> getOccurrencesInRange(LocalDate start, LocalDate end) {
    List<ZonedDateTime> occurrences = new ArrayList<>();
    ZonedDateTime current = startDateTime;

    while (!current.toLocalDate().isAfter(end)) {
      if (recurrenceEndDateTime != null && current.isAfter(recurrenceEndDateTime)) {
        break;
      }

      if (!current.toLocalDate().isBefore(start)) {
        occurrences.add(current);
      }

      current = pattern.getNext(current);
      if (current == null) {
        break;
      }
    }

    return occurrences;
  }

  @Override
  public Ievent copyToNewStart(ZonedDateTime newStart) {
    long duration = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    ZonedDateTime newEnd = newStart.plusMinutes(duration);

    ZonedDateTime newRecurrenceEnd = null;
    if (recurrenceEndDateTime != null) {
      long recurrenceDuration = ChronoUnit.DAYS.between(
          startDateTime.toLocalDate(), recurrenceEndDateTime.toLocalDate());
      newRecurrenceEnd = newStart.plusDays(recurrenceDuration);
    }

    return new RecurringEvent(name, newStart, newEnd, description,
        pattern, newRecurrenceEnd);
  }

  @Override
  public boolean isRecurring() {
    return true;
  }
}
