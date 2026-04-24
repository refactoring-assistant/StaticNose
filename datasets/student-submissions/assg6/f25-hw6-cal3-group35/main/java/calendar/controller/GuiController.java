package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Gui operations.
 * Handles user interactions from the Gui view and delegates to the model.
 */
public class GuiController {

  private final CalendarManager manager;

  /**
   * Constructs the Gui controller.
   *
   * @param manager the calendar manager
   */
  public GuiController(CalendarManager manager) {
    this.manager = manager;
  }

  /**
   * Creates a new calendar.
   *
   * @param name the calendar name
   * @param timezoneId the timezone ID string
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void createCalendar(String name, String timezoneId) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    try {
      ZoneId zoneId = ZoneId.of(timezoneId);
      manager.createCalendar(name.trim(), zoneId);
      manager.useCalendar(name.trim());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneId);
    }
  }

  /**
   * Switches to a different calendar.
   *
   * @param name the calendar name
   */
  public void useCalendar(String name) {
    manager.useCalendar(name);
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the active calendar name or null if none active
   */
  public String getActiveCalendarName() {
    return manager.getActiveCalendarName();
  }

  /**
   * Gets all calendar names.
   *
   * @return set of calendar names
   */
  public Set<String> getAllCalendarNames() {
    return manager.getAllCalendars().keySet();
  }

  /**
   * Gets all events for a specific date.
   *
   * @param date the date
   * @return list of events on that date
   */
  public List<Event> getEventsForDate(LocalDate date) {
    Optional<Calendar> activeCalendar = manager.getActiveCalendar();
    if (!activeCalendar.isPresent()) {
      return java.util.Collections.emptyList();
    }

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

    return activeCalendar.get().getService().getAllEvents().stream()
        .filter(event -> !event.getStart().isBefore(startOfDay)
            && event.getStart().isBefore(endOfDay))
        .filter(event -> !event.getSubject().startsWith("DELETED_"))
        .collect(Collectors.toList());
  }

  /**
   * Creates a new event.
   *
   * @param subject the event subject
   * @param start the start date-time
   * @param end the end date-time
   * @param location optional location
   * @param description optional description
   * @param isRecurring whether event recurs
   * @param recurringDays days of week for recurrence
   * @param count recurrence count
   * @param until recurrence end date
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end,
                          String location, String description, boolean isRecurring,
                          EnumSet<DayOfWeek> recurringDays, Integer count, LocalDate until) {
    Optional<Calendar> activeCalendar = manager.getActiveCalendar();
    if (!activeCalendar.isPresent()) {
      throw new IllegalArgumentException("No active calendar selected");
    }

    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be empty");
    }

    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end times are required");
    }

    if (end.isBefore(start) || end.equals(start)) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    Optional<String> loc = location != null && !location.trim().isEmpty()
        ? Optional.of(location.trim()) : Optional.empty();
    Optional<String> desc = description != null && !description.trim().isEmpty()
        ? Optional.of(description.trim()) : Optional.empty();

    try {
      if (isRecurring) {
        if (recurringDays == null || recurringDays.isEmpty()) {
          throw new IllegalArgumentException("Must specify days for recurring events");
        }

        calendar.model.RecurrenceRule rule = new calendar.model.RecurrenceRule(
            recurringDays,
            Optional.ofNullable(count),
            Optional.ofNullable(until)
        );

        activeCalendar.get().getService().createEventSeries(
            new Event(subject.trim(), start, end, desc, loc,
                Optional.empty(), Optional.empty()),
            rule
        );
      } else {
        activeCalendar.get().getService().createSingleEvent(
            new Event(subject.trim(), start, end, desc, loc,
                Optional.empty(), Optional.empty())
        );
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create event: " + e.getMessage());
    }
  }

  /**
   * Edits an event.
   *
   * @param subject the event subject
   * @param fromDateTime reference date-time for editing
   * @param scope edit scope
   * @param property property to edit
   * @param newValue new value
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void editEvent(String subject, LocalDateTime fromDateTime, String scope,
                        String property, String newValue) {
    Optional<Calendar> activeCalendar = manager.getActiveCalendar();
    if (!activeCalendar.isPresent()) {
      throw new IllegalArgumentException("No active calendar selected");
    }

    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be empty");
    }

    if (property == null || property.trim().isEmpty()) {
      throw new IllegalArgumentException("Property cannot be empty");
    }

    if (newValue == null || newValue.trim().isEmpty()) {
      throw new IllegalArgumentException("New value cannot be empty");
    }

    try {
      switch (scope.toLowerCase()) {
        case "single":
          activeCalendar.get().getService().editSingle(
              subject, fromDateTime, property, newValue);
          break;
        case "from":
          activeCalendar.get().getService().editFrom(
              subject, fromDateTime, property, newValue);
          break;
        case "series":
          activeCalendar.get().getService().editSeries(
              subject, fromDateTime, property, newValue);
          break;
        default:
          throw new IllegalArgumentException("Invalid scope: " + scope);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit event: " + e.getMessage());
    }
  }

  /**
   * Deletes an event.
   *
   * @param subject the event subject
   * @param fromDateTime reference date-time
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void deleteEvent(String subject, LocalDateTime fromDateTime) {
    Optional<Calendar> activeCalendar = manager.getActiveCalendar();
    if (!activeCalendar.isPresent()) {
      throw new IllegalArgumentException("No active calendar selected");
    }

    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be empty");
    }

    try {
      activeCalendar.get().getService().editSeries(
          subject, fromDateTime, "subject", "DELETED_" + System.currentTimeMillis());
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to delete event: " + e.getMessage());
    }
  }
}