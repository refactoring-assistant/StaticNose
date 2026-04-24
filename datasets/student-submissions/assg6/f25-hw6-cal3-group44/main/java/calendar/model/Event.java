package calendar.model;

import calendar.model.datatypes.EventStatus;
import calendar.model.datatypes.Location;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.EventReadOnly;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single event in the calendar.
 */

public class Event implements EventReadOnly {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final Location location;
  private final EventStatus eventStatus;
  private final TypeOfEvent eventType;
  private final UUID eventId;
  private final boolean allDay;


  /**
   * Private constructs an Event with the given parameters.
   *
   * @param subject       the title of the event (String)
   * @param startDateTime starting date and time of the event (LocalDateTime - yyyy-MM-dd'T'HH:mm).
   * @param endDateTime   end date and time of the event (LocalDateTime - yyyy-MM-dd'T'HH:mm).
   * @param description   the contents or the description of the event. (String)
   * @param location      the location of the event,
   *                      represented using Enum location(PHYSICAL, ONLINE, UNKNOWN)
   * @param status        the status of the event,
   *                      represented using Enum Event status (PUBLIC, PRIVATE, UNKNOWN)
   * @param type          the type of event,
   *                      represented using Enum TypeOfEvent (SERIES, SINGLE)
   * @param eventId       UUID - unique id for each event (series events will have same id)
   * @param allDay        Represents if it is an all day event, true represents an all event.
   */
  private Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                String description, Location location, EventStatus status, TypeOfEvent type,
                UUID eventId, boolean allDay) {

    if (endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("Error: endDateTime cannot be before startDateTime");
    }

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.description = description;
    this.endDateTime = endDateTime;
    this.location = location;
    this.eventStatus = status;
    this.eventType = type;
    this.eventId = eventId;
    this.allDay = allDay;
  }

  /**
   * Compares this event to another object for equality.
   * Two events are considered equal if they share the same subject.
   * start date/time, and end date/time.
   *
   * @param o the reference object with which to compare.
   * @return true if they are equal else false.
   */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return subject.equals(event.subject)
        && startDateTime.equals(event.startDateTime)
        && endDateTime.equals(event.endDateTime);
  }

  /**
   * Returns the hash code value for this event.
   * The hash code is based on the subject, start date/time, and end date/time.
   *
   * @return the hashcode value of the event.
   */

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  /**
   * Returns a string representation of the event.
   *
   * @return a formatted string containing event details.
   */

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Subject: ").append(subject).append(", ");
    sb.append("Start: ").append(startDateTime).append(", ");
    sb.append("End: ").append(endDateTime).append(", ");
    sb.append("Description: ").append(description).append(", ");
    if (location != Location.UNKNOWN) {
      sb.append("Location: ").append(location).append(", ");
    }
    if (eventStatus != EventStatus.UNKNOWN) {
      sb.append("Status: ").append(eventStatus).append(", ");
    }
    sb.append("Type: ").append(eventType).append(", ");
    sb.append("AllDayEvent: ").append(allDay);
    return sb.toString();
  }

  @Override
  public String getSubject() {
    return this.subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return this.startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return this.endDateTime;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public Location getLocation() {
    return this.location;
  }

  @Override
  public EventStatus getEventStatus() {
    return this.eventStatus;
  }

  @Override
  public TypeOfEvent getEventType() {
    return this.eventType;
  }

  @Override
  public UUID getId() {
    return this.eventId;
  }

  @Override
  public boolean isAllDay() {
    return this.allDay;
  }

  /**
   * A builder class used to build and create a Event class.
   * It follows the builder patterns to simplify creation of Event.
   * Helps in chaining and setting the values.
   */

  public static class EventBuilder {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description;
    private Location location;
    private EventStatus eventStatus;
    private TypeOfEvent eventType;
    private UUID eventId;
    private boolean allDay;

    /**
     * We first initialize a event builder with.
     * mandatory fields subject and StartDateTime from the user.
     */
    public EventBuilder(String subject, LocalDateTime startDateTime) {
      if (subject == null || startDateTime == null) {
        throw new IllegalArgumentException("Error: subject and startDateTime cannot be null");
      }

      this.subject = subject;
      this.startDateTime = startDateTime;
      this.endDateTime = startDateTime.plusHours(1);
      this.description = "No description given";
      this.location = Location.UNKNOWN;
      this.eventStatus = EventStatus.UNKNOWN;
      this.eventType = TypeOfEvent.SINGLE;
      this.eventId = UUID.randomUUID();
      this.allDay = false;
    }

    /**
     * Creates a new EventBuilder initialized with the values of an existing event.
     *
     * @param event the event whose values will be copied into the builder
     */
    public EventBuilder(EventReadOnly event) {
      this.subject = event.getSubject();
      this.startDateTime = event.getStartDateTime();
      this.endDateTime = event.getEndDateTime();
      this.description = event.getDescription();
      this.location = event.getLocation();
      this.eventStatus = event.getEventStatus();
      this.eventType = event.getEventType();
      this.eventId = event.getId();
      this.allDay = event.isAllDay();
    }

    /**
     * Sets the subject for the builder event.
     */
    public EventBuilder setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start date and time for the builder event.
     */
    public EventBuilder setStartDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date and time for the builder event.
     */
    public EventBuilder setEndDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the description for the builder event.
     */
    public EventBuilder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location for the builder event.
     */
    public EventBuilder setLocation(Location location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event status for the builder event.
     */
    public EventBuilder setEventStatus(EventStatus eventStatus) {
      this.eventStatus = eventStatus;
      return this;
    }

    /**
     * Sets the event type for the builder event.
     */
    public EventBuilder setEventType(TypeOfEvent eventType) {
      this.eventType = eventType;
      return this;
    }

    /**
     * Sets the uuid for the builder event.
     */
    public EventBuilder setEventId(UUID eventId) {
      this.eventId = eventId;
      return this;
    }

    /**
     * Sets the end date and time for the builder event.
     */
    public EventBuilder setAllDay(boolean allDay) {
      this.allDay = allDay;
      return this;
    }


    /**
     * Builds and returns a instance of Event.
     *
     * @return a fully built Event object.
     */
    public EventReadOnly build() {
      return new Event(this.subject, this.startDateTime, this.endDateTime, this.description,
          this.location, this.eventStatus, this.eventType, this.eventId, this.allDay);
    }
  }
}
