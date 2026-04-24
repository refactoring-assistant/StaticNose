package calendar.model;

import calendar.controller.filter.CalendarFilter;
import calendar.controller.filter.EventPredicates;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import calendar.model.interfaces.ModifyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implementation of the ICalenderEditable interface.
 * Stores and manages calendar events, including adding, editing, removing,
 * and exporting events to a file.
 */

public class CalendarImpl implements CalendarEditable {
  protected Map<LocalDate, List<EventReadOnly>> events;

  /**
   * Constructor of a new empty calendar.
   * We are using Map to store the calendar events.
   * Each entry contains list of all the events for a given day.
   */
  public CalendarImpl() {
    this.events = new TreeMap<>();
  }

  @Override
  public EventReadOnly addEvent(EventReadOnly newEvent) {
    LocalDate eventDate = newEvent.getStartDateTime().toLocalDate();
    List<EventReadOnly> existingEvents = events.getOrDefault(eventDate, new ArrayList<>());

    for (EventReadOnly existingEvent : existingEvents) {
      if (newEvent.equals(existingEvent)) {
        throw new IllegalArgumentException("Error: Event already exists");
      }
    }
    existingEvents.add(newEvent);
    events.put(eventDate, existingEvents);
    return newEvent;
  }

  @Override
  public List<EventReadOnly> editEvent(List<EventReadOnly> events, String property,
                                       String newValue) {
    if (events.isEmpty()) {
      throw new IllegalArgumentException("Error: Event with given details doesn't exist");
    }
    if (events.size() == 1) {
      return editSingleEvent(events, property, newValue);
    }
    return editMultipleEvents(events, property, newValue);

  }

  /**
   * Edits multiple events in a batch by modifying the specified property.
   *
   * @param events   the list of events to modify
   * @param property the property name to edit
   * @param newValue the new value for the property
   * @throws UnsupportedOperationException if the property is invalid
   */

  private List<EventReadOnly> editMultipleEvents(List<EventReadOnly> events,
                                                 String property, String newValue) {
    Map<String, ModifyEvent> modifyEvents = populateModifyEvents();
    ModifyEvent modifyEvent = modifyEvents.getOrDefault(property.toLowerCase(), null);
    if (modifyEvent == null) {
      throw new UnsupportedOperationException("Invalid property value to edit: " + property);
    }
    UUID uuid = UUID.randomUUID();
    List<EventReadOnly> editedEvents = new ArrayList<>();
    for (EventReadOnly event : events) {
      EventReadOnly updatedEvent;
      if (property.equalsIgnoreCase("start")) {
        updatedEvent = modifyEvents.get("id").edit(this, event, uuid.toString());
        String currStart = updatedEvent.getStartDateTime().toString();
        String newStartTime = newValue.split("T")[1];
        String newStratDateTime = currStart.split("T")[0] + "T" + newStartTime;
        updatedEvent = modifyEvent.edit(this, updatedEvent, newStratDateTime);
      } else if (property.equalsIgnoreCase("end")) {
        String currEnd = event.getEndDateTime().toString();
        String newEndTime = newValue.split("T")[1];
        String newEndDateTime = currEnd.split("T")[0] + "T" + newEndTime;
        updatedEvent = modifyEvent.edit(this, event, newEndDateTime);
      } else {
        updatedEvent = modifyEvent.edit(this, event, newValue);
      }
      updateEvent(event, updatedEvent);
      editedEvents.add(updatedEvent);
    }
    return editedEvents;
  }

