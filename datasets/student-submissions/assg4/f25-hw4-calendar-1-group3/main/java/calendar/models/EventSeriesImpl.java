package calendar.models;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a series of events that have same start and end times.
 */
public class EventSeriesImpl implements EventSeries {

  private final String seriesId;
  private final Set<Event> events;
  private final LocalDate startDate;
  private final RecurrenceRule rule;
  private final Event prototypeEvent;

  /**
   * Initialize a new series of events.
   *
   * @param prototypeEvent template of an event used to make copies of events in the series
   * @param startDate      start date of the series
   * @param recurrenceRule rule that defines the frequency and number of events in the series
   */
  public EventSeriesImpl(Event prototypeEvent, LocalDate startDate, RecurrenceRule recurrenceRule) {
    this.seriesId = UUID.randomUUID().toString();
    this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
    this.rule = Objects.requireNonNull(recurrenceRule, "Recurrence Rule must not be null");
    this.prototypeEvent = Objects.requireNonNull(prototypeEvent, "PrototypeEvent must not be null");
    this.events = new HashSet<>();
  }

  @Override
  public String getSeriesId() {
    return this.seriesId;
  }

  @Override
  public RecurrenceRule getRecurrenceRule() {
    return this.rule;
  }

  @Override
  public Set<Event> getEvents() {
    if (events.isEmpty()) {
      events.addAll(generateEvents());
    }
    return events;
  }

  private Set<Event> generateEvents() {
    Set<Event> events = new HashSet<>();
    List<LocalDate> dates = rule.generateDates(startDate);
    for (LocalDate date : dates) {
      Event event = prototypeEvent.toBuilder().from(date, prototypeEvent.getStartTime())
          .to(date, prototypeEvent.getEndTime()).seriesId(this.seriesId).build();
      events.add(event);
    }
    return events;
  }
}