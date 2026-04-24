package calendar.event;

import calendar.enums.EventStatus;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a calendar event with a subject, start date & time,
 * and optional details like end date & time, description, location and status.
 */
public class Event implements EventInterface {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private String description;
  private String location;
  private EventStatus status;
  private String seriesId;
  private static final LocalTime START_TIME = LocalTime.of(8, 0);
  private static final LocalTime END_TIME = LocalTime.of(17, 0);

  /**
   * Constructor with the use of eventBuilder.
   *
   * @param builder the builder object containing the event details.
   * @throws IllegalArgumentException if the end date/time is before the start date/time.
   */
  private Event(EventBuilder builder) throws IllegalArgumentException {
    this.subject = builder.subject;
    if (builder.isAllDayEvent && builder.end == null) {
      this.start = builder.start.with(START_TIME);
      this.end = builder.start.with(END_TIME);
    } else {
      this.start = builder.start;
      this.end = builder.end;
    }
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = builder.seriesId;
    if (this.end != null && this.end.isBefore(this.start)) {
      throw new IllegalArgumentException("End date/time must be after start date/time.");
    }
  }

  /**
   * Static factory method to create a new EventBuilder.
   *
   * @param subject the subject of the event
   * @param start the start date & time of the event
   * @return a new EventBuilder instance
   */
  public static EventBuilder builder(String subject, LocalDateTime start) {
    return new EventBuilder(subject, start);
  }

  /**
   * Builder class for constructing an Event object.
   */
  public static class EventBuilder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String description;
    private String location;
    private EventStatus status = EventStatus.PUBLIC;
    public boolean isAllDayEvent = false;
    private String seriesId;

    /**
     * Constructs a new EventBuilder with given information.
     *
     * @param subject the subject of the event
     * @param start the start date & time of the event
     */
    public EventBuilder(String subject, LocalDateTime start) {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }
      if (start == null) {
        throw new IllegalArgumentException("Start date cannot be null");
      }
      this.subject = subject.trim();
      this.start = start;
    }

    /**
     * Sets the optional end date & time of the event.
     *
     * @param end the end date & time
     * @return the builder instance
     */
    public EventBuilder end(LocalDateTime end) {
      this.end = end;
      this.isAllDayEvent = false;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the event description
     * @return the builder instance
     */
    public EventBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param location the event location
     * @return the builder instance
     */
    public EventBuilder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status (public or private) of the event.
     *
     * @param status a string representing the status
     * @return the builder instance
     */
    public EventBuilder status(String status) {
      if (status == null || status.trim().isEmpty()) {
        return this;
      }
      if (status.equals("private")) {
        this.status = EventStatus.PRIVATE;
      } else {
        this.status = EventStatus.PUBLIC;
      }
      return this;
    }

    /**
     * Marks the event as an all-day event.
     * If an end date/time is not explicitly set, the event will span from 8:00 to 17:00.
     *
     * @return the builder instance
     */
    public EventBuilder setAllDayEvent() {
      this.isAllDayEvent = true;
      return this;
    }

    /**
     * Sets the series ID for recurring events.
     *
     * @param seriesId the ID of the event series
     * @return the builder instance
     */
    public EventBuilder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Creates and returns the final event object.
     *
     * @return the constructed event
     */
    public Event build() {
      return new Event(this);
    }
  }

  /**
   * Checks if the event is considered an all-day event.
   *
   * @return true if the event is an all-day event, false otherwise.
   */
  public boolean isAllDayEvent() {
    if (end != null && start.toLocalTime().equals(START_TIME)
        && end.toLocalTime().equals(END_TIME)) {
      return true;
    }
    return false;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return start;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return end;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public EventStatus getEventStatus() {
    return status;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public void editDescription(String newDescription) {
    this.description = newDescription;
  }

  @Override
  public void editLocation(String newLocation) {
    this.location = newLocation;
  }

  @Override
  public void editEventStatus(String newEventStatus) {
    if (newEventStatus.equals("private")) {
      this.status = EventStatus.PRIVATE;
    } else {
      this.status = EventStatus.PUBLIC;
    }
  }
}