  /**
   * Edits a single event's property.
   * and if necessary, changes its type from series to single.
   *
   * @param events   list containing events.
   * @param property the property to modify.
   * @param newValue the value for the property.
   * @throws UnsupportedOperationException if the property is invalid
   */
  private List<EventReadOnly> editSingleEvent(List<EventReadOnly> events,
                                              String property, String newValue) {
    Map<String, ModifyEvent> modifyEvents = populateModifyEvents();
    ModifyEvent modifyEvent = modifyEvents.getOrDefault(property.toLowerCase(), null);
    if (modifyEvent == null) {
      throw new UnsupportedOperationException("Invalid property value to edit: " + property);
    }

    List<EventReadOnly> editedEvents = new ArrayList<>();

    for (EventReadOnly event : events) {
      EventReadOnly updatedEvent = event;

      if (property.equalsIgnoreCase("start")
          && event.getEventType() == TypeOfEvent.SERIES) {
        updatedEvent = new Event.EventBuilder(event)
            .setEventId(UUID.randomUUID())
            .setEventType(TypeOfEvent.SINGLE)
            .build();
      }
      updatedEvent = modifyEvent.edit(this, updatedEvent, newValue);
      updateEvent(event, updatedEvent);
      editedEvents.add(updatedEvent);
    }
    return editedEvents;
  }

  /**
   * Populates a map of property modification handlers used.
   * call and modify events for the given property.
   *
   * @return a map linking property names to their respective modification classes.
   */
  private Map<String, ModifyEvent> populateModifyEvents() {
    Map<String, ModifyEvent> modifyEvents = new HashMap<>();

    modifyEvents.put("subject", new ModifySubject());
    modifyEvents.put("start", new ModifyStartDate());
    modifyEvents.put("end", new ModifyEndDate());
    modifyEvents.put("location", new ModifyLocation());
    modifyEvents.put("description", new ModifyDescription());
    modifyEvents.put("status", new ModifyStatus());
    modifyEvents.put("id", new ModifyId());

    return modifyEvents;
  }

  @Override
  public void removeEvent(EventReadOnly event) {
    LocalDate eventDate = event.getStartDateTime().toLocalDate();
    List<EventReadOnly> existingEvents = events.get(eventDate);
    existingEvents.remove(event);
  }

  @Override
  public void forEachEvent(Consumer<EventReadOnly> consumer) {
    for (List<EventReadOnly> eventList : events.values()) {
      for (EventReadOnly event : eventList) {
        consumer.accept(event);
      }
    }
  }


  @Override
  public List<EventReadOnly> getEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {

    if (startDateTime.toLocalDate().equals(endDateTime.toLocalDate())
        && startDateTime.toLocalTime().equals(LocalTime.of(0, 0))
        && endDateTime.toLocalTime().equals(LocalTime.of(23, 59))) {
      return events.getOrDefault(startDateTime.toLocalDate(), new ArrayList<>());
    }
    CalendarFilter filter = new CalendarFilter(this);
    return filter.filter(EventPredicates
        .betweenStartAndEnd(startDateTime, endDateTime));
  }

  @Override
  public Map<LocalDate, List<EventReadOnly>> getAllEvents() {
    return this.events;
  }


  @Override
  public boolean isBusy(LocalDateTime dateTime) {

    LocalDate date = dateTime.toLocalDate();
    if (!events.containsKey(date)) {
      return false;
    }
    List<EventReadOnly> eventList = events.get(date);
    for (EventReadOnly event : eventList) {
      LocalDateTime startDateTime = event.getStartDateTime();
      LocalDateTime endDateTime = event.getEndDateTime();
      if (event.getStartDateTime().equals(dateTime)
          || (startDateTime.isBefore(dateTime)
          && endDateTime.isAfter(dateTime))) {
        return true;
      }
    }
    return false;
  }

  private void updateEvent(EventReadOnly oldEvent, EventReadOnly newEvent) {
    List<EventReadOnly> eventsOnDate =
        events.getOrDefault(oldEvent.getStartDateTime().toLocalDate(), new ArrayList<>());
    eventsOnDate.remove(oldEvent);
    this.forEachEvent(event -> {
      if (event.equals(newEvent)) {
        throw new IllegalArgumentException("Update conflicting with existing event");
      }
    });
    this.addEvent(newEvent);
    events.put(oldEvent.getStartDateTime().toLocalDate(), eventsOnDate);
  }
}
