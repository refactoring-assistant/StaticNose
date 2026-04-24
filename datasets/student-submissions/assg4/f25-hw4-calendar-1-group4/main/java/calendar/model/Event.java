package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single calendar event, including its subject, timing,
 * description, location, and status. Each event may optionally belong
 * to a recurring series identified by a seriesId.
 */
class Event {
  private String subject;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private String description;
  private EventLocation location;
  private EventStatus status;
  private String seriesId;

  /**
   * Constructs a new Event with the specified details.
   * Parses the start and end date-time strings into LocalDate and LocalTime components.
   *
   * @param subject the subject or title of the event
   * @param startDateTime the start date-time in ISO-8601 format (e.g., 2025-11-01T09:00)
   * @param endDateTime the end date-time in ISO-8601 format (e.g., 2025-11-01T10:00)
   * @param seriesId an optional identifier for recurring series; may be null
   */
  public Event(String subject, String startDateTime, String endDateTime, String seriesId) {
    this.subject = subject;
    this.seriesId = seriesId;
    convertDateTimeToFormat(startDateTime, endDateTime);
  }

  /**
   * Returns the event subject or title.
   *
   * @return the event subject or title
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject or title of the event.
   *
   * @param subject the subject to assign
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the description of the event.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description for the event.
   *
   * @param description the description to assign
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the location of the event.
   *
   * @return the event location
   */
  public EventLocation getLocation() {
    return location;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the event location
   */
  public void setLocation(EventLocation location) {
    this.location = location;
  }

  /**
   * Returns the visibility status of the event.
   *
   * @return the event status
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Sets the visibility status of the event.
   *
   * @param status the status to assign
   */
  public void setStatus(EventStatus status) {
    this.status = status;
  }

  /**
   * Returns the start date of the event.
   *
   * @return the event start date
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * Returns the end date of the event.
   *
   * @return the event end date
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Returns the start time of the event.
   *
   * @return the event start time
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Returns the end time of the event.
   *
   * @return the event end time
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * Returns the recurring series identifier, if applicable.
   *
   * @return the series ID associated with the event
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Sets the recurring series identifier for the event.
   *
   * @param seriesId the series ID to assign
   */
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Sets the end time of the event.
   *
   * @param endTime the end time to assign
   */
  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  /**
   * Sets the start time of the event.
   *
   * @param startTime the start time to assign
   */
  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  /**
   * Parses ISO-8601 formatted start and end date-time strings and
   * initializes the corresponding LocalDate and LocalTime fields.
   *
   * @param startDateTime the start date-time string (e.g., 2025-11-01T09:00)
   * @param endDateTime the end date-time string (e.g., 2025-11-01T10:00)
   */
  void convertDateTimeToFormat(String startDateTime, String endDateTime) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    LocalDateTime start = LocalDateTime.parse(startDateTime, formatter);
    LocalDateTime end = LocalDateTime.parse(endDateTime, formatter);

    this.startDate = start.toLocalDate();
    this.startTime = start.toLocalTime();

    this.endDate = end.toLocalDate();
    this.endTime = end.toLocalTime();
  }

}