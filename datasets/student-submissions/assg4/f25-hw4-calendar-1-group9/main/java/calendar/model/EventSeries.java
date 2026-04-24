package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages a collection of recurring events with shared properties.
 * Handles series-wide operations and maintains consistency across all instances.
 * Supports operations on single events, events from a date, or entire series.
 */
public class EventSeries {

  private final String baseSubject;
  private final RecurrencePattern pattern;
  private final List<RecurringEvent> instances;

  /**
   * Constructs an EventSeries with a pattern.
   * Note: Instances must be generated separately using generateInstances().
   *
   * @param baseSubject the subject for all events in series
   * @param pattern     the recurrence pattern defining repeat schedule
   * @param description optional description for all events
   * @param location    optional location for all events
   * @param status      privacy status for all events
   */
  public EventSeries(String baseSubject, RecurrencePattern pattern,
                     String description, String location, EventStatus status) {
    if (baseSubject == null || baseSubject.trim().isEmpty()) {
      throw new IllegalArgumentException("Base subject cannot be null or empty");
    }
    if (pattern == null) {
      throw new IllegalArgumentException("Pattern cannot be null");
    }

    this.baseSubject = baseSubject;
    this.pattern = pattern;
    this.instances = new ArrayList<>();
  }

  /**
   * Generates all event instances based on the recurrence pattern.
   * Creates RecurringEvent objects for each occurrence date.
   *
   * @param startDate   the first date to generate from
   * @param description optional description for all instances
   * @param location    optional location for all instances
   * @param status      privacy status for all instances
   */
  public void generateInstances(LocalDate startDate, String description,
                                String location, EventStatus status) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }

    List<LocalDate> dates = pattern.generateOccurrences(startDate);

    for (LocalDate date : dates) {
      LocalDateTime start = LocalDateTime.of(date, pattern.getStartTime());
      LocalDateTime end = pattern.getEndTime() != null
          ? LocalDateTime.of(date, pattern.getEndTime())
          : null;

      RecurringEvent event = new RecurringEvent(
          baseSubject, start, end, description, location, status, this
      );
      instances.add(event);
    }
  }

  /**
   * Updates a single event in the series.
   * Only modifies the specified event, leaving others unchanged.
   *
   * @param event   the specific event to update
   * @param updater consumer that modifies the event properties
   */
  public void updateSingleEvent(RecurringEvent event, Consumer<RecurringEvent> updater) {
    if (event == null || updater == null) {
      throw new IllegalArgumentException("Event and updater cannot be null");
    }

    if (instances.contains(event)) {
      updater.accept(event);
    }
  }

  /**
   * Updates all events in the series starting from a specific date (inclusive).
   * Events before the specified date are not modified.
   *
   * @param fromDate the date to start updating from (inclusive)
   * @param updater  consumer that modifies event properties
   */
  public void updateSeriesFrom(LocalDate fromDate, Consumer<RecurringEvent> updater) {
    if (fromDate == null || updater == null) {
      throw new IllegalArgumentException("From date and updater cannot be null");
    }

    instances.stream()
        .filter(e -> !e.getStartDateTime().toLocalDate().isBefore(fromDate))
        .forEach(updater);
  }

  /**
   * Updates all events in the entire series.
   * Applies the same modification to every instance.
   *
   * @param updater consumer that modifies event properties
   */
  public void updateEntireSeries(Consumer<RecurringEvent> updater) {
    if (updater == null) {
      throw new IllegalArgumentException("Updater cannot be null");
    }

    instances.forEach(updater);
  }

  /**
   * Splits the series at a specific date when start time changes.
   * Events before the split date remain in this series with the old time.
   * Events from split date onward are removed and returned as a new list.
   * This is necessary because events with different start times cannot be in same series.
   *
   * @param splitDate the date to split at (inclusive)
   * @return list of events to be moved to a new series
   */
  public List<RecurringEvent> splitSeries(LocalDate splitDate) {
    if (splitDate == null) {
      throw new IllegalArgumentException("Split date cannot be null");
    }

    List<RecurringEvent> afterSplit = instances.stream()
        .filter(e -> !e.getStartDateTime().toLocalDate().isBefore(splitDate))
        .collect(Collectors.toList());

    instances.removeAll(afterSplit);
    afterSplit.forEach(e -> e.setParentSeries(null));

    return afterSplit;
  }

  /**
   * Gets all event instances in this series.
   *
   * @return unmodifiable list of recurring events
   */
  public List<RecurringEvent> getInstances() {
    return Collections.unmodifiableList(instances);
  }

  /**
   * Gets the base subject of the series.
   *
   * @return the base subject
   */
  public String getBaseSubject() {
    return baseSubject;
  }

  /**
   * Gets the recurrence pattern.
   *
   * @return the pattern defining the recurrence schedule
   */
  public RecurrencePattern getPattern() {
    return pattern;
  }

  /**
   * Removes an event from the series.
   * Used when deleting a single occurrence.
   *
   * @param event the event to remove
   */
  public void removeInstance(RecurringEvent event) {
    if (event != null) {
      instances.remove(event);
    }
  }

  /**
   * Adds an event to the series.
   * Sets this series as the parent of the event.
   *
   * @param event the event to add
   */
  public void addInstance(RecurringEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (!instances.contains(event)) {
      instances.add(event);
      event.setParentSeries(this);
    }
  }

  /**
   * Gets the number of instances in this series.
   *
   * @return the instance count
   */
  public int getInstanceCount() {
    return instances.size();
  }

  /**
   * Checks if this series is empty.
   *
   * @return true if no instances exist, false otherwise
   */
  public boolean isEmpty() {
    return instances.isEmpty();
  }

  @Override
  public String toString() {
    return "EventSeries{subject='" + baseSubject + "', instances=" + instances.size()
        + ", pattern=" + pattern + "}";
  }
}
