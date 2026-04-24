package calendar.model;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a recurring event that repeats on specific weekdays, either for a fixed number of
 * occurrences or until a specified end date.
 * This class caches generated instances of the event for efficiency and provides
 * functionality to split a series or find event indices.
 */
public class RecurringEvent implements RecurringEventInterface {

  private final EventInterface baseEvent;
  private int occurrences;
  private Set<Weekday> weekdaySet;
  private Set<DayOfWeek> dayOfWeekSet;
  private ZonedDateTime endDate;

  private final List<EventInterface> generatedEvents = new ArrayList<>();
  private boolean generated = false;
  private final List<ZonedDateTime> skippedInstances = new ArrayList<>();
  private final Map<ZonedDateTime, EventInterface> modifiedInstances = new HashMap<>();

  /**
   * Recurring event.
   *
   * @param baseEvent base event.
   * @param weekdays the days the events occur.
   * @param occurrences number of occurrences.
   * @param endDate end date of the recurring events.
   */
  public RecurringEvent(EventInterface baseEvent, List<Weekday> weekdays, int occurrences,
                        ZonedDateTime endDate) {
    Objects.requireNonNull(baseEvent, "Base event required");
    Objects.requireNonNull(weekdays, "Weekdays cannot be null");
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be empty");
    }

    if (baseEvent.getEnd() != null
        && !baseEvent.getStart().toLocalDate().equals(baseEvent.getEnd().toLocalDate())) {
      throw new IllegalArgumentException("Recurring events must start and end on the same day.");
    }

    if (occurrences > 0 && endDate != null) {
      throw new IllegalArgumentException("Specify either occurrences or endDate, not both.");
    }

    this.baseEvent = baseEvent;
    this.occurrences = occurrences;
    this.endDate = endDate;
    setWeekdays(weekdays);
  }

  private boolean isMatchingDay(ZonedDateTime date) {
    return dayOfWeekSet.contains(date.getDayOfWeek());
  }

  private EventInterface copyEvent(EventInterface e) {
    return new Event(
        e.getSubject(),
        e.getStart(),
        e.getEnd(),
        e.getDescription(),
        e.getLocation(),
        e.getStatus(),
        e.isAllDay()
    );
  }

  private void generateIfNeeded() {
    if (generated) {
      return;
    }
    generatedEvents.clear();

    ZonedDateTime currentStart = baseEvent.getStart();
    ZonedDateTime currentEnd = baseEvent.getEnd();
    int count = 0;

    while ((occurrences == 0 || count < occurrences)
        && (endDate == null || !currentStart.isAfter(endDate))) {

      if (isMatchingDay(currentStart)) {
        generatedEvents.add(copyEvent(baseEventWithDate(currentStart, currentEnd)));
        count++;
      }

      currentStart = currentStart.plusDays(1);
      if (currentEnd != null) {
        currentEnd = currentEnd.plusDays(1);
      }
    }

    generated = true;
  }

  private EventInterface baseEventWithDate(ZonedDateTime start, ZonedDateTime end) {
    return new Event(
        baseEvent.getSubject(),
        start,
        end,
        baseEvent.getDescription(),
        baseEvent.getLocation(),
        baseEvent.getStatus(),
        baseEvent.isAllDay()
    );
  }

  @Override
  public EventInterface getEventInstance(int occurrenceIndex) {
    generateIfNeeded();
    if (occurrenceIndex < 0 || occurrenceIndex >= generatedEvents.size()) {
      return null;
    }
    return generatedEvents.get(occurrenceIndex);
  }

  @Override
  public List<EventInterface> getAllEvents() {
    generateIfNeeded();
    return new ArrayList<>(generatedEvents);
  }

  @Override
  public int getOccurrences() {
    generateIfNeeded();
    return generatedEvents.size();
  }

  @Override
  public void setOccurrences(int occurrences) {
    this.occurrences = occurrences;
    invalidateGeneration();
  }

  @Override
  public List<Weekday> getWeekdays() {
    return new ArrayList<>(weekdaySet);
  }

  @Override
  public void setWeekdays(List<Weekday> weekdays) {
    Objects.requireNonNull(weekdays, "Weekdays cannot be null");
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be empty");
    }
    this.weekdaySet = new HashSet<>(weekdays);
    this.dayOfWeekSet = new HashSet<>();
    for (Weekday wd : weekdays) {
      dayOfWeekSet.add(wd.toDayOfWeek());
    }
    invalidateGeneration();
  }

  @Override
  public ZonedDateTime getEndDate() {
    return endDate;
  }

  @Override
  public void setEndDate(ZonedDateTime endDate) {
    this.endDate = endDate;
    invalidateGeneration();
  }

  /**
   * Invalid generation.
   */
  public void invalidateGeneration() {
    generated = false;
    generatedEvents.clear();
  }

  /**
   * index of class.
   *
   * @param target value.
   * @return negative one.
   */
  public int indexOf(EventInterface target) {
    generateIfNeeded();
    for (int i = 0; i < generatedEvents.size(); i++) {
      EventInterface e = generatedEvents.get(i);
      if (e == target
          ||
          (e.getStart().equals(target.getStart()) && e.getSubject().equals(target.getSubject()))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Recurring event.
   *
   * @param index of event.
   * @return null.
   */
  public RecurringEvent splitAtIndex(int index) {
    generateIfNeeded();
    if (index < 0 || index > generatedEvents.size()) {
      throw new IllegalArgumentException("Index out of bounds: " + index);
    }
    if (index == generatedEvents.size()) {
      return null;
    }

    EventInterface suffixBase = generatedEvents.get(index);
    int suffixCount = generatedEvents.size() - index;
    RecurringEvent suffix = new RecurringEvent(copyEvent(suffixBase),
        getWeekdays(), suffixCount, null);

    for (int i = index; i < generatedEvents.size(); i++) {
      suffix.generatedEvents.add(copyEvent(generatedEvents.get(i)));
    }
    suffix.generated = true;

    generatedEvents.subList(index, generatedEvents.size()).clear();
    this.generated = true;

    if (this.occurrences > 0) {
      this.occurrences = generatedEvents.size();
    }

    return suffix;
  }

  @Override
  public void addSkippedInstances(List<ZonedDateTime> skipped) {
    if (skipped != null) {
      skippedInstances.addAll(skipped);
    }
  }

  @Override
  public List<ZonedDateTime> getSkippedInstances() {
    return new ArrayList<>(skippedInstances);
  }

  @Override
  public void addModifiedInstances(Map<ZonedDateTime, EventInterface> modified) {
    if (modified != null) {
      modifiedInstances.putAll(modified);
    }
  }

  @Override
  public Map<ZonedDateTime, EventInterface> getModifiedInstances() {
    return new HashMap<>(modifiedInstances);
  }
}
