package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a recurring event series.
 * Generates multiple event instances based on repeat pattern.
 */
public class MyEventSeries {
  private final String seriesId;
  private final String subject;
  private final LocalDateTime baseStart;
  private final LocalDateTime baseEnd;
  private final Set<DayOfWeek> repeatDays;
  private final Integer occurrences;
  private final LocalDate endDate;
  private final boolean isAllDay;

  private String location;
  private String description;
  private String status;

  /**
   * Creates an event series with a specific number of occurrences.
   *
   * @param subject the event subject
   * @param start the start time for each occurrence
   * @param end the end time for each occurrence
   * @param repeatDays the days of week to repeat on
   * @param occurrences the number of occurrences
   * @param isAllDay whether events are all-day
   */
  public MyEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                       Set<DayOfWeek> repeatDays, int occurrences, boolean isAllDay) {
    this.seriesId = UUID.randomUUID().toString();
    this.subject = subject;
    this.baseStart = start;
    this.baseEnd = end;
    this.repeatDays = repeatDays;
    this.occurrences = occurrences;
    this.endDate = null;
    this.isAllDay = isAllDay;
    this.status = "public";
  }

  /**
   * Creates an event series that repeats until a specific date.
   *
   * @param subject the event subject
   * @param start the start time for each occurrence
   * @param end the end time for each occurrence
   * @param repeatDays the days of week to repeat on
   * @param endDate the last date to repeat until (inclusive)
   * @param isAllDay whether events are all-day
   */
  public MyEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                       Set<DayOfWeek> repeatDays, LocalDate endDate, boolean isAllDay) {
    this.seriesId = UUID.randomUUID().toString();
    this.subject = subject;
    this.baseStart = start;
    this.baseEnd = end;
    this.repeatDays = repeatDays;
    this.occurrences = null;
    this.endDate = endDate;
    this.isAllDay = isAllDay;
    this.status = "public";
  }

  /**
   * Sets the location for all events in this series.
   *
   * @param location the location
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Sets the description for all events in this series.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the status for all events in this series.
   *
   * @param status the status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Generates all event instances in this series.
   *
   * @return list of events
   */
  public List<Event> generateEvents() {
    List<Event> events = new ArrayList<>();
    LocalDate currentDate = baseStart.toLocalDate();
    int count = 0;

    if (occurrences != null) {
      while (count < occurrences) {
        if (repeatDays.contains(currentDate.getDayOfWeek())) {
          Event event = createEventForDate(currentDate);
          events.add(event);
          count++;
        }
        currentDate = currentDate.plusDays(1);
      }
    } else {
      while (!currentDate.isAfter(endDate)) {
        if (repeatDays.contains(currentDate.getDayOfWeek())) {
          Event event = createEventForDate(currentDate);
          events.add(event);
        }
        currentDate = currentDate.plusDays(1);
      }
    }

    return events;
  }

  /**
   * Creates an event for a specific date using the base time.
   *
   * @param date the date for the event
   * @return the created event
   */
  private Event createEventForDate(LocalDate date) {
    LocalDateTime eventStart = date.atTime(baseStart.toLocalTime());
    LocalDateTime eventEnd = date.atTime(baseEnd.toLocalTime());

    Event event;
    if (isAllDay) {
      event = new MyEventImplement(subject, eventStart);
    } else {
      event = new MyEventImplement(subject, eventStart, eventEnd);
    }

    event.setSeriesId(seriesId);
    event.setLocation(location);
    event.setDescription(description);
    event.setStatus(status);

    return event;
  }

  /**
   * Parses weekday string (e.g., "MWF") into a set of DayOfWeek.
   * M = Monday, T = Tuesday, W = Wednesday, R = Thursday,
   * F = Friday, S = Saturday, U = Sunday.
   *
   * @param weekdays the weekday string
   * @return set of DayOfWeek
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdays) {
    Set<DayOfWeek> days = new HashSet<>();

    for (char c : weekdays.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }

    return days;
  }

  /**
   * Gets the unique series ID.
   *
   * @return the series ID
   */
  public String getSeriesId() {
    return seriesId;
  }
}