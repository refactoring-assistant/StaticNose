package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Class with getter and setter functions to create events.
 * */
public class Events implements EventsInterface {
  private final String uniqueId;
  private EventStatus eventStatus;
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private final boolean isAllDay;
  private String description;
  private String location;
  private String seriesId;
  private LocalDateTime originalStart;

  /**
   * Constructor to initialise event.
   *
   * @param subject the event's subject
   * @param start the event's start date
   * @param end the event's end date
   * */
  public Events(String subject, LocalDateTime start, LocalDateTime end) {
    this.uniqueId = UUID.randomUUID().toString();
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.isAllDay = false;
    this.eventStatus = EventStatus.PUBLIC;
  }

  /**
   * Constructor for all day event.
   * An all day event begins at 8AM and ends at 5PM.
   *
   * @param subject the event's subject
   * @param date the date the event occurred
   * */
  public Events(String subject, LocalDate date) {
    this.uniqueId = UUID.randomUUID().toString();
    this.subject = subject;
    this.originalStart = date.atTime(8, 0);
    this.start = date.atTime(8, 0);
    this.end = date.atTime(17, 0);
    this.isAllDay = true;
    this.eventStatus = EventStatus.PUBLIC;
  }

  /**
   * Copy constructor to create new instance of event.
   * Used in EventSeries to generate new event instances for recurrence.
   *
   * @param other event to copy from
   * @param start start time for new instance
   * @param end end time for new event instance
   * */
  public Events(Events other, LocalDateTime start, LocalDateTime end) {
    this.uniqueId = other.uniqueId;
    this.subject = other.subject;
    this.start = start;
    this.end = end;
    this.isAllDay = other.isAllDay;
    this.eventStatus = other.eventStatus;
    this.description = other.description;
    this.location = other.location;
    this.seriesId = other.seriesId;
    this.originalStart = other.originalStart;
  }

  /**
   * Getter function to obtain subject of event.
   * */
  @Override
  public String getSubject() {
    return this.subject;
  }

  /**
   * Getter function to obtain start time of the event.
   *
   * @return the start time
   * */
  @Override
  public LocalDateTime getStartTime() {
    return this.start;
  }

  /**
   * Getter function to obtain end time of event.
   *
   * @return the event's end date
   * */
  @Override
  public LocalDateTime getEndTime() {
    return this.end;
  }

  /**
   * Getter function to implement optional feature of obtaining long description of event.
   *
   * @return the event's long description
   * */
  @Override
  public String getDescription() {
    return this.description;
  }

  /**
   * Getter function to implement optional feature to obtain location of event.
   * Location maybe physical or online.
   *
   * @return the event's location
   * */
  @Override
  public String getLocation() {
    return this.location;
  }

  /**
   * Function to determine if event is public or private.
   *
   * @return the event status
   * */
  @Override
  public EventStatus getStatus() {
    return this.eventStatus;
  }

  /**
   * Function to check whether event is all day event or not.
   * All day events are from 8AM - 5PM.
   *
   * @return whether the event is an all day event or not.
   * */
  @Override
  public boolean isAllDay() {
    return this.isAllDay;
  }

  /**
   * Gets the series ID of event from recurring series.
   *
   * @return the series id
   * */
  @Override
  public String getIdSeries() {
    return seriesId;
  }

  /**
   * Function to fetch original start date of a series for tracking any modifications.
   *
   * @return the very initial start date
   * */
  @Override
  public LocalDateTime getInitStart() {
    return originalStart;
  }

  /**
   * Setter function to set the subject of the event.
   *
   * @param subject the provided subject
   * */
  @Override
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Setter function to set the start time of event.
   *
   * @param start the event's start date
   * */
  @Override
  public void setStartTime(LocalDateTime start) {
    this.originalStart = this.start;
    this.start = start;
  }

  /**
   * Setter function to set end time of event.
   *
   * @param end the event's end time
   * */
  @Override
  public void setEndTime(LocalDateTime end) {
    this.end = end;
  }

  /**
   * Setter function to set the description.
   *
   * @param description the event's description
   * */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Setter function to set the location of an event.
   *
   * @param location the event's location
   * */
  @Override
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Setter function to assign id to a series.
   *
   * @param seriesId the series id
   * */
  @Override
  public void setIdSeries(String seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Setter function to assign the status to event.
   *
   * @param status the status of event
   * @throws IllegalStateException if mentioned status is not public or private
   * */
  @Override
  public void setStatus(String status) {
    if (status.equals("public")) {
      this.eventStatus = EventStatus.PUBLIC;
    } else if (status.equals("private")) {
      this.eventStatus = EventStatus.PRIVATE;
    } else {
      throw new IllegalStateException("Invalid event status");
    }
  }

  /**
   * Setter function to assign original start date to a series.
   * This function is important when checking for modifications to start date of series.
   *
   * @param start the original start date of the series
   * */
  @Override
  public void setInitStart(LocalDateTime start) {
    originalStart = start;
  }

  /**
   * Getter function to fetch unique id of event.
   *
   * @return the event's unique id
   * */
  @Override
  public String getId() {
    return this.subject + "/" + this.start + "/" + this.end;
  }
}
