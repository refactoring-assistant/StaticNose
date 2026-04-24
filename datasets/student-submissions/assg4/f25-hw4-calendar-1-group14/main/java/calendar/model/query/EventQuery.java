package calendar.model.query;

import calendar.model.datetime.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable class representing query parameters for searching events.
 * Can be used to search by various combinations of criteria.
 */
public final class EventQuery {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final LocalDate date;
  private final LocalDateTime fromDateTime;
  private final LocalDateTime toDateTime;
  private final String location;
  private final Boolean isPublic;
  private final Set<DayOfWeek> weekdays;

  /**
   * Validates query parameters.
   */
  public EventQuery(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                    LocalDate date, LocalDateTime fromDateTime, LocalDateTime toDateTime,
                    String location, Boolean isPublic, Set<DayOfWeek> weekdays) {
    if (fromDateTime != null && toDateTime != null) {
      if (fromDateTime.isAfter(toDateTime)) {
        throw new IllegalArgumentException("From date/time must be before to date/time");
      }
    }

    if (startDateTime != null && fromDateTime != null) {
      if (startDateTime.isBefore(fromDateTime)) {
        throw new IllegalArgumentException("Start time conflicts with date range");
      }
    }

    if (startDateTime != null && toDateTime != null) {
      if (startDateTime.isAfter(toDateTime)) {
        throw new IllegalArgumentException("Start time conflicts with date range");
      }
    }

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.date = date;
    this.fromDateTime = fromDateTime;
    this.toDateTime = toDateTime;
    this.location = location;
    this.isPublic = isPublic;
    this.weekdays = weekdays;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public LocalDate getDate() {
    return date;
  }

  public LocalDateTime getFromDateTime() {
    return fromDateTime;
  }

  public LocalDateTime getToDateTime() {
    return toDateTime;
  }

  public String getLocation() {
    return location;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public Set<DayOfWeek> getWeekdays() {
    return weekdays;
  }

  /**
   * Creates a query for events with a specific subject.
   *
   * @param subject the event subject
   * @return a new EventQuery
   */
  public static EventQuery bySubject(String subject) {
    return new EventQuery(subject, null, null, null, null, null, null, null, null);
  }

  /**
   * Creates a query for events on a specific date.
   *
   * @param date the date to search
   * @return a new EventQuery
   */
  public static EventQuery byDate(LocalDate date) {
    return new EventQuery(null, null, null, date, null, null, null, null, null);
  }

  /**
   * Creates a query for events at a specific time.
   *
   * @param dateTime the exact date/time
   * @return a new EventQuery
   */
  public static EventQuery byDateTime(LocalDateTime dateTime) {
    return new EventQuery(null, dateTime, null, null, null, null, null, null, null);
  }

  /**
   * Creates a query for events starting at a specific time.
   *
   * @param startDateTime the start date/time
   * @return a new EventQuery
   */
  public static EventQuery byStartTime(LocalDateTime startDateTime) {
    return new EventQuery(null, startDateTime, null, null, null, null, null, null, null);
  }

  /**
   * Creates a query for events with specific subject and start time.
   *
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @return a new EventQuery
   */
  public static EventQuery bySubjectAndStart(String subject, LocalDateTime startDateTime) {
    return new EventQuery(subject, startDateTime, null, null, null, null, null, null, null);
  }

  /**
   * Creates a query for events with specific subject, start, and end times.
   * This is used for finding exact matches.
   *
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @param endDateTime   the end date/time
   * @return a new EventQuery
   */
  public static EventQuery exactMatch(String subject, LocalDateTime startDateTime,
                                      LocalDateTime endDateTime) {
    return new EventQuery(subject, startDateTime, endDateTime, null, null, null,
        null, null, null);
  }

  /**
   * Creates a query for events within a date/time range.
   *
   * @param from the start of the range
   * @param to   the end of the range
   * @return a new EventQuery
   */
  public static EventQuery inRange(LocalDateTime from, LocalDateTime to) {
    return new EventQuery(null, null, null, null, from, to, null, null, null);
  }

  /**
   * Creates a query for events within a date range (full days).
   *
   * @param fromDate the start date (inclusive)
   * @param toDate   the end date (inclusive)
   * @return a new EventQuery
   */
  public static EventQuery inDateRange(LocalDate fromDate, LocalDate toDate) {
    LocalDateTime from = fromDate.atStartOfDay();
    LocalDateTime to = toDate.atTime(23, 59, 59);
    return new EventQuery(null, null, null, null, from, to, null, null, null);
  }

  /**
   * Creates a query for all events (no filters).
   *
   * @return a new EventQuery with no restrictions
   */
  public static EventQuery all() {
    return new EventQuery(null, null, null, null, null, null, null, null, null);
  }

  /**
   * Creates a query for public or private events.
   *
   * @param isPublic true for public events, false for private
   * @return a new EventQuery
   */
  public static EventQuery byStatus(boolean isPublic) {
    return new EventQuery(null, null, null, null, null, null, null, isPublic, null);
  }

  /**
   * Creates a query for events at a specific location.
   *
   * @param location the location to search for
   * @return a new EventQuery
   */
  public static EventQuery byLocation(String location) {
    return new EventQuery(null, null, null, null, null, null, location, null, null);
  }

  /**
   * Creates a query for events on specific weekdays.
   *
   * @param weekdays the set of weekdays
   * @return a new EventQuery
   */
  public static EventQuery byWeekdays(Set<DayOfWeek> weekdays) {
    return new EventQuery(null, null, null, null, null, null, null, null, weekdays);
  }

  /**
   * Creates a new query with an additional subject filter.
   *
   * @param subject the subject to filter by
   * @return a new EventQuery with the subject set
   */
  public EventQuery withSubject(String subject) {
    return new EventQuery(subject, startDateTime, endDateTime, date,
        fromDateTime, toDateTime, location, isPublic, weekdays);
  }

  /**
   * Creates a new query with an additional date filter.
   *
   * @param date the date to filter by
   * @return a new EventQuery with the date set
   */
  public EventQuery withDate(LocalDate date) {
    return new EventQuery(subject, startDateTime, endDateTime, date,
        fromDateTime, toDateTime, location, isPublic, weekdays);
  }

  /**
   * Creates a new query with an additional location filter.
   *
   * @param location the location to filter by
   * @return a new EventQuery with the location set
   */
  public EventQuery withLocation(String location) {
    return new EventQuery(subject, startDateTime, endDateTime, date,
        fromDateTime, toDateTime, location, isPublic, weekdays);
  }

  /**
   * Creates a query for events ending at a specific time.
   *
   * @param endDateTime the end date/time
   * @return a new EventQuery
   */
  public static EventQuery byEndTime(LocalDateTime endDateTime) {
    return new EventQuery(null, null, endDateTime, null, null, null, null, null, null);
  }

  /**
   * Creates a new query with an additional status filter.
   *
   * @param isPublic true for public events only
   * @return a new EventQuery with the status set
   */
  public EventQuery withStatus(boolean isPublic) {
    return new EventQuery(subject, startDateTime, endDateTime, date,
        fromDateTime, toDateTime, location, isPublic, weekdays);
  }

  /**
   * Creates a new query with a date range filter.
   *
   * @param from the start of the range
   * @param to   the end of the range
   * @return a new EventQuery with the range set
   */
  public EventQuery withRange(LocalDateTime from, LocalDateTime to) {
    return new EventQuery(subject, startDateTime, endDateTime, date,
        from, to, location, isPublic, weekdays);
  }

  /**
   * Checks if this query has any filters set.
   *
   * @return true if any filter is non-null
   */
  public boolean hasFilters() {
    return subject != null || startDateTime != null || endDateTime != null
        || date != null || fromDateTime != null || toDateTime != null
        || location != null || isPublic != null
        || (weekdays != null && !weekdays.isEmpty());
  }

  /**
   * Checks if this is an exact match query (has subject, start, and end).
   *
   * @return true if this queries for an exact event
   */
  public boolean isExactMatch() {
    return subject != null && startDateTime != null && endDateTime != null;
  }

  /**
   * Checks if this is a range query.
   *
   * @return true if this queries for a date/time range
   */
  public boolean isRangeQuery() {
    return fromDateTime != null || toDateTime != null;
  }

  /**
   * Checks if this is a date-specific query.
   *
   * @return true if this queries for a specific date
   */
  public boolean isDateQuery() {
    return date != null;
  }

  /**
   * Gets a description of this query for display.
   *
   * @return a human-readable description
   */
  public String describe() {
    StringBuilder sb = new StringBuilder("Query for events");
    boolean hasCondition = false;

    if (subject != null) {
      sb.append(" with subject '").append(subject).append("'");
      hasCondition = true;
    }

    if (date != null) {
      sb.append(hasCondition ? " and" : "");
      sb.append(" on ").append(date);
      hasCondition = true;
    }

    if (startDateTime != null) {
      sb.append(hasCondition ? " and" : "");
      if (endDateTime != null) {
        sb.append(" from ").append(startDateTime).append(" to ").append(endDateTime);
      } else {
        sb.append(" starting at ").append(startDateTime);
      }
      hasCondition = true;
    }

    if (fromDateTime != null || toDateTime != null) {
      sb.append(hasCondition ? " and" : "");
      sb.append(" in range");
      if (fromDateTime != null) {
        sb.append(" from ").append(fromDateTime);
      }
      if (toDateTime != null) {
        sb.append(" to ").append(toDateTime);
      }
      hasCondition = true;
    }

    if (location != null) {
      sb.append(hasCondition ? " and" : "");
      sb.append(" at location '").append(location).append("'");
      hasCondition = true;
    }

    if (isPublic != null) {
      sb.append(hasCondition ? " and" : "");
      sb.append(isPublic ? " public" : " private");
      hasCondition = true;
    }

    if (weekdays != null && !weekdays.isEmpty()) {
      sb.append(hasCondition ? " and" : "");
      sb.append(" on ").append(DayOfWeek.toCodes(weekdays));
    }

    if (!hasCondition) {
      sb.append(" (all events)");
    }

    return sb.toString();
  }

  /**
   * Returns a string representation of this object.
   *
   * @return the description
   */
  @Override
  public String toString() {
    return describe();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    EventQuery that = (EventQuery) obj;
    return Objects.equals(subject, that.subject)
        && Objects.equals(startDateTime, that.startDateTime)
        && Objects.equals(endDateTime, that.endDateTime)
        && Objects.equals(date, that.date)
        && Objects.equals(fromDateTime, that.fromDateTime)
        && Objects.equals(toDateTime, that.toDateTime)
        && Objects.equals(location, that.location)
        && Objects.equals(isPublic, that.isPublic)
        && Objects.equals(weekdays, that.weekdays);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime, date, fromDateTime,
        toDateTime, location, isPublic, weekdays);
  }
}