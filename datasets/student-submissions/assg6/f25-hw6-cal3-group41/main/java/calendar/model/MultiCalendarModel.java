package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages multiple calendars.
 */
public class MultiCalendarModel {
  private final Map<String, Calendar> calendars = new HashMap<>();
  private Calendar currentCalendar;

  /**
   * Creates a calendar.
   *
   * @param name     the name
   * @param timezone the timezone
   */
  public void createCalendar(String name, String timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    String nameTrimmed = name.trim();
    if (calendars.containsKey(nameTrimmed.toLowerCase())) {
      throw new IllegalArgumentException("Calendar '" + name + "' already exists.");
    }
    try {
      ZoneId zoneId = ZoneId.of(timezone);
      Calendar calendar = new Calendar(nameTrimmed, zoneId);
      calendars.put(nameTrimmed.toLowerCase(), calendar);
      if (currentCalendar == null) {
        currentCalendar = calendar;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }
  }

  /**
   * Creates a calendar with ZoneId.
   *
   * @param name   the name
   * @param zoneId the timezone
   */
  public void createCalendar(String name, ZoneId zoneId) {
    createCalendar(name, zoneId.getId());
  }

  /**
   * Gets all calendar names.
   *
   * @return list of calendar names
   */
  public List<String> getCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  /**
   * Edits a calendar.
   *
   * @param name     the name
   * @param property the property
   * @param newValue the new value
   */
  public void editCalendar(String name, String property, String newValue) {
    Calendar calendar = getCalendar(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found.");
    }

    if ("name".equalsIgnoreCase(property)) {
      String oldName = calendar.getName();
      String newName = newValue.trim();
      if (newName.isEmpty()) {
        throw new IllegalArgumentException("Calendar name cannot be empty.");
      }
      if (!oldName.equalsIgnoreCase(newName) && calendars.containsKey(newName.toLowerCase())) {
        throw new IllegalArgumentException("Calendar '" + newValue + "' already exists.");
      }
      calendars.remove(oldName.toLowerCase());
      calendar.setName(newName);
      calendars.put(newName.toLowerCase(), calendar);
      if (currentCalendar != null && currentCalendar.equals(calendar)) {
        currentCalendar = calendar;
      }
    } else if ("timezone".equalsIgnoreCase(property)) {
      try {
        ZoneId zoneId = ZoneId.of(newValue);
        calendar.setTimezone(zoneId);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone: " + newValue);
      }
    } else {
      throw new IllegalArgumentException(
          "Invalid property: " + property + ". Must be 'name' or 'timezone'.");
    }
  }

  /**
   * Sets the current calendar.
   *
   * @param name the calendar name
   */
  public void useCalendar(String name) {
    Calendar calendar = getCalendar(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found.");
    }
    this.currentCalendar = calendar;
  }

  /**
   * Gets the current calendar.
   *
   * @return the calendar
   */
  public Calendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Gets a calendar by name.
   *
   * @param name the name
   * @return the calendar
   */
  public Calendar getCalendar(String name) {
    if (name == null) {
      return null;
    }
    return calendars.get(name.trim().toLowerCase());
  }

  /**
   * Gets the current calendar model.
   *
   * @return the model
   */
  public CalendarModel getCurrentModel() {
    if (currentCalendar == null) {
      throw new IllegalStateException(
          "No calendar is currently in use. Use 'use calendar --name <name>' first.");
    }
    return currentCalendar.getModel();
  }

  /**
   * Gets the current calendar timezone.
   *
   * @return the timezone
   */
  public ZoneId getCurrentTimezone() {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use.");
    }
    return currentCalendar.getTimezone();
  }

