package calendar.model;

import calendar.controller.commands.EditProperty;
import calendar.controller.commands.ShowStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This interface represents the model used by the calendar.
 */
public interface Model {
  /**
   * A method to create a single event.
   *
   * @param consumer A consumer that consumes the event builder to build the event to be created.
   */
  void createEvent(Consumer<EventV1.Builder> consumer);

  /**
   * A method to create a series of events that repeat a certain number of times on given weekdays.
   *
   * @param consumer A consumer that consumes the event builder to build the event to be created.
   * @param weekdays A HashSet of the weekdays the event should repeat on.
   * @param repetition The integer number of times the event should repeat.
   */
  void createEventSeries(Consumer<EventV1.Builder> consumer, HashSet<DayOfWeek> weekdays,
                         int repetition);

  /**
   * A method to create a series of events that repeat until a given timestamp on given weekdays.
   *
   * @param consumer A consumer that consumes the event builder to build the event to be created.
   * @param weekdays A HashSet of the weekdays the event should repeat on.
   * @param repeatUntil The LocalDateTime timestamp until which the event should repeat.
   */
  void createEventSeries(Consumer<EventV1.Builder> consumer, HashSet<DayOfWeek> weekdays,
                         LocalDate repeatUntil);

  /**
   * A method to find all events that satisfy a given criteria.
   *
   * @param predicate The criteria wrapped in a Predicate function.
   * @return A list of events satisfying the given criteria.
   */
  List<Event> filterEvents(Predicate<Event> predicate);

  /**
   * This method allows to going over all the events.
   *
   * @return An iterable that iterates over all the events.
   */
  Iterable<Event> allEvents();

  /**
   * Edit a single event instance.
   *
   * @param property The property to edit
   * @param targetEvent The event to find and edit
   * @param newValue The new value for the property
   */
  void editEvent(EditProperty property, Event targetEvent, String newValue);

  /**
   * Edit this event and all future events in the series.
   *
   * @param property The property to edit
   * @param targetEvent The event to find (identifies the series and starting point)
   * @param newValue The new value for the property
   */
  void editEventsForward(EditProperty property, Event targetEvent, String newValue);

  /**
   * Edit all events in the series.
   *
   * @param property The property to edit
   * @param targetEvent The event to find (identifies the series)
   * @param newValue The new value for the property
   */
  void editEventSeries(EditProperty property, Event targetEvent, String newValue);

  /**
   * Get the user's status (busy or available) at a given date and time.
   *
   * @param dateTime The date and time to check
   * @return Status enum indicating BUSY or AVAILABLE
   */
  ShowStatus showStatus(LocalDateTime dateTime);
}