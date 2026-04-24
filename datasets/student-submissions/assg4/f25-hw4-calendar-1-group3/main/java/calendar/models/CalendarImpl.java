package calendar.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of a calendar that stores and manages events and event series.
 */
public class CalendarImpl implements Calendar {

  private final String title;
  private final Set<Event> events;
  private final Map<String, EventSeries> seriesMap;

  /**
   * Initialize a calendar with title.
   *
   * @param title name of the calendar.
   */
  public CalendarImpl(String title) {
    this.title = Objects.requireNonNull(title);
    this.events = new HashSet<>();
    this.seriesMap = new HashMap<>();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public Set<Event> filterEvents(FilterCondition condition) {
    if (condition == null) {
      throw new IllegalArgumentException("Filter condition must not be null");
    }

    Set<Event> result = new HashSet<>();
    for (Event event : events) {
      if (condition.evaluate(event)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    Set<Event> filteredEvents = filterEvents(
        e -> !LocalDateTime.of(e.getEndDate(), e.getEndTime()).isBefore(dateTime)
            && !LocalDateTime.of(e.getStartDate(), e.getStartTime()).isAfter(dateTime));

    return !filteredEvents.isEmpty();
  }

  private boolean removeEvent(Event event) {

    boolean removed = events.remove(event);

    if (removed && event.isPartOfSeries()) {
      String seriesId = event.getSeriesId();

      boolean seriesStillHasEvents = false;
      for (Event e : events) {
        if (seriesId.equals(e.getSeriesId())) {
          seriesStillHasEvents = true;
          break;
        }
      }

      if (!seriesStillHasEvents) {
        seriesMap.remove(seriesId);
      }
    }

    return removed;
  }

  @Override
  public Event findSingleEvent(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    for (Event event : events) {
      if (event.getSubject().equalsIgnoreCase(subject)
          && event.getStartDate().equals(startDateTime.toLocalDate())
          && event.getStartTime().equals(startDateTime.toLocalTime())
          && event.getEndDate().equals(endDateTime.toLocalDate())
          && event.getEndTime().equals(endDateTime.toLocalTime())) {
        return event;
      }
    }
    return null;
  }

  @Override
  public Set<Event> findEventBySubjectAndStart(String subject, LocalDateTime startDateTime) {
    Set<Event> matches = new HashSet<>();

    for (Event event : events) {
      if (event.getSubject().equalsIgnoreCase(subject)
          && event.getStartDate().equals(startDateTime.toLocalDate())
          && event.getStartTime().equals(startDateTime.toLocalTime())) {
        matches.add(event);
      }
    }

    return matches;
  }

  @Override
  public Event editSingleEvent(Event event, EventProperty property, String newValue) {

    if (event == null || property == null) {
      throw new IllegalArgumentException("Event and property must not be null");
    }

    EventImpl.EventBuilder builder = event.toBuilder();

    switch (property) {
      case SUBJECT:
        builder.subject(newValue);
        break;

      case START_DATE_TIME:
        LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);
        builder.from(newStartDateTime.toLocalDate(), newStartDateTime.toLocalTime());
        break;

      case END_DATE_TIME:
        LocalDateTime newEndDateTime = LocalDateTime.parse(newValue);
        builder.to(newEndDateTime.toLocalDate(), newEndDateTime.toLocalTime());
        break;

      case DESCRIPTION:
        builder.description(newValue);
        break;

      case LOCATION:
        Location location = parseLocation(newValue);
        builder.location(location);
        break;

      default:
        Status status = Status.valueOf(newValue.toUpperCase());
        builder.status(status);
        break;
    }

    Event newEvent = builder.build();

    removeEvent(event);
    addEvent(newEvent);
    return newEvent;
  }

  @Override
  public EventSeries editSeriesEvent(Event event, EventProperty property, String newValue) {
    if (event == null || property == null) {
      throw new IllegalArgumentException("Event and property must not be null");
    }
    String seriesId = event.getSeriesId();

    if (seriesId == null) {
      throw new IllegalArgumentException("Given event is not part of any series");
    }

    EventImpl.EventBuilder prototypeEventBuilder = event.toBuilder().seriesId(null);

    switch (property) {
      case SUBJECT:
        prototypeEventBuilder.subject(newValue);
        break;

      case START_DATE_TIME:
        LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);
        prototypeEventBuilder.from(newStartDateTime.toLocalDate(), newStartDateTime.toLocalTime());
        break;

      case END_DATE_TIME:
        LocalDateTime newEndDateTime = LocalDateTime.parse(newValue);
        prototypeEventBuilder.to(newEndDateTime.toLocalDate(), newEndDateTime.toLocalTime());
        break;

      case DESCRIPTION:
        prototypeEventBuilder.description(newValue);
        break;

      case LOCATION:
        Location location = parseLocation(newValue);
        prototypeEventBuilder.location(location);
        break;

      case STATUS:
        Status status = Status.valueOf(newValue.toUpperCase());
        prototypeEventBuilder.status(status);
        break;

      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }

    Event prototypeEvent = prototypeEventBuilder.build();

    EventSeries oldSeries = seriesMap.get(seriesId);
    EventSeries newSeries =
        new EventSeriesImpl(prototypeEvent, event.getStartDate(), oldSeries.getRecurrenceRule());

    removeSeries(seriesId);
    addEventSeries(newSeries);

    return newSeries;
  }

  @Override
  public EventSeries editThisAndFollowingEvents(Event event, EventProperty property,
      String newValue) {
    if (property == EventProperty.START_DATE_TIME) {
      LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);
      if (!newStartDateTime.toLocalTime().equals(event.getStartTime())) {
        return splitAndCreateNewSeries(event, newValue);
      }
    }

    String seriesId = event.getSeriesId();
    LocalDateTime targetStart = LocalDateTime.of(event.getStartDate(), event.getStartTime());

    Set<Event> eventsCopy = new HashSet<>(events);

    for (Event e : eventsCopy) {
      if (seriesId.equals(e.getSeriesId())) {
        LocalDateTime eventStart = LocalDateTime.of(e.getStartDate(), e.getStartTime());
        if (!eventStart.isBefore(targetStart)) {
          editSingleEvent(e, property, newValue);
        }
      }
    }

    return seriesMap.get(seriesId);
  }

