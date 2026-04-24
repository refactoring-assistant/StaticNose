package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a calendar event.
 */
class Event implements EventInterface {
  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private EventStatus status;
  private String seriesId;

  /**
   * Create a new event with the required fields.
   *
   * @param subject the event subject
   * @param startDateTime the start date/time
   * @param endDateTime the end date/time
   */
  Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.location = null;
    this.description = null;
    this.status = EventStatus.PUBLIC;
    this.seriesId = "None";
  }

  /**
   * All day event constructor.
   *
   * @param subject the event subject
   * @param date the event date
   */
  Event(String subject, LocalDate date) {
    this(subject,
        date.atTime(8, 0, 0),
        date.atTime(17, 0, 0));
  }

  /**
   * Full constructor with all fields.
   *
   * @param subject the event subject
   * @param startDateTime the start Date/Time.
   * @param endDateTime the end Date/Time.
   * @param location the location (default is null).
   * @param description the description (default is null).
   * @param status the status of the event.
   * @param seriesId the seriesID for the event.
   */
  Event(String subject,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String location,
        String description,
        EventStatus status,
        String seriesId) {
    setSubject(subject);
    setStartDateTime(startDateTime);
    setEndDateTime(endDateTime);
    setLocation(location);
    setDescription(description);
    setStatus(status);
    setSeriesId(seriesId);
  }



  /**
   * Gets and returns the subject of {@code this}.
   *
   * @return the subject of {@code this}
   */

  @Override
  public String getSubject() {
    return subject;
  }

  /**
   * Gets and returns the startDateTime of {@code this}.
   *
   * @return the startDateTime of {@code this}
   */

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets and returns the endDateTime of {@code this}.
   *
   * @return the endDateTime of {@code this}
   */

  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Gets and returns the location of {@code this}.
   *
   * @return the location of {@code this}
   */

  @Override
  public String getLocation() {
    return location;
  }

  /**
   * Gets and returns the status of {@code this}.
   *
   * @return the status of {@code this}
   */

  @Override
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Gets and returns the seriesId of {@code this}.
   *
   * @return the seriesId of {@code this}
   */

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Gets and returns the description of {@code this}.
   *
   * @return the description of {@code this}
   */

  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Set the event subject/title.
   *
   * @param subject the new subject for the event.
   * @throws IllegalArgumentException if the subject is empty or null.
   */
  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject is not Empty");
    }
    this.subject = subject.trim();
  }

  /**
   * Set the event start Date/Time.
   *
   * @param startDateTime the new start Date/Time
   * @throws IllegalArgumentException if the startDateTime is empty or null.
   */
  @Override
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start DateTime is not Present");
    }

    if (this.endDateTime != null && !startDateTime.isBefore(this.endDateTime)) {
      throw new IllegalArgumentException("Start must be before end");
    }
    this.startDateTime = startDateTime;
  }

  /**
   * Set the event end Date/Time.
   *
   * @param endDateTime the new end Date/Time
   * @throws IllegalArgumentException if the endDateTime is empty or null.
   */
  @Override
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }

    if (this.startDateTime != null && !endDateTime.isAfter(this.startDateTime)) {
      throw new IllegalArgumentException("End must be after start");
    }

    this.endDateTime = endDateTime;
  }

  /**
   * Set the event location.
   *
   * @param location the new location, or null to clear
   */
  @Override
  public void setLocation(String location) {
    this.location = (location != null && !location.trim().isEmpty())
        ? location.trim()
        : null;
  }

  /**
   * Set the event description.
   *
   * @param description the new description, or null to clear
   */
  @Override
  public void setDescription(String description) {
    this.description = (description != null && !description.trim().isEmpty())
        ? description.trim()
        : null;
  }

  /**
   * Set the event status.
   *
   * @param status the new status
   * @throws IllegalArgumentException if status is null
   */
  @Override
  public void setStatus(EventStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    this.status = status;
  }

  /**
   * Set the series ID this event belongs to.
   *
   * @param seriesId the series ID, or "None" for non-series events
   */
  @Override
  public void setSeriesId(String seriesId) {
    this.seriesId = (seriesId != null) ? seriesId : "None";
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Event event = (Event) o;
    return Objects.equals(subject, event.subject)
        && Objects.equals(startDateTime, event.startDateTime)
        && Objects.equals(endDateTime, event.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return String.format(
        "Event - {Subject = '%s', start = %s, end = %s, location = %s, status = %s, seriesId = %s}",
        subject, startDateTime, endDateTime, location, status, seriesId
    );
  }
}
