package calendar.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a recurring series .
 */
public class EventSeries implements EventSeriesInterface {
  private final UUID id = UUID.randomUUID();
  private Event baseEvent;
  private Set<DayOfWeek> repeatDays = EnumSet.noneOf(DayOfWeek.class);
  private Integer occurrences;
  private LocalDate untilDate;
  private final List<Event> events = new ArrayList<>();

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Event getBaseEvent() {
    return baseEvent;
  }

  @Override
  public Set<DayOfWeek> getRepeatDays() {
    return Collections.unmodifiableSet(repeatDays);
  }

  @Override
  public int getOccurrences() {
    return occurrences == null ? -1 : occurrences;
  }

  @Override
  public LocalDate getEndDate() {
    return untilDate;
  }

  @Override
  public List<Event> getEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public void setBaseEvent(Event baseEvent) {
    this.baseEvent = baseEvent;
  }

  @Override
  public void setRepeatDays(Set<DayOfWeek> repeatDays) {
    this.repeatDays = EnumSet.copyOf(repeatDays);
  }

  @Override
  public void setOccurrences(Integer occurrences) {
    this.occurrences = occurrences;
  }

  @Override
  public void setEndDate(LocalDate endDate) {
    this.untilDate = endDate;
  }

  @Override
  public List<Event> generateEvents() {
    events.clear();
    if (baseEvent == null || repeatDays.isEmpty()) {
      return events;
    }

    LocalDate seed = baseEvent.startDate().toLocalDate();
    LocalTime startTime = baseEvent.startDate().toLocalTime();
    Duration dur = Duration.between(baseEvent.startDate(), baseEvent.endDate());

    int made = 0;
    boolean seedCreated = false;
    LocalDate d = seed.minusDays(1);
    while (true) {
      d = d.plusDays(1);
      
      boolean shouldCreate = false;
      if (!seedCreated && d.equals(seed)) {
        shouldCreate = true;
        seedCreated = true;
      } else if (seedCreated && repeatDays.contains(d.getDayOfWeek())) {
        shouldCreate = true;
      }
      
      if (!shouldCreate) {
        continue;
      }
      
      if (occurrences != null && made >= occurrences) {
        break;
      }
      if (untilDate != null && d.isAfter(untilDate)) {
        break;
      }

      LocalDateTime st = LocalDateTime.of(d, startTime);
      LocalDateTime en = st.plus(dur);
      if (!st.toLocalDate().equals(en.toLocalDate())) {
        throw new IllegalArgumentException("Series event must start and end on same day.");
      }
      Event inst = baseEvent.copy();
      inst.setStart(st);
      inst.setEnd(en);
      inst.setSeriesId(id);
      inst.setRecurring(true);
      events.add(inst);
      made++;

      if (occurrences == null && untilDate == null && seedCreated) {
        break;
      }
      if (occurrences != null && made >= occurrences) {
        break;
      }
    }
    return events;
  }

  @Override
  public void modifySeries(String newSubject, String newDescription, String newLocation,
                           String newStatus, boolean newRecurring) {
    if (newSubject != null) {
      baseEvent.setSubject(newSubject);
    }
    if (newDescription != null) {
      baseEvent.setDescription(newDescription);
    }
    if (newLocation != null) {
      baseEvent.setLocation(newLocation);
    }
    if (newStatus != null) {
      baseEvent.setStatus(newStatus);
    }
    baseEvent.setRecurring(newRecurring);
    generateEvents();
  }

  @Override
  public boolean contains(Event e) {
    return e.getSeriesId() != null && e.getSeriesId().equals(this.id);
  }
}
