package calendar.model;

import calendar.controller.commands.EditProperty;
import calendar.controller.commands.ShowStatus;
import calendar.model.util.SeriesDatesGenerator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This class implements the first version of a Calendar model.
 * This version of the model uses the in-memory repository to store the events.
 *
 * <p>This model allows for creating a single event or an event series.
 * It allows for editing an event.
 * It allows for filtering events.
 * It also allows for iterating over all events.
 */
public class ModelV1 implements Model {
  private final Repository repository;

  /**
   * Initializes the model with an empty in-memory repository.
   */
  public ModelV1() {
    repository = new InMemRepository();
  }

  @Override
  public void createEvent(Consumer<EventV1.Builder> consumer) {
    EventV1.Builder builder = new EventV1.Builder();
    consumer.accept(builder);
    Event event = builder.build();

    if (repository.exists(event)) {
      throw new IllegalArgumentException("Event already exists");
    }

    repository.insert(event);
  }

  @Override
  public void createEventSeries(Consumer<EventV1.Builder> consumer, HashSet<DayOfWeek> weekdays,
                                int repetition) {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }

    EventV1.Builder eventBuilder = new EventV1.Builder();
    consumer.accept(eventBuilder);
    Event baseEvent = eventBuilder.build();

    if (!baseEvent.startsAt().toLocalDate().isEqual(baseEvent.endsAt().toLocalDate())) {
      throw new IllegalArgumentException("Event in a series cannot span more than 24 hours");
    }

    List<List<LocalDateTime>> seriesDates =
        SeriesDatesGenerator.generate(baseEvent.startsAt(), baseEvent.endsAt(), weekdays,
            repetition);

    List<Event> eventsToBeInserted =
        generateEventsWithSeriesDates(UUID.randomUUID(), eventBuilder, seriesDates);