  private EventSeries splitAndCreateNewSeries(Event event, String newValue) {
    String seriesId = event.getSeriesId();
    LocalDateTime targetStart = LocalDateTime.of(event.getStartDate(), event.getStartTime());

    Set<Event> followingEvents = new HashSet<>();
    for (Event e : events) {
      if (seriesId.equals(e.getSeriesId())) {
        LocalDateTime eventStart = LocalDateTime.of(e.getStartDate(), e.getStartTime());
        if (!eventStart.isBefore(targetStart)) {
          followingEvents.add(e);
        }
      }
    }

    for (Event e : followingEvents) {
      removeEvent(e);
    }

    EventImpl.EventBuilder prototypeEventBuilder = event.toBuilder().seriesId(null);
    LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);
    prototypeEventBuilder.from(newStartDateTime.toLocalDate(), newStartDateTime.toLocalTime());

    Event prototype = prototypeEventBuilder.build();
    EventSeries oldSeries = seriesMap.get(seriesId);
    EventSeries newSeries =
        new EventSeriesImpl(prototype, prototype.getStartDate(), oldSeries.getRecurrenceRule());

    addEventSeries(newSeries);
    return newSeries;
  }

  private Location parseLocation(String locationStr) {
    return Location.valueOf(locationStr.trim().toUpperCase());
  }

  @Override
  public boolean addEvent(Event event) {
    return events.add(event);
  }

  @Override
  public void addEventSeries(EventSeries series) {
    seriesMap.put(series.getSeriesId(), series);
    events.addAll(series.getEvents());
  }

  private void removeSeries(String seriesId) {
    Set<Event> eventsToRemove = new HashSet<>();
    for (Event e : events) {
      if (seriesId.equals(e.getSeriesId())) {
        eventsToRemove.add(e);
      }
    }
    events.removeAll(eventsToRemove);
    seriesMap.remove(seriesId);
  }
}