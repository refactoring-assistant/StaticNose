package calendar.model;

import calendar.util.ConflictException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of CalendarInterface. Stores single events and recurring events.
 * Handles time zones, conflict detection, and edits.
 */
public class CalendarImpl implements CalendarInterface {

  private final List<EventInterface> events;
  private final List<RecurringEventInterface> recurringEvents;
  private ZoneId calendarZone;
  private String name;

  /**
   * Creates a new calendar with empty event lists and sets the timezone to New York.
   */
  public CalendarImpl() {
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
    this.calendarZone = ZoneId.of("America/New_York");
  }

  /**
   * Creates a new calendar with a given name.
   *
   * @param name the name of the calendar.
   */
  public CalendarImpl(String name) {
    this();
    setName(name);
  }

  /**
   * Creates a new calendar with a given name and timezone.
   *
   * @param name the name of the calendar.
   * @param zone the timezone for this calendar.
   */
  public CalendarImpl(String name, ZoneId zone) {
    this(name);
    setCalendarZone(zone);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    this.name = name;
  }

  @Override
  public ZoneId getCalendarZone() {
    return calendarZone;
  }

  @Override
  public void setCalendarZone(ZoneId zone) {
    if (zone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.calendarZone = zone;
  }

  @Override
  public void addEvent(EventInterface event) throws ConflictException {
    Objects.requireNonNull(event, "Event cannot be null");
    validateEventTimes(event);
    adjustEventToCalendarZone(event);

    if (hasConflict(event)) {
      throw new ConflictException("Event conflicts with existing event.");
    }

    events.add(event);
  }

  @Override
  public void addRecurringEvent(RecurringEventInterface recurringEvent) throws ConflictException {
    Objects.requireNonNull(recurringEvent, "Recurring event cannot be null");
    List<EventInterface> instances = recurringEvent.getAllEvents();

    for (EventInterface e : instances) {
      adjustEventToCalendarZone(e);
    }

    if (hasConflict(instances)) {
      throw new ConflictException("Recurring event conflicts with existing events.");
    }

    recurringEvents.add(recurringEvent);
    events.addAll(instances);
  }


  @Override
  public boolean removeEvent(EventInterface event) {
    events.remove(event);
    return false;
  }

  @Override
  public void removeRecurringEvent(RecurringEventInterface recurringEvent) {
    recurringEvents.remove(recurringEvent);
    events.removeAll(recurringEvent.getAllEvents());
  }

  @Override
  public List<EventInterface> getEventsOn(ZonedDateTime date) {
    ZonedDateTime dateNormalized = date.withZoneSameInstant(getCalendarZone());

    List<EventInterface> result = new ArrayList<>();
    for (EventInterface e : events) {
      ZonedDateTime start = e.getStart().withZoneSameInstant(getCalendarZone());
      ZonedDateTime end =
          (e.getEnd() != null) ? e.getEnd().withZoneSameInstant(getCalendarZone()) : start;
      if (!start.toLocalDate().isAfter(dateNormalized.toLocalDate())
          && !end.toLocalDate().isBefore(dateNormalized.toLocalDate())) {
        result.add(e);
      }
    }
    return result;
  }

  @Override
  public List<EventInterface> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    ZonedDateTime startNormalized = start.withZoneSameInstant(getCalendarZone());
    ZonedDateTime endNormalized = end.withZoneSameInstant(getCalendarZone());

    List<EventInterface> result = new ArrayList<>();
    for (EventInterface event : events) {
      ZonedDateTime eventStart = event.getStart().withZoneSameInstant(getCalendarZone());
      ZonedDateTime eventEnd =
          (event.getEnd() != null) ? event.getEnd().withZoneSameInstant(getCalendarZone()) :
              eventStart;
      if (!eventEnd.isBefore(startNormalized) && !eventStart.isAfter(endNormalized)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public boolean isBusy(ZonedDateTime dateTime) {
    ZonedDateTime dateTimeNormalized = dateTime.withZoneSameInstant(getCalendarZone());

    for (EventInterface e : events) {
      ZonedDateTime start = e.getStart().withZoneSameInstant(getCalendarZone());
      ZonedDateTime end =
          (e.getEnd() != null) ? e.getEnd().withZoneSameInstant(getCalendarZone()) : start;
      if (!dateTimeNormalized.isBefore(start) && !dateTimeNormalized.isAfter(end)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public EventInterface getEvent(String subject, ZonedDateTime start, ZonedDateTime end) {
    ZonedDateTime startNormalized = start.withZoneSameInstant(getCalendarZone());
    ZonedDateTime endNormalized = (end != null) ? end.withZoneSameInstant(getCalendarZone()) : null;

    for (EventInterface e : events) {
      ZonedDateTime eventStart = e.getStart().withZoneSameInstant(getCalendarZone());
      ZonedDateTime eventEnd =
          (e.getEnd() != null) ? e.getEnd().withZoneSameInstant(getCalendarZone()) : null;
      if (e.getSubject().equalsIgnoreCase(subject)
          && eventStart.equals(startNormalized)
          && ((endNormalized == null && eventEnd == null) || (eventEnd != null
          && eventEnd.equals(endNormalized)))) {
        return e;
      }
    }
    return null;
  }

  @Override
  public RecurringEventInterface getRecurringEvent(String subject, ZonedDateTime start) {
    ZonedDateTime startNormalized = start.withZoneSameInstant(getCalendarZone());

    for (RecurringEventInterface r : recurringEvents) {
      for (EventInterface e : r.getAllEvents()) {
        if (e.getSubject().equalsIgnoreCase(subject)
            && e.getStart().withZoneSameInstant(getCalendarZone()).equals(startNormalized)) {
          return r;
        }
      }
    }
    return null;
  }

  @Override
  public void editEvent(EventInterface target, EditableField field, Object newValue)
      throws ConflictException {
    Objects.requireNonNull(target, "Target event cannot be null");
    Objects.requireNonNull(field, "EditableField cannot be null");
    Objects.requireNonNull(newValue, "New value cannot be null");

    if (!events.contains(target)) {
      throw new IllegalArgumentException("Event not found in calendar");
    }

    String originalSubject = target.getSubject();
    ZonedDateTime originalStart = target.getStart();
    ZonedDateTime originalEnd = target.getEnd();
    String originalDescription = target.getDescription();
    String originalLocation = target.getLocation();
    EventStatus originalStatus = target.getStatus();

    applyEdit(target, field, newValue);

    if (hasConflict(target)) {
      switch (field) {
        case SUBJECT:
          target.setSubject(originalSubject);
          break;
        case START:
          target.setStart(originalStart);
          break;
        case END:
          target.setEnd(originalEnd);
          break;
        case DESCRIPTION:
          target.setDescription(originalDescription);
          break;
        case LOCATION:
          target.setLocation(originalLocation);
          break;
        case STATUS:
          target.setStatus(originalStatus);
          break;
        default:
          throw new IllegalArgumentException("Invalid field");
      }
      throw new ConflictException("Edit would cause a conflict with existing event.");
    }
  }

  @Override
  public void editEventsFrom(EventInterface target, EditableField field, Object newValue)
      throws ConflictException {

    RecurringEventInterface series = getRecurringEvent(target.getSubject(), target.getStart());
    if (series == null) {
      editEvent(target, field, newValue);
      return;
    }

    List<EventInterface> eventsCopy = new ArrayList<>(series.getAllEvents());
    boolean startEditing = false;

    for (EventInterface e : eventsCopy) {
      if (e.equals(target)) {
        startEditing = true;
      }
      if (startEditing) {
        editEvent(e, field, newValue);
      }
    }
  }

  @Override
  public void editEntireSeries(EventInterface target, EditableField field, Object newValue)
      throws ConflictException {

    RecurringEventInterface series = getRecurringEvent(target.getSubject(), target.getStart());
    if (series == null) {
      editEvent(target, field, newValue);
      return;
    }

    List<EventInterface> eventsCopy = new ArrayList<>(series.getAllEvents());
    for (EventInterface e : eventsCopy) {
      editEvent(e, field, newValue);
    }
  }

  @Override
  public List<EventInterface> getAllCalendarEvents() {
    return new ArrayList<>(events);
  }

  private void applyEdit(EventInterface event, EditableField field, Object newValue) {
    switch (field) {
      case SUBJECT:
        event.setSubject((String) newValue);
        break;
      case START:
        event.setStart((ZonedDateTime) newValue);
        break;
      case END:
        event.setEnd((ZonedDateTime) newValue);
        break;
      case DESCRIPTION:
        event.setDescription((String) newValue);
        break;
      case LOCATION:
        event.setLocation((String) newValue);
        break;
      case STATUS:
        event.setStatus((EventStatus) newValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid EditableField: " + field);
    }
    validateEventTimes(event);

    adjustEventToCalendarZone(event);
  }

  private void validateEventTimes(EventInterface event) {
    if (event.getStart() == null) {
      throw new IllegalArgumentException("Event start cannot be null.");
    }
    if (event.getEnd() != null && event.getEnd().isBefore(event.getStart())) {
      throw new IllegalArgumentException("Event end cannot be before start.");
    }
  }

  private void adjustEventToCalendarZone(EventInterface event) {
    if (!event.getStart().getZone().equals(getCalendarZone())) {
      event.setStart(event.getStart().withZoneSameInstant(getCalendarZone()));
      if (event.getEnd() != null) {
        event.setEnd(event.getEnd().withZoneSameInstant(getCalendarZone()));
      }
    }
  }

  private boolean hasConflict(EventInterface newEvent) {
    return hasConflict(Collections.singletonList(newEvent));
  }

  private boolean hasConflict(List<EventInterface> newEvents) {
    for (EventInterface newEvent : newEvents) {
      ZonedDateTime newStart = newEvent.getStart();
      ZonedDateTime newEnd = (newEvent.getEnd() != null) ? newEvent.getEnd() : newStart;

      for (EventInterface existing : events) {
        if (existing == newEvent) {
          continue;
        }

        ZonedDateTime existingStart = existing.getStart();
        ZonedDateTime existingEnd = (existing.getEnd() != null) ? existing.getEnd() : existingStart;
        if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
          return true;
        }
      }
    }
    return false;
  }

}