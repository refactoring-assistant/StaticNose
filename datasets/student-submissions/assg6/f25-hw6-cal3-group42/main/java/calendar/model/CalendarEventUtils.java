package calendar.model;

import calendar.model.event.AbstractCalendarEvent;
import calendar.model.event.CalendarEvent;
import calendar.model.event.SingleEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class providing helper methods for calendar event operations.
 * All methods are static and the class cannot be instantiated.
 *
 * <p>Provides functionality for creating modified copies of immutable events,
 * validation, and common event-related operations.
 */
public class CalendarEventUtils {

  /**
   * Constructor to prevent instantiation.
   *
   * @throws UnsupportedOperationException if instantiation is attempted
   */
  CalendarEventUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Creates a new event with an updated property value.
   *
   * @param event     the original event
   * @param property  the property name to update
   * @param newValue  the new value for the property
   * @param seriesUid the series UID for the new event
   * @return a new SingleEvent with the updated property
   */
  public static CalendarEvent withUpdatedProperty(CalendarEvent event, String property,
                                                  Object newValue, String seriesUid) {
    Map<String, Object> params = new HashMap<>(event.getHashMap());
    params.put(property, newValue);
    return new SingleEvent(params, seriesUid);
  }

  /**
   * Creates a temporary event for searching purposes.
   *
   * @param subject       the event subject
   * @param startDateTime the start date-time
   * @param endDateTime   the end date-time
   * @return a temporary SingleEvent with no series UID
   */
  public static SingleEvent createTempEvent(String subject, ZonedDateTime startDateTime,
                                            ZonedDateTime endDateTime) {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", subject);
    params.put("start", startDateTime);
    params.put("end", endDateTime);
    return new SingleEvent(params, null);
  }

  /**
   * Creates a Duplicate event for returning purposes.
   *
   * @param event the event to create a duplicate of
   * @return a duplicate SingleEvent with same series UID
   */
  public static CalendarEvent createDuplicateEvent(CalendarEvent event) {
    Map<String, Object> params = event.getHashMap();
    String seriesUid = event.getSeriesUid();
    return new SingleEvent(params, seriesUid);
  }

  /**
   * Creates a new event with updated time property, preserving the original date.
   * Only updates the time-of-day component, keeping the event on its original date.
   * Used for updating subsequent events in a series.
   *
   * @param event     the original event
   * @param property  "start" or "end"
   * @param newValue  the new date-time (only time component is used)
   * @param seriesUid the series UID for the new event
   * @return a new event with updated time on the original date
   * @throws IllegalArgumentException if the new time would make start >= end
   */
  public static CalendarEvent withUpdatedTime(CalendarEvent event, String property,
                                              Object newValue, String seriesUid) {
    ZonedDateTime newDateTime = (ZonedDateTime) newValue;
    LocalTime newTime = newDateTime.toLocalTime();

    if (property.equals("start")) {
      LocalDate originalDate = event.getStartDateTime().toLocalDate();
      ZonedDateTime newStart = originalDate.atTime(newTime)
          .atZone(event.getStartDateTime().getZone());

      if (newStart.isAfter(event.getEndDateTime()) || newStart.equals(event.getEndDateTime())) {
        throw new IllegalArgumentException(
            "Start time " + newTime + " must be before end time "
                + event.getEndDateTime().toLocalTime());
      }
      return withUpdatedProperty(event, property, newStart, seriesUid);
    } else {
      LocalDate originalDate = event.getEndDateTime().toLocalDate();
      ZonedDateTime newEnd = originalDate.atTime(newTime)
          .atZone(event.getEndDateTime().getZone());

      if (!newEnd.isAfter(event.getStartDateTime())) {
        throw new IllegalArgumentException(
            "End time " + newTime + " must be after start time "
                + event.getStartDateTime().toLocalTime());
      }
      return withUpdatedProperty(event, property, newEnd, seriesUid);
    }
  }

  /**
   * Checks if a property name refers to a time property.
   *
   * @param property the property name
   * @return true if property is "start" or "end"
   */
  public static boolean isTimeProperty(String property) {
    return property.equals("start") || property.equals("end");
  }

  /**
   * Checks if series parameters would result in only a single event.
   *
   * @param parameters the series parameters
   * @return true if the series would have only one occurrence
   * @throws IllegalArgumentException if neither ndays nor untildate is specified
   */
  public static boolean isSingleDaySeries(Map<String, Object> parameters) {
    if (parameters.containsKey("ndays")) {
      int ndays = Integer.parseInt(parameters.get("ndays").toString());
      return ndays == 1;
    }
    if (parameters.containsKey("untildate")) {
      LocalDate untildate = (LocalDate) parameters.get("untildate");
      ZonedDateTime start = (ZonedDateTime) parameters.get("start");
      return untildate.equals(start.toLocalDate());
    }
    throw new IllegalArgumentException("Must specify either ndays or untildate");
  }

  /**
   * Checks if an event spans multiple days.
   *
   * @param event the event to check
   * @return true if the event starts and ends on different days
   */
  public static boolean isMultiDayEvent(CalendarEvent event) {
    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();
    return !startDate.equals(endDate);
  }

  /**
   * Sets all-day event times (8am to 5pm) in the parameter map.
   * Modifies the provided map in-place.
   *
   * @param parameters map containing "ondate" - will be updated with "start" and "end"
   */
  public static void setAllDayTimes(Map<String, Object> parameters, ZoneId timezone) {
    LocalDate date = (LocalDate) parameters.get("ondate");
    parameters.put("start", date.atTime(8, 0).atZone(timezone));
    parameters.put("end", date.atTime(17, 0).atZone(timezone));
  }

