package calendar.model;

import java.time.LocalDateTime;

/**
 * Interface for events.
 * */
public interface EventsInterface {

  /**
   * Function to get subject. Subject is required for every event.
   *
   * @return the event's subject
   * */
  String getSubject();

  /**
   * Function to get start time of event.
   *
   * @return the start date and time
   * */
  LocalDateTime getStartTime();

  /**
   * Function to get end time of event.
   *
   * @return the end date and date
   * */
  LocalDateTime getEndTime();

  /**
   * Function to get long description of an event.
   *
   * @return the description of the event
   * */
  String getDescription();

  /**
   * Function to obtain physical or online location of event.
   *
   * @return the event's location, null if not set
   * */
  String getLocation();

  /**
   * Function to determine whether event is public or private.
   *
   * @return the status of event
   * */
  EventStatus getStatus();

  /**
   * Function to determine if the event occurs all day.
   *
   * @return true if the event is all day event else false
   * */
  boolean isAllDay();

  /**
   * Gets the series ID of event from recurring series.
   *
   * @return the series id, null if not set
   * */
  String getIdSeries();

  /**
   * Gets the original start time to track series, determines
   * if event has been modified from original start time.
   *
   * @return the original start time, null if not set
   * */
  LocalDateTime getInitStart();

  /**
   * Setter function for subject.
   *
   * @param subject the event's subject
   * */
  void setSubject(String subject);

  /**
   * Setter function for start date of the event.
   *
   * @param startDate the event's start date
   * */
  void setStartTime(LocalDateTime startDate);

  /**
   * Setter function for end date of the event.
   *
   * @param endDate the event's end date
   * */
  void setEndTime(LocalDateTime endDate);

  /**
   * Setter function for event description.
   *
   * @param description the description to set
   * */
  void setDescription(String description);

  /**
   * Setter function for location of the event.
   *
   * @param location the event's location
   * */
  void setLocation(String location);

  /**
   * Setter function for series id of an event series.
   *
   * @param seriesId the provided series id
   * */
  void setIdSeries(String seriesId);

  /**
   * Setter function to set the status of an event.
   *
   * @param status the provided status
   * */
  void setStatus(String status);

  /**
   * Setter function for start time to track series.
   *
   * @param start the original start date and time
   * */
  void setInitStart(LocalDateTime start);

  /**
   * Create a unique key for event identification.
   *
   * @return the unique identifier
   * */
  String getId();
}
