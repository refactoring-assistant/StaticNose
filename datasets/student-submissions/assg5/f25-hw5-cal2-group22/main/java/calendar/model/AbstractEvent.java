package calendar.model;

import calendar.model.enums.EventLocation;
import calendar.model.enums.EventStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

/**
 * Represents the base abstraction for a calendar event. This class provides the common
 * properties and behaviors shared by all types of events, including single and
 * recurring event instances.
 */
public abstract class AbstractEvent {
  protected static final DateTimeFormatter DF =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  protected String subject;
  protected LocalDateTime start;
  protected LocalDateTime end;
  protected EventLocation location;
  protected String description;
  protected EventStatus status;
  protected ZoneId timezone;


  /**
   * Constructs a new event with the given details.
   *
   * @param builder the builder containing event properties
   */
  protected AbstractEvent(AbstractEventBuilder<?> builder) {
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.location = builder.location;
    this.description = builder.description;
    this.status = builder.status;
    this.timezone = builder.timezone;
  }

  static EventLocation parseLocation(String newValue) {
    if (newValue == null || newValue.isEmpty()) {
      return EventLocation.NONE;
    }
    Map<String, EventLocation> locationMap = Map.of(
        "PHYSICAL", EventLocation.PHYSICAL,
        "ONLINE", EventLocation.ONLINE,
        "NONE", EventLocation.NONE
    );
    return locationMap.getOrDefault(newValue.trim().toUpperCase(), EventLocation.NONE);
  }

  static EventStatus parseStatus(String newValue) {
    if (newValue == null || newValue.isEmpty()) {
      return EventStatus.NONE;
    }
    Map<String, EventStatus> statusMap = Map.of(
        "PUBLIC", EventStatus.PUBLIC,
        "PRIVATE", EventStatus.PRIVATE,
        "NONE", EventStatus.NONE
    );
    return statusMap.getOrDefault(newValue.trim().toUpperCase(), EventStatus.NONE);

  }

  /**
   * Create a pre-populated builder from this event's current state.
   * Concrete classes must return their concrete builder.
   */
  public abstract AbstractEventBuilder<?> toBuilder();

  /**
   * Method to return subject.
   *
   * @return the event’s subject or title
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Method to return start date time.
   *
   * @return the event’s start date time
   */
  public LocalDateTime getStart() {
    return start;
  }

  /**
   * Method to return end date time.
   *
   * @return the event’s end date time
   */
  public LocalDateTime getEnd() {
    return end;
  }

  /**
   * Method to return description.
   *
   * @return the event’s description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Method to return location (physical or online).
   *
   * @return the event’s subject or title
   */
  public EventLocation getLocation() {
    return location;
  }

  /**
   * Method to return status (private or public).
   *
   * @return the event’s status
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Method to return time zone.
   *
   * @return the event’s time zone
   */
  public ZoneId getTimeZone() {
    return timezone;
  }

  /**
   * Returns the event's timezone as a string ID.
   *
   * @return the event's timezone ID (e.g., "America/New_York")
   */
  public String getTimeZoneId() {
    return timezone != null ? timezone.getId() : "";
  }

  /**
   * Generic editing used by CalendarImpl.
   */
  public AbstractEvent editProperty(EventProperty property, String newValue) {
    if (property == null) {
      throw new IllegalArgumentException("Property cannot be null.");
    }
    AbstractEvent updated = property.apply(this, newValue);

    this.subject = updated.subject;
    this.start = updated.start;
    this.end = updated.end;
    this.location = updated.location;
    this.description = updated.description;
    this.status = updated.status;
    this.timezone = updated.timezone;

    return this;
  }

  /**
   * Method to return convert to string.
   *
   * @return the event to string
   */
  @Override
  public String toString() {
    String dayAbbrev = start.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

    String startStr = start.format(DF);
    String endStr = end.format(DF);

    String loc = (location == null || location == EventLocation.NONE) ? "" : location.name();
    String desc = (description == null || description.isEmpty()) ? "-" : description;
    String stat = (status == null || status == EventStatus.NONE) ? "" : status.name();
    return String.format("%s | %s | %s to %s | %s | %s | %s |",
        subject, dayAbbrev, startStr, endStr, loc, desc, stat
    );
  }

  /**
   * Abstract builder class for events.
   *
   * @param <T> the concrete builder type
   */
  protected abstract static class AbstractEventBuilder<T extends AbstractEventBuilder<T>> {
    protected String subject;
    protected LocalDateTime start;
    protected LocalDateTime end;
    protected EventLocation location = EventLocation.NONE;
    protected String description = "";
    protected EventStatus status = EventStatus.NONE;
    protected ZoneId timezone;

    /**
     * Sets the event subject.
     *
     * @param subject the event subject
     * @return the builder
     */
    public T subject(String subject) {
      this.subject = subject;
      return returnBuilder();
    }

    /**
     * Sets the event start time.
     *
     * @param start the start time
     * @return the builder
     */
    public T start(LocalDateTime start) {
      this.start = start;
      return returnBuilder();
    }

    /**
     * Sets the event start time.
     *
     * @param end the start time
     * @return the builder
     */
    public T end(LocalDateTime end) {
      this.end = end;
      return returnBuilder();
    }

    /**
     * Sets the event location.
     *
     * @param location the event location
     * @return the builder
     */
    public T location(EventLocation location) {
      this.location = (location == null) ? EventLocation.NONE : location;
      return returnBuilder();
    }

    /**
     * Sets the event description.
     *
     * @param description the event description
     * @return the builder
     */
    public T description(String description) {
      this.description = (description == null) ? "" : description;
      return returnBuilder();
    }

    /**
     * Sets the event status.
     *
     * @param status the event status
     * @return the builder
     */
    public T status(EventStatus status) {
      this.status = (status == null) ? EventStatus.NONE : status;
      return returnBuilder();
    }

    /**
     * Sets the time zone.
     *
     * @param timezone - time zone
     * @return the builder
     */
    public T timezone(ZoneId timezone) {
      this.timezone = timezone;
      return returnBuilder();
    }


    /**
     * Copy this builder's fields from an event (for edits).
     */
    public T copyFrom(AbstractEvent e) {
      this.subject = e.subject;
      this.start = e.start;
      this.end = e.end;
      this.location = e.location == null ? EventLocation.NONE : e.location;
      this.description = e.description == null ? "" : e.description;
      this.status = e.status == null ? EventStatus.NONE : e.status;
      this.timezone = e.timezone;
      return returnBuilder();
    }

    /**
     * Builds the event.
     *
     * @return the built event
     */
    public abstract AbstractEvent build();

    /**
     * Returns the concrete builder instance.
     *
     * @return the builder
     */
    protected abstract T returnBuilder();
  }
}
