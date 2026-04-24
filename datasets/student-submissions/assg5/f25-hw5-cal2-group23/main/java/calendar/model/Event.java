package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a single event in the calendar, which may be atimed or all-day-event.
 */
public abstract class Event {
  private String eventName;
  private String notes;
  private String location;
  private String status;
  private String seriesId;

  /**
   * Creates a new event.
   *
   * @param eventName name of the event
   * @param notes optional notes
   * @param location optional location
   * @param status status public or private
   * @param seriesId the recurring series ID
   */
  public Event(String eventName,  String notes,
               String location, String status, String seriesId) {

    if (eventName == null) {
      throw new IllegalArgumentException("eventName cannot be null");
    }
    this.eventName = eventName;
    this.notes = notes;
    this.location = location;
    this.status = status;
    this.seriesId = seriesId;

  }

  public String getEventName() {
    return eventName;
  }

  /** Returns start time of the event. */

  public abstract LocalDateTime getStartTime();

  /** Returns end time of the event. */

  public abstract LocalDateTime getEndTime();

  public String getNotes() {
    return notes;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  public String getSeriesId() {
    return seriesId;
  }

  /** Sets the name of the event. */
  public void setEventName(String eventName) {
    if (eventName == null) {
      throw new IllegalArgumentException("eventName cannot be null");
    }
    this.eventName = eventName;
  }


  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  /** Determines if event is all day. */
  public abstract boolean isAllDay();

  /** Checks if event occurs on given date. */

  public abstract boolean occursOn(LocalDate date);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event other = (Event) o;
    return Objects.equals(eventName, other.eventName)
        &&
        Objects.equals(getStartTime(), other.getStartTime())
        && Objects.equals(getEndTime(), other.getEndTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventName, getStartTime(), getEndTime());
  }

  /** Returns actual end time of the event. */

  public LocalDateTime getActualEndTime() {
    if (getEndTime() == null) {
      return getStartTime().withHour(17).withMinute(0);
    }
    return getEndTime();
  }


  @Override
  public String toString() {
    return "Event: " + eventName
        +
        " from " + getStartTime() + " to "
        + (getEndTime() == null ? "end of the day" : getEndTime());
  }

  /**
   * Creates a copy of this event instance.
   *
   * @param newStartTime the new start date/time for the copied event.
   * @param newEndTime the new end date/time for the copied event.
   * @param newSeriesId the series ID for the copied event.
   * @return a new Event object with updated times and series ID.
   */
  public abstract Event copy(LocalDateTime newStartTime, LocalDateTime newEndTime,
                             String newSeriesId);

  /**
   * checks if the event occurs within the specified date-time interval.
   *
   * @param start The start of the interval.
   * @param end The end of the interval.
   * @return true if the event overlaps with the interval, false otherwise.
   */
  public abstract boolean occursInInterval(LocalDateTime start, LocalDateTime end);


}
