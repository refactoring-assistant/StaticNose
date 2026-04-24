package calendar.model.event;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for calendar events providing common functionality.
 * Implements validation, property access, and equality comparison for all event types.
 *
 * <p>All event properties are immutable. To modify an event, create a new instance
 * with the updated properties.
 */
public abstract class AbstractCalendarEvent implements CalendarEvent {
  /**
   * The subject/title of the event (required).
   */
  protected final String subject;

  /**
   * The start date and time (required).
   */
  protected final ZonedDateTime startDateTime;

  /**
   * Optional description of the event.
   */
  protected final String description;

  /**
   * The end date and time (required).
   */
  protected final ZonedDateTime endDateTime;

  /**
   * Optional location ("physical" or "online").
   */
  protected final String location;

  /**
   * Optional status ("public" or "private").
   */
  protected final String status;

  /**
   * The UID of the series this event belongs to (null for standalone events).
   */
  protected final String seriesUid;

  /**
   * Constructs a new calendar event with validation.
   *
   * @param params    map of event parameters
   * @param seriesUid the series UID, or null for standalone events
   * @throws IllegalArgumentException if required parameters are missing or invalid
   */
  public AbstractCalendarEvent(Map<String, Object> params, String seriesUid) {
    validateParams(params);
    this.subject = (String) params.get("subject");
    this.startDateTime = (ZonedDateTime) params.get("start");
    this.description = (String) params.getOrDefault("description", null);
    this.endDateTime = (ZonedDateTime) params.getOrDefault("end", null);
    this.location = (String) params.getOrDefault("location", null);
    this.status = (String) params.getOrDefault("status", null);
    this.seriesUid = seriesUid;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public ZonedDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public ZonedDateTime getEndDateTime() {
    return endDateTime;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public String getSeriesUid() {
    return this.seriesUid;
  }

  /**
   * Returns a map representation of this event's properties.
   * Does not include series-specific metadata.
   *
   * @return map containing subject, start, end, location, status, and description
   */
  @Override
  public Map<String, Object> getHashMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("subject", this.getSubject());
    map.put("start", this.getStartDateTime());
    map.put("end", this.getEndDateTime());
    map.put("location", this.getLocation());
    map.put("status", this.getStatus());
    map.put("description", this.getDescription());
    return map;
  }

  /**
   * Checks if this event is part of a series.
   * Default implementation returns false; subclasses may override.
   *
   * @return false for base implementation
   */
  @Override
  public abstract boolean isPartOfSeries();

  /**
   * Validates all event parameters.
   *
   * @param params the parameter map to validate
   * @throws IllegalArgumentException if any validation fails
   */
  private void validateParams(Map<String, Object> params) {
    if (params == null) {
      throw new IllegalArgumentException("Parameters must not be null");
    }
    String subject = (String) params.get("subject");
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    ZonedDateTime startDateTime = (ZonedDateTime) params.get("start");
    ZonedDateTime endDateTime = (ZonedDateTime) params.get("end");
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Dates cannot be null");
    }
    if (!startDateTime.isBefore(endDateTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    validateLocation(params);
    validateStatus(params);
  }

  /**
   * Validates the location parameter if present.
   *
   * @param params the parameter map
   * @throws IllegalArgumentException if location is not "physical" or "online"
   */
  private void validateLocation(Map<String, Object> params) {
    Object locationObj = params.get("location");
    if (locationObj != null) {
      String location = locationObj.toString().toLowerCase();
      if (!location.equals("physical") && !location.equals("online")) {
        throw new IllegalArgumentException(
            "Location must be either 'physical' or 'online'"
        );
      }
    }
  }

  /**
   * Validates the status parameter if present.
   *
   * @param params the parameter map
   * @throws IllegalArgumentException if status is not "private" or "public"
   */
  private void validateStatus(Map<String, Object> params) {
    Object statusObj = params.get("status");
    if (statusObj != null) {
      String status = statusObj.toString().toLowerCase();
      if (!status.equals("private") && !status.equals("public")) {
        throw new IllegalArgumentException(
            "Status must be either 'private' or 'public'"
        );
      }
    }
  }

  /**
   * Compares this event with another for equality.
   * Two events are equal if they have the same subject, start time, and end time.
   *
   * @param obj the object to compare with
   * @return true if the events are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CalendarEvent)) {
      return false;
    }

    CalendarEvent other = (CalendarEvent) obj;
    return this.subject.equals(other.getSubject())
        && this.startDateTime.equals(other.getStartDateTime())
        && this.endDateTime.equals(other.getEndDateTime());
  }

  /**
   * Returns a hash code for this event based on subject, start time, and end time.
   *
   * @return the hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }
}