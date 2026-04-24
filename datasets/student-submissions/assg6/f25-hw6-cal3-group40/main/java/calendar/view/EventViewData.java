package calendar.view;

import java.time.Instant;

/**
 * Data Transfer Object for Event data to be used in the View layer.
 * This decouples the View from the Model's Event interface.
 */
public class EventViewData {
  private final String subject;
  private final Instant start;
  private final Instant end;
  private final String description;
  private final String location;
  private final boolean isPrivate;
  private final String seriesId;
  private final boolean isSeries;

  /**
   * Initializes the EventViewData object.
   *
   * @param subject Subject of the event.
   * @param start Start date and time of the event.
   * @param end End date and time of the event.
   * @param description Description of the event,
   * @param location Location of the event.
   * @param isPrivate If the event is private.
   * @param seriesId Series id of the event.
   * @param isSeries If it is a series.
   */
  public EventViewData(String subject, Instant start, Instant end, String description,
                       String location,
                       boolean isPrivate, String seriesId, boolean isSeries) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.isPrivate = isPrivate;
    this.seriesId = seriesId;
    this.isSeries = isSeries;
  }

  /**
   * Gets the subject of the event.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time
   */
  public Instant getStart() {
    return start;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time
   */
  public Instant getEnd() {
    return end;
  }

  /**
   * Gets the description of the event.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks if the event is private.
   *
   * @return true if private, false otherwise
   */
  public boolean isPrivate() {
    return isPrivate;
  }

  /**
   * Gets the series ID if this event is part of a series.
   *
   * @return the series ID, or null if not part of a series
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Checks if the event is part of a series.
   *
   * @return true if part of a series, false otherwise
   */
  public boolean isSeries() {
    return isSeries;
  }
}