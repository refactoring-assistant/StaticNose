package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a Timed Event.
 */
public class TimedEvent extends Event {

  private LocalDateTime startTime;
  private LocalDateTime endTime;

  /**
   * Constructor of a Timed -event.
   *
   * @param eventName the event name
   * @param notes     notes
   * @param location  location
   * @param status    status whether public or private
   * @param seriesId  seriesId
   * @param startTime the startTime of the event
   * @param endTime   the endTime of the event
   */
  public TimedEvent(String eventName, String notes,
                    String location, String status, String seriesId, LocalDateTime startTime,
                    LocalDateTime endTime) {
    super(eventName, notes, location, status, seriesId);
    if (startTime == null) {
      throw new NullPointerException("startTime or endTime is null");
    }
    if (endTime != null && endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("endTime is before startTime is null");
    }
    this.startTime = startTime;
    this.endTime = endTime;
  }

  @Override
  public LocalDateTime getStartTime() {
    return startTime;
  }

  @Override
  public LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * Sets the start date of the event.
   *
   * @param startTime the new start date
   */
  public void setStartTime(LocalDateTime startTime) {
    if (startTime == null) {
      throw new IllegalArgumentException("startTime cannot be null");
    }
    if (endTime != null && startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("startTime cannot be after endTime date");
    }
    this.startTime = startTime;
  }

  /**
   * Sets the end time of the event.
   *
   * @param endTime the new endtime
   */
  public void setEndTime(LocalDateTime endTime) {
    if (endTime != null && endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("endTime cannot be before startTime date");
    }
    this.endTime = endTime;
  }

  @Override
  public boolean isAllDay() {
    return false;
  }

  @Override
  public boolean occursOn(LocalDate date) {
    LocalDate startDate = startTime.toLocalDate();
    LocalDate endDate = endTime != null ? endTime.toLocalDate() : startDate;
    return (!startDate.isAfter(date) && !endDate.isBefore(date));
  }

  @Override
  public Event copy(LocalDateTime newStartTime, LocalDateTime newEndTime, String newSeriesId) {
    return new TimedEvent(
        this.getEventName(),
        this.getNotes(),
        this.getLocation(),
        this.getStatus(),
        newSeriesId,
        newStartTime,
        newEndTime
    );
  }

  @Override
  public boolean occursInInterval(LocalDateTime start, LocalDateTime end) {
    return this.getStartTime().isBefore(end) && this.getActualEndTime().isAfter(start);
  }

}

