package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single (non-recurring) event.
 */
public class Event implements Ievent {
  private final String name;
  private final ZonedDateTime startDateTime;
  private final ZonedDateTime endDateTime;
  private final String description;

  /**
   * Creates a new event.
   *
   * @param name          the event name
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param description   the description
   */
  public Event(String name, ZonedDateTime startDateTime,
               ZonedDateTime endDateTime, String description) {
    if (name == null || startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Name and times cannot be null");
    }
    if (endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End time must be after start time");
    }
    this.name = name;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description == null ? "" : description;
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

  @Override
  public boolean occursOn(LocalDate date) {
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  @Override
  public List<ZonedDateTime> getOccurrencesInRange(LocalDate start, LocalDate end) {
    List<ZonedDateTime> occurrences = new ArrayList<>();
    if (occursOn(start) || occursOn(end)
        || (startDateTime.toLocalDate().isAfter(start)
        && startDateTime.toLocalDate().isBefore(end))) {
      occurrences.add(startDateTime);
    }
    return occurrences;
  }

  @Override
  public Ievent copyToNewStart(ZonedDateTime newStart) {
    long duration = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    ZonedDateTime newEnd = newStart.plusMinutes(duration);
    return new Event(name, newStart, newEnd, description);
  }

  @Override
  public boolean isRecurring() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return name.equals(event.name)
        && startDateTime.equals(event.startDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, startDateTime);
  }
}
