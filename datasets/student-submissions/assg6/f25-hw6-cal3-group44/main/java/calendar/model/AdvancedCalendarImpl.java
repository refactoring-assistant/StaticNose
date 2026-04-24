package calendar.model;

import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an advanced calendar that supports time zones and can perform
 * all standard calendar operations like adding, editing, and removing events.
 */
public class AdvancedCalendarImpl implements AdvancedCalendar {
  private final String name;
  private final ZoneId zoneId;
  private final CalendarEditable calendar;

  /**
   * Creates a new advanced calendar with the given editable calendar, name, and time zone.
   *
   * @param calendar the base editable calendar
   * @param name     the name of the calendar
   * @param zoneId   the time zone for this calendar
   */
  private AdvancedCalendarImpl(CalendarEditable calendar, String name, ZoneId zoneId) {
    this.calendar = Objects.requireNonNull(calendar);
    this.name = name;
    this.zoneId = zoneId;
  }

  /**
   * Adds a new event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if the event already exists
   */
  @Override
  public EventReadOnly addEvent(EventReadOnly event) {
    return calendar.addEvent(event);
  }

  /**
   * Edits one or more existing events by modifying a specified property.
   *
   * @param events   the list of events to modify
   * @param property the property to edit like subject
   * @param newValue the new value to assign to the property
   * @throws IllegalArgumentException      if no events match the given criteria
   * @throws UnsupportedOperationException if the property name is invalid
   */
  @Override
  public List<EventReadOnly> editEvent(List<EventReadOnly> events, String property,
                                       String newValue) {
    return calendar.editEvent(events, property, newValue);
  }

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   */
  @Override
  public void removeEvent(EventReadOnly event) {
    calendar.removeEvent(event);
  }

  /**
   * Checks whether the status in calendar is busy at the given date and time.
   *
   * @param dateTime the date and time to check
   * @return true if busy at the given time else false.
   */
  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return calendar.isBusy(dateTime);
  }

  /**
   * Executes the provided consumer for every event in the calendar.
   * Used mainly for filtering and passing each event to the filter predicate.
   *
   * @param consumer the consumer that processes each event.
   */
  @Override
  public void forEachEvent(Consumer<EventReadOnly> consumer) {
    calendar.forEachEvent(consumer);
  }

  /**
   * Retrieves all events that occur between the given start and end times.
   *
   * @param startDateTime the start date and time of the range.
   * @param endDateTime   the end date and time of the range.
   * @return a list of events occurring in the given range, empty if no events.
   */
  @Override
  public List<EventReadOnly> getEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return calendar.getEvents(startDateTime, endDateTime);
  }

  @Override
  public Map<LocalDate, List<EventReadOnly>> getAllEvents() {
    return calendar.getAllEvents();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public ZoneId getZoneId() {
    return this.zoneId;
  }

  @Override
  public CalendarEditable getCalendar() {
    return this.calendar;
  }

  /**
   * Builder class to create instances of AdvancedCalendarImpl.
   */
  public static class AdvancedCalendarBuilder {
    private CalendarEditable calendar;
    private String name;
    private ZoneId zoneId;

    /**
     * Creates a builder with a name and time zone.
     *
     * @param name   the calendar name
     * @param zoneId the time zone
     */
    public AdvancedCalendarBuilder(String name, ZoneId zoneId) {
      this.calendar = new CalendarImpl();
      this.name = name;
      this.zoneId = zoneId;
    }

    /**
     * Creates a builder from an existing advanced calendar.
     *
     * @param advancedCalendar the existing advanced calendar
     */
    public AdvancedCalendarBuilder(AdvancedCalendar advancedCalendar) {
      this.calendar = Objects.requireNonNull(advancedCalendar.getCalendar());
      this.name = Objects.requireNonNull(advancedCalendar.getName());
      this.zoneId = Objects.requireNonNull(advancedCalendar.getZoneId());
    }

    /**
     * Sets the base editable calendar.
     *
     * @param calendar the editable calendar to use
     * @return this builder instance
     */
    public AdvancedCalendarBuilder setCalendar(CalendarEditable calendar) {
      this.calendar = calendar;
      return this;
    }

    /**
     * Sets the name of the calendar.
     *
     * @param name the name to use
     * @return this builder instance
     */
    public AdvancedCalendarBuilder setName(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the time zone for the calendar.
     *
     * @param zoneId the time zone
     * @return this builder instance
     */
    public AdvancedCalendarBuilder setZoneId(ZoneId zoneId) {
      this.zoneId = zoneId;
      return this;
    }

    /**
     * Builds and returns the AdvancedCalendarImpl object.
     *
     * @return a new AdvancedCalendarImpl instance
     */
    public AdvancedCalendarImpl build() {
      return new AdvancedCalendarImpl(calendar, name, zoneId);
    }
  }
}