  /**
   * Copies an event from one calendar to another.
   *
   * @param sourceCalendarName source calendar
   * @param eventName          event name
   * @param sourceStart        start time
   * @param targetCalendarName target calendar
   * @param targetStart        target start time
   */
  public void copyEvent(String sourceCalendarName, String eventName, LocalDateTime sourceStart,
                        String targetCalendarName, LocalDateTime targetStart) {
    Calendar sourceCal = getCalendar(sourceCalendarName);
    Calendar targetCal = getCalendar(targetCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar '" + sourceCalendarName + "' not found.");
    }
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalendarName + "' not found.");
    }

    List<Event> events = sourceCal.getModel().getEventsOn(sourceStart.toLocalDate());
    Event sourceEvent = null;
    for (Event e : events) {
      if (e.subject().trim().equalsIgnoreCase(eventName.trim())
          && e.startDate().equals(sourceStart)) {
        sourceEvent = e;
        break;
      }
    }
    if (sourceEvent == null) {
      throw new IllegalArgumentException("Event '" + eventName + "' not found at specified time.");
    }

    Duration duration = Duration.between(sourceEvent.startDate(), sourceEvent.endDate());
    LocalDateTime newStart = targetStart;
    LocalDateTime newEnd = newStart.plus(duration);

    targetCal.getModel().createEvent(sourceEvent.subject(), newStart, newEnd);
    List<Event> targetEvents = targetCal.getModel().getEventsOn(newStart.toLocalDate());
    for (Event e : targetEvents) {
      if (e.subject().equalsIgnoreCase(sourceEvent.subject())
          && e.startDate().equals(newStart) && e.endDate().equals(newEnd)) {
        e.setDescription(sourceEvent.description());
        e.setLocation(sourceEvent.location());
        e.setStatus(sourceEvent.status());
        if (sourceEvent.isRecurring()) {
          e.setRecurring(true);
          e.setSeriesId(sourceEvent.getSeriesId());
        }
        break;
      }
    }
  }

  /**
   * Copies all events on a date.
   *
   * @param sourceCalendarName source calendar
   * @param sourceDate         the date
   * @param targetCalendarName target calendar
   * @param targetDate         target date
   * @return number of events copied
   */
  public int copyEventsOnDate(String sourceCalendarName, LocalDate sourceDate,
                              String targetCalendarName, LocalDate targetDate) {
    Calendar sourceCal = getCalendar(sourceCalendarName);
    Calendar targetCal = getCalendar(targetCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar '" + sourceCalendarName + "' not found.");
    }
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalendarName + "' not found.");
    }

    List<Event> events = sourceCal.getModel().getEventsOn(sourceDate);
    int count = 0;

    for (Event sourceEvent : events) {
      try {
        ZonedDateTime sourceStartZ = ZonedDateTime.of(
            sourceEvent.startDate(), sourceCal.getTimezone());
        ZonedDateTime sourceEndZ = ZonedDateTime.of(
            sourceEvent.endDate(), sourceCal.getTimezone());
        ZonedDateTime targetStartZ = sourceStartZ.withZoneSameInstant(targetCal.getTimezone());
        ZonedDateTime targetEndZ = sourceEndZ.withZoneSameInstant(targetCal.getTimezone());

        LocalTime startTime = targetStartZ.toLocalTime();
        LocalTime endTime = targetEndZ.toLocalTime();
        LocalDateTime newStart = LocalDateTime.of(targetDate, startTime);
        LocalDateTime newEnd = LocalDateTime.of(targetDate, endTime);

        if (newEnd.isBefore(newStart)) {
          long minutes = Duration.between(sourceEvent.startDate(),
              sourceEvent.endDate()).toMinutes();
          newEnd = newStart.plusMinutes(minutes);
        }

        targetCal.getModel().createEvent(sourceEvent.subject(), newStart, newEnd);
        List<Event> targetEvents = targetCal.getModel().getEventsOn(newStart.toLocalDate());
        for (Event e : targetEvents) {
          if (e.subject().equalsIgnoreCase(sourceEvent.subject())
              && e.startDate().equals(newStart) && e.endDate().equals(newEnd)) {
            e.setDescription(sourceEvent.description());
            e.setLocation(sourceEvent.location());
            e.setStatus(sourceEvent.status());
            if (sourceEvent.isRecurring()) {
              e.setRecurring(true);
              e.setSeriesId(sourceEvent.getSeriesId());
            }
            break;
          }
        }
        count++;
      } catch (IllegalArgumentException e) {
        // Skip events that would create duplicates
      }
    }

    return count;
  }

  /**
   * Copies events in a date range.
   *
   * @param sourceCalendarName source calendar
   * @param sourceStart        start date
   * @param sourceEnd          end date
   * @param targetCalendarName target calendar
   * @param targetStart        target start
   * @return number of events copied
   */
  public int copyEventsBetween(String sourceCalendarName, LocalDate sourceStart,
                               LocalDate sourceEnd, String targetCalendarName,
                               LocalDateTime targetStart) {
    Calendar sourceCal = getCalendar(sourceCalendarName);
    Calendar targetCal = getCalendar(targetCalendarName);
    if (sourceCal == null) {
      throw new IllegalArgumentException("Source calendar '" + sourceCalendarName + "' not found.");
    }
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalendarName + "' not found.");
    }

    LocalDateTime sourceStartDateTime = sourceStart.atStartOfDay();
    LocalDateTime sourceEndDateTime = sourceEnd.atTime(23, 59, 59);
    List<Event> events =
        sourceCal.getModel().getEventsBetween(sourceStartDateTime, sourceEndDateTime);
    int count = 0;

    for (Event sourceEvent : events) {
      try {
        LocalDate eventDate = sourceEvent.startDate().toLocalDate();
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(sourceStart, eventDate);

        LocalTime startTime = sourceEvent.startDate().toLocalTime();
        LocalTime endTime = sourceEvent.endDate().toLocalTime();
        LocalDate newTargetDate = targetStart.toLocalDate().plusDays(daysDiff);
        LocalDateTime newStart = LocalDateTime.of(newTargetDate, startTime);
        LocalDateTime newEnd = LocalDateTime.of(newTargetDate, endTime);

        if (newEnd.isBefore(newStart)) {
          long minutes = Duration.between(sourceEvent.startDate(),
              sourceEvent.endDate()).toMinutes();
          newEnd = newStart.plusMinutes(minutes);
        }

        targetCal.getModel().createEvent(sourceEvent.subject(), newStart, newEnd);
        List<Event> targetEvents = targetCal.getModel().getEventsOn(newStart.toLocalDate());
        for (Event e : targetEvents) {
          if (e.subject().equalsIgnoreCase(sourceEvent.subject())
              && e.startDate().equals(newStart) && e.endDate().equals(newEnd)) {
            e.setDescription(sourceEvent.description());
            e.setLocation(sourceEvent.location());
            e.setStatus(sourceEvent.status());
            if (sourceEvent.isRecurring()) {
              e.setRecurring(true);
              e.setSeriesId(sourceEvent.getSeriesId());
            }
            break;
          }
        }
        count++;
      } catch (IllegalArgumentException e) {
        // Skip events that would create duplicates
      }
    }

    return count;
  }

  /**
   * Gets events for a specific calendar on a specific date.
   *
   * @param calendarName the calendar name
   * @param date         the date
   * @return list of events on that date
   */
  public List<Event> getEventsForCalendarOnDate(String calendarName, LocalDate date) {
    Calendar calendar = getCalendar(calendarName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' not found.");
    }
    return calendar.getModel().getEventsOn(date);
  }

  /**
   * Adds an event to a specific calendar.
   *
   * @param calendarName the calendar name
   * @param event        the event to add
   */
  public void addEventToCalendar(String calendarName, Event event) {
    Calendar calendar = getCalendar(calendarName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' not found.");
    }
    calendar.getModel().createEvent(event.subject(), event.startDate(), event.endDate());
    // Update the created event with additional properties
    List<Event> events = calendar.getModel().getEventsOn(event.startDate().toLocalDate());
    for (Event e : events) {
      if (e.subject().equals(event.subject())
          && e.startDate().equals(event.startDate())
          && e.endDate().equals(event.endDate())) {
        e.setDescription(event.description());
        e.setLocation(event.location());
        e.setStatus(event.status());
        if (event.isRecurring()) {
          e.setRecurring(true);
          e.setSeriesId(event.getSeriesId());
        }
        break;
      }
    }
  }

  /**
   * Updates an event in a specific calendar.
   *
   * @param calendarName the calendar name
   * @param event        the updated event
   */
  public void updateEventInCalendar(String calendarName, Event event) {
    Calendar calendar = getCalendar(calendarName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' not found.");
    }
    CalendarModel model = calendar.getModel();
    model.editEvent(event.subject(), event.startDate(), event.endDate(), "description",
        event.description());
    if (event.location() != null && !event.location().isEmpty()) {
      model.editEvent(event.subject(), event.startDate(), event.endDate(), "location",
          event.location());
    }
    if (event.status() != null && !event.status().isEmpty()) {
      model.editEvent(event.subject(), event.startDate(), event.endDate(), "status",
          event.status());
    }
  }

}

