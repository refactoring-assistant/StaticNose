package model;

import java.time.LocalDateTime;


/**
 * Represents an event that is part of a recurring series.
 * Each SeriesEvent instance represents a single occurrence within a series
 * but maintains a reference to its parent series through a series ID.
 *
 */
public class SeriesEvent extends AbstractEvent {

  private final String seriesId;

  /**
   * Constructs a SeriesEvent with the specified properties.
   *
   * @param subject       the subject of the event (cannot be null or empty)
   * @param startDateTime the start date and time (cannot be null)
   * @param endDateTime   the end date and time (may be null for all-day events)
   * @param description   the event description (may be null)
   * @param location      the event location (may be null)
   * @param isPublic      true if the event is public, false if private
   * @param seriesId      the unique identifier of the series (cannot be null)
   * @throws IllegalArgumentException if seriesId is null or if parent class validation fails
   */
  public SeriesEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String description, String location, boolean isPublic, String seriesId) {
    super(subject, description, startDateTime, endDateTime, isPublic, location);
    if (seriesId == null) {
      throw new IllegalArgumentException("Series ID cannot be null");
    }
    this.seriesId = seriesId;
  }

  @Override
  public boolean isPartOfSeries() {
    return true;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }
}
