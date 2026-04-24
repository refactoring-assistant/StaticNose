package calendar.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class that represents the configuration for recurring series of events. It is a master object to
 * manage the generated events and rules for recurrence.
 */
public class SeriesMaster {
  private final String masterId;
  private final List<CalendarEvent> events;
  private CalendarEvent templateEvent;
  private RecurrenceRule rule;

  /**
   * Constructor to construct a new SeriesMaster object when a template event and recurrence rule is
   * passed as an argument.
   *
   * @param templateEvent event that is considered as reference for creating the event series
   * @param rule          refers to the rule that defines the repetition of templateEvent
   * @throws IllegalArgumentException when either the templateEvent or rule is null
   */
  public SeriesMaster(CalendarEvent templateEvent, RecurrenceRule rule)
      throws IllegalArgumentException {
    if (templateEvent == null || rule == null) {
      throw new IllegalArgumentException("Template event and recurrence rule cannot be null");
    }
    this.masterId = UUID.randomUUID().toString();
    this.templateEvent = templateEvent;
    this.rule = rule;
    this.events = new ArrayList<>();
  }

  /**
   * Getter method to get the unique internal ID for this series.
   *
   * @return String object containing the UUID of the series.
   */
  public String getMasterId() {
    return masterId;
  }

  /**
   * Getter method to get the templateEvent used for the series.
   *
   * @return CalendarEvent object that is used as template for the series
   */
  public CalendarEvent getTemplateEvent() {
    return templateEvent;
  }

  /**
   * Setter method to update the event template used for series creation.
   *
   * @param templateEvent new templateEvent to be used as reference
   */
  public void setTemplateEvent(CalendarEvent templateEvent) {
    this.templateEvent = templateEvent;
  }

  /**
   * Getter method to get the rule used for recurrence.
   *
   * @return RecurrenceRule object that defines the repetition
   */
  public RecurrenceRule getRule() {
    return rule;
  }

  /**
   * Setter method to update the recurrence rule used for series creation.
   *
   * @param rule new RecurrenceRule object to modify the series
   */
  public void setRule(RecurrenceRule rule) {
    this.rule = rule;
  }

  /**
   * Getter method to get a list of all events that belong to the series.
   *
   * @return list of CalendarEvent objects
   */
  public List<CalendarEvent> getEvents() {
    return events;
  }

  /**
   * Returns a list of all events in this series, sorted by start time.
   *
   * @return A new, sorted list of events.
   */
  public List<CalendarEvent> getSortedEvents() {
    return events.stream()
        .sorted(Comparator.comparing(CalendarEvent::getStart))
        .collect(Collectors.toList());
  }

  /**
   * Setter method to set the entire list of events for this master.
   *
   * @param events List of events to set.
   */
  public void setEvents(List<CalendarEvent> events) {
    this.events.clear();
    this.events.addAll(events);
  }

  /**
   * Method to remove an event from the set of events that belong to this series.
   *
   * @param event CalendarEvent object of the event to be removed
   */
  public void removeEvent(CalendarEvent event) {
    this.events.remove(event);
  }

  /**
   * Method to remove a list of events from this series.
   *
   * @param eventsToRemove List of CalendarEvent objects to remove.
   */
  public void removeEvents(List<CalendarEvent> eventsToRemove) {
    this.events.removeAll(eventsToRemove);
  }

  /**
   * Method to add a list of events to this series.
   *
   * @param eventsToAdd List of CalendarEvent objects to add.
   */
  public void addEvents(List<CalendarEvent> eventsToAdd) {
    this.events.addAll(eventsToAdd);
  }
}