    repository.insertAll(eventsToBeInserted);
  }

  @Override
  public void createEventSeries(Consumer<EventV1.Builder> consumer, HashSet<DayOfWeek> weekdays,
                                LocalDate repeatUntil) {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }

    if (repeatUntil == null) {
      throw new IllegalArgumentException("RepeatUntil cannot be null");
    }

    EventV1.Builder eventBuilder = new EventV1.Builder();
    consumer.accept(eventBuilder);
    Event baseEvent = eventBuilder.build();

    if (!baseEvent.startsAt().toLocalDate().isEqual(baseEvent.endsAt().toLocalDate())) {
      throw new IllegalArgumentException("Event in a series cannot span more than 24 hours");
    }

    if (repeatUntil.isBefore(baseEvent.endsAt().toLocalDate())) {
      throw new IllegalArgumentException("RepeatUntil has to be after endsAt");
    }

    List<List<LocalDateTime>> seriesDates =
        SeriesDatesGenerator.generate(baseEvent.startsAt(), baseEvent.endsAt(), weekdays,
            repeatUntil);

    List<Event> eventsToBeInserted =
        generateEventsWithSeriesDates(UUID.randomUUID(), eventBuilder, seriesDates);

    repository.insertAll(eventsToBeInserted);
  }

  @Override
  public List<Event> filterEvents(Predicate<Event> predicate) {
    return repository.filter(predicate);
  }

  @Override
  public Iterable<Event> allEvents() {
    return repository;
  }

  @Override
  public void editEvent(EditProperty property, Event targetEvent, String newValue) {
    Event foundEvent = findEvent(targetEvent.subject(), targetEvent.startsAt(),
        targetEvent.endsAt());

    if (foundEvent == null) {
      throw new IllegalArgumentException("Event not found");
    }

    repository.delete(foundEvent);

    UUID newSeriesId = (property == EditProperty.START) ? UUID.randomUUID()
        : foundEvent.seriesId();
    Event modifiedEvent = applyPropertyChangeWithSeriesId(foundEvent, property, newValue,
        newSeriesId);

    if (repository.exists(modifiedEvent)) {
      repository.insert(foundEvent); // Rollback
      throw new IllegalArgumentException(
          "Event with same subject, start, and end already exists");
    }

    repository.insert(modifiedEvent);
  }

  @Override
  public void editEventsForward(EditProperty property, Event targetEvent, String newValue) {
    Event foundEvent = findEventByStartTime(targetEvent.subject(), targetEvent.startsAt());

    if (foundEvent == null) {
      throw new IllegalArgumentException("Event not found");
    }

    UUID seriesId = foundEvent.seriesId();
    List<Event> seriesEvents = repository.filter(event -> event.seriesId().equals(seriesId));

    List<Event> eventsBefore = new ArrayList<>();
    List<Event> eventsFromTarget = new ArrayList<>();

    for (Event event : seriesEvents) {
      if (event.startsAt().isBefore(targetEvent.startsAt())) {
        eventsBefore.add(event);
      } else {
        eventsFromTarget.add(event);
      }
    }

    for (Event event : eventsFromTarget) {
      repository.delete(event);
    }

    UUID newSeriesId = (property == EditProperty.START) ? UUID.randomUUID() : seriesId;
    List<Event> modifiedEvents = new ArrayList<>();

    for (Event event : eventsFromTarget) {
      Event modified = applyPropertyChangeWithSeriesId(event, property, newValue, newSeriesId);

      if (repository.exists(modified)) {
        repository.insertAll(eventsFromTarget); // Rollback
        throw new IllegalArgumentException(
            "Edit would create event with same subject, start, and end as existing event");
      }

      modifiedEvents.add(modified);
    }

    repository.insertAll(modifiedEvents);
  }

  @Override
  public void editEventSeries(EditProperty property, Event targetEvent, String newValue) {
    Event foundEvent = findEventByStartTime(targetEvent.subject(), targetEvent.startsAt());

    if (foundEvent == null) {
      throw new IllegalArgumentException("Event not found");
    }

    UUID seriesId = foundEvent.seriesId();
    List<Event> seriesEvents = repository.filter(event -> event.seriesId().equals(seriesId));

    for (Event event : seriesEvents) {
      repository.delete(event);
    }

    UUID newSeriesId = (property == EditProperty.START) ? UUID.randomUUID() : seriesId;
    List<Event> modifiedEvents = new ArrayList<>();

    for (Event event : seriesEvents) {
      Event modified = applyPropertyChangeWithSeriesId(event, property, newValue, newSeriesId);

      if (repository.exists(modified)) {
        repository.insertAll(seriesEvents); // Rollback
        throw new IllegalArgumentException(
            "Edit would create event with same subject, start, and end as existing event");
      }

      modifiedEvents.add(modified);
    }

    repository.insertAll(modifiedEvents);
  }


  private List<Event> generateEventsWithSeriesDates(UUID seriesId, EventV1.Builder builder,
                                                    List<List<LocalDateTime>> seriesDates) {
    List<Event> events = new ArrayList<>();

    for (List<LocalDateTime> dateList : seriesDates) {
      Event newEvent =
          builder.seriesId(seriesId).startsAt(dateList.get(0)).endsAt(dateList.get(1)).build();
      if (repository.exists(newEvent)) {
        throw new IllegalArgumentException(
            "Series clashes with another event on " + dateList.get(0) + " - " + dateList.get(1));
      }
      events.add(newEvent);
    }

    return events;
  }

  private Event findEvent(String subject, LocalDateTime startsAt, LocalDateTime endsAt) {
    for (Event event : repository) {
      if (event.matches(subject, startsAt, endsAt)) {
        return event;
      }
    }
    return null;
  }

  private Event findEventByStartTime(String subject, LocalDateTime startsAt) {
    for (Event event : repository) {
      if (event.subject().equals(subject) && event.startsAt().equals(startsAt)) {
        return event;
      }
    }
    return null;
  }

  private Event applyPropertyChangeWithSeriesId(Event event, EditProperty property,
                                                String newValue, UUID seriesId) {
    EventV1.Builder builder = new EventV1.Builder();

    builder.seriesId(seriesId);
    builder.subject(event.subject());
    builder.description(event.description());
    builder.startsAt(event.startsAt());
    builder.endsAt(event.endsAt());
    builder.location(event.location());
    builder.status(event.status());

    switch (property) {
      case SUBJECT:
        builder.subject(newValue);
        break;
      case START:
        LocalDateTime newStartDateTime = LocalDateTime.parse(newValue,
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        LocalDateTime updatedStart = event.startsAt()
            .withHour(newStartDateTime.getHour())
            .withMinute(newStartDateTime.getMinute());

        builder.startsAt(updatedStart);
        break;
      case END:
        LocalDateTime newEndDateTime = LocalDateTime.parse(newValue,
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        LocalDateTime updatedEnd = event.endsAt()
            .withHour(newEndDateTime.getHour())
            .withMinute(newEndDateTime.getMinute());

        builder.endsAt(updatedEnd);
        break;
      case DESCRIPTION:
        builder.description(newValue);
        break;
      case LOCATION:
        builder.location(Location.stringToLocation(newValue.toLowerCase()));
        break;
      case STATUS:
      default:
        builder.status(Status.stringToStatus(newValue.toLowerCase()));
    }

    return builder.build();
  }

  @Override
  public ShowStatus showStatus(LocalDateTime dateTime) {
    for (Event event : repository) {
      if (dateTime.isEqual(event.startsAt()) || dateTime.isAfter(event.startsAt())
          && dateTime.isBefore(event.endsAt())) {
        return ShowStatus.BUSY;
      }
    }
    return ShowStatus.AVAILABLE;
  }
}