  /**
   * Preprocesses parameters for edit operations.
   * Validates edit property parameters and converts all datetime values
   * to the calendar's timezone.
   *
   * @param parameters the parameter map to preprocess.
   * @param timezone   the target timezone to convert to.
   * @throws IllegalArgumentException if validation fails.
   */
  public static void preProcess(Map<String, Object> parameters, ZoneId timezone) {
    validateEditProperty(parameters);
    convertToTimezone(parameters, timezone);
  }

  /**
   * Converts all datetime values in parameters to the calendar's timezone.
   * Modifies the parameter map in-place.
   *
   * @param parameters the parameter map containing datetime values.
   * @param targetZone the target timezone to convert to.
   */
  public static void convertToTimezone(Map<String, Object> parameters, ZoneId targetZone) {
    if (parameters.get("start") != null) {
      ZonedDateTime start = (ZonedDateTime) parameters.get("start");
      parameters.put("start", start.withZoneSameInstant(targetZone));
    }

    if (parameters.get("end") != null) {
      ZonedDateTime end = (ZonedDateTime) parameters.get("end");
      parameters.put("end", end.withZoneSameInstant(targetZone));
    }

    if (parameters.get("value") != null) {
      Object value = parameters.get("value");
      if (value instanceof ZonedDateTime) {
        parameters.put("value", ((ZonedDateTime) value).withZoneSameInstant(targetZone));
      }
    }
  }

  /**
   * Validates edit operation parameters.
   *
   * @param parameters map containing "property" and "value"
   * @throws IllegalArgumentException if parameters are missing or invalid for the property type
   */
  public static void validateEditProperty(Map<String, Object> parameters) {
    String property = (String) parameters.get("property");
    if (property == null) {
      throw new IllegalArgumentException("Must specify property");
    }
    Object value = parameters.get("value");
    if (value == null) {
      throw new IllegalArgumentException("Must specify value");
    }
    switch (property) {
      case "subject":
      case "location":
      case "description":
      case "status":
        if (((String) value).trim().isEmpty()) {
          throw new IllegalArgumentException(
              property.substring(0, 1).toUpperCase() + property.substring(1)
                  + " cannot be empty or contain only whitespace. Please provide a valid "
                  + property + " value.");
        }
        break;
      case "start":
      case "end":
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property
            + ". Valid properties are: start, end, subject, location, description, status");
    }
  }

  /**
   * Value object representing a time window with start and end times.
   */
  public static class TimeWindow {
    /**
     * The start time of the window.
     */
    public final ZonedDateTime start;

    /**
     * The end time of the window.
     */
    public final ZonedDateTime end;

    /**
     * Constructs a TimeWindow with the specified start and end times.
     *
     * @param start the start time
     * @param end the end time
     */
    public TimeWindow(ZonedDateTime start, ZonedDateTime end) {
      this.start = start;
      this.end = end;
    }
  }

  /**
   * Calculates the time window for an event copied to a specific date.
   * Preserves the original time-of-day in the target timezone.
   *
   * @param event the event to copy
   * @param targetDate the target date
   * @param targetTimezone the target timezone
   * @return TimeWindow with start and end in the target timezone
   */
  public static TimeWindow calculateTimeWindowForDate(CalendarEvent event,
                                                      LocalDate targetDate,
                                                      ZoneId targetTimezone) {
    ZonedDateTime startInTarget = event.getStartDateTime()
        .withZoneSameInstant(targetTimezone);
    ZonedDateTime endInTarget = event.getEndDateTime()
        .withZoneSameInstant(targetTimezone);

    LocalTime startTime = startInTarget.toLocalTime();
    LocalTime endTime = endInTarget.toLocalTime();

    ZonedDateTime targetStart = targetDate.atTime(startTime).atZone(targetTimezone);
    ZonedDateTime targetEnd = targetDate.atTime(endTime).atZone(targetTimezone);

    return new TimeWindow(targetStart, targetEnd);
  }

  /**
   * Calculates the time window for an event shifted by a day offset.
   * Converts to the target timezone after shifting.
   *
   * @param event the event to copy
   * @param dayOffset the number of days to shift
   * @param targetTimezone the target timezone
   * @return TimeWindow with shifted start and end in the target timezone
   */
  public static TimeWindow calculateTimeWindowWithOffset(CalendarEvent event,
                                                         long dayOffset,
                                                         ZoneId targetTimezone) {
    ZonedDateTime targetStart = event.getStartDateTime()
        .plusDays(dayOffset)
        .withZoneSameInstant(targetTimezone);
    ZonedDateTime targetEnd = event.getEndDateTime()
        .plusDays(dayOffset)
        .withZoneSameInstant(targetTimezone);

    return new TimeWindow(targetStart, targetEnd);
  }

  /**
   * Calculates the end time for a copied event based on the original duration.
   *
   * @param originalStart the original event's start time
   * @param originalEnd the original event's end time
   * @param targetStart the target start time
   * @return the calculated target end time
   */
  public static ZonedDateTime calculateTargetEnd(ZonedDateTime originalStart,
                                                 ZonedDateTime originalEnd,
                                                 ZonedDateTime targetStart) {
    long duration = ChronoUnit.SECONDS.between(originalStart, originalEnd);
    return targetStart.plusSeconds(duration);
  }
}