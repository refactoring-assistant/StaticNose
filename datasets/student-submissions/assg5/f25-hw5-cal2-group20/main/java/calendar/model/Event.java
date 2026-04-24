package calendar.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents an Event class implementation of EventObject can be a part of Event Series.
 * Contains mandatory fields, such as subject, startDateTime, and includes optional ones, such as
 * longer description, location, and so on.
 */
public class Event implements EventObject {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String description;
  private final LocalDateTime endDateTime;
  private final EventLocation location;
  private final EventStatus status;
  private final int eventSeriesId;

  // default start and end times of an all day event
  public static final LocalTime DEFAULT_START_TIME = LocalTime.of(8, 0);
  public static final LocalTime DEFAULT_END_TIME = LocalTime.of(17, 0);

  private Event(EventBuilder builder) throws IllegalArgumentException, NullPointerException {
    if (builder.subject == null || builder.startDateTime == null || builder.subject.isEmpty()) {
      throw new NullPointerException(
        "An event is required to have a subject, start date and time");
    }
    this.subject = builder.subject;
    if (builder.endDateTime == null) {
      this.startDateTime =
        LocalDateTime.of(builder.startDateTime.getYear(), builder.startDateTime.getMonth(),
          builder.startDateTime.getDayOfMonth(), DEFAULT_START_TIME.getHour(),
          DEFAULT_START_TIME.getMinute());
      this.endDateTime =
        LocalDateTime.of(builder.startDateTime.getYear(), builder.startDateTime.getMonth(),
          builder.startDateTime.getDayOfMonth(), DEFAULT_END_TIME.getHour(),
          DEFAULT_END_TIME.getMinute());
    } else {
      this.startDateTime = builder.startDateTime;
      this.endDateTime = builder.endDateTime;
    }

    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.eventSeriesId = builder.eventSeriesId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event event = (Event) o;
    return this.subject.equals(event.subject) && startDateTime.equals(event.startDateTime)
      && this.endDateTime.equals(event.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.subject, this.startDateTime, this.endDateTime);
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
  public String getDescription() {
    return this.description;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return this.endDateTime;
  }

  @Override
  public EventLocation getLocation() {
    return this.location;
  }

  @Override
  public EventStatus getStatus() {
    return this.status;
  }

  @Override
  public int getEventSeriesId() {
    return this.eventSeriesId;
  }

  /**
   * Represents a static inner builder class needed to construct an event based on wanted fields.
   */
  public static class EventBuilder {
    private String subject;
    private LocalDateTime startDateTime;
    private String description;
    private LocalDateTime endDateTime;
    private EventLocation location;
    private EventStatus status;
    private int eventSeriesId = -1;

    private EventBuilder() {
    }

    /**
     * Sets the subject for the Event builder.
     *
     * @param subject subject of event.
     * @return builder with set event subject.
     */
    public EventBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start DateTime for the Event builder.
     *
     * @param startDateTime start DateTime of event.
     * @return builder with set event startDateTime.
     */
    public EventBuilder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the description for the Event builder.
     *
     * @param description description of event.
     * @return builder with set event description.
     */
    public EventBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the end DateTime for the Event builder.
     *
     * @param endDateTime description of event.
     * @return builder with set event endDateTime.
     * @throws IllegalArgumentException if the end datetime is before start time or the same.
     */
    public EventBuilder endDateTime(LocalDateTime endDateTime) {
      if (endDateTime.equals(this.startDateTime) || endDateTime.isBefore(this.startDateTime)) {
        throw new IllegalArgumentException("The end datetime cannot be same "
            + "or before the start date");
      }
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the location for the Event builder.
     *
     * @param location description of event.
     * @return builder with set event location.
     */
    public EventBuilder location(EventLocation location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status for the Event builder.
     *
     * @param status status of event.
     * @return builder with set event status.
     */
    public EventBuilder status(EventStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the event SeriesId for the Event builder when event is in series.
     *
     * @param eventSeriesId SeriesId of event.
     * @return builder with set event SeriesId.
     */
    public EventBuilder eventSeriesId(int eventSeriesId) {
      this.eventSeriesId = eventSeriesId;
      return this;
    }

    /**
     * Copies the given event into a new event builder.
     *
     * @param event the event to copy.
     * @return event builder with the same fields as the given event.
     */
    public EventBuilder copyEventFrom(EventObject event) {
      this.subject = event.getSubject();
      this.startDateTime = event.getStartDateTime();
      this.description = event.getDescription();
      this.endDateTime = event.getEndDateTime();
      this.location = event.getLocation();
      this.status = event.getStatus();
      this.eventSeriesId = event.getEventSeriesId();
      return this;
    }

    /**
     * Builds the event.
     *
     * @return new event with built fields.
     */
    public EventObject build() {
      return new Event(this);
    }
  }

  /**
   * Gets a new event builder.
   *
   * @return event builder.
   */
  public static EventBuilder getBuilder() {
    return new EventBuilder();
  }

  private String locationToString() {
    if (this.location == null) {
      return "";
    } else if (this.location.equals(EventLocation.PHYSICAL)) {
      return " in person";
    } else {
      return " online";
    }
  }

  @Override
  public String eventForBulletPoint() {
    String startDate = this.convertDateToString(this.startDateTime);
    String startTime = this.convertTimeToString(this.startDateTime);
    String endDate = this.convertDateToString(this.endDateTime);
    String endTime = this.convertTimeToString(this.endDateTime);
    String subject = this.useDoubleQuotesOnSubject();
    String res = subject + " starting on " + startDate + " at " + startTime + ", ending on "
        + endDate + " at " + endTime;
    res += this.locationToString();
    return res;
  }

  private String convertTimeToString(LocalDateTime dt) {
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    return dt.format(timeFormat);
  }

  private String convertDateToString(LocalDateTime dt) {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
    return dt.format(dateFormat);
  }

  private boolean isEventAllDay() {
    return this.startDateTime.toLocalDate().equals(this.endDateTime.toLocalDate())
      && this.startDateTime.toLocalTime().equals(DEFAULT_START_TIME)
      && this.endDateTime.toLocalTime().equals(DEFAULT_END_TIME);
  }

  private String useDoubleQuotesOnSubject() {
    return this.subject.split("\\s+").length > 1 ? "\"" + this.subject + "\"" : this.subject;
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    String subject = this.useDoubleQuotesOnSubject();
    res.append(subject).append(",");
    res.append(this.convertDateToString(this.startDateTime)).append(",");
    res.append(this.convertTimeToString(this.startDateTime)).append(",");
    res.append(this.convertDateToString(this.endDateTime)).append(",");
    res.append(this.convertTimeToString(this.endDateTime)).append(",");
    if (this.isEventAllDay()) {
      res.append("True,");
    } else {
      res.append("False,");
    }
    if (this.description != null) {
      res.append(this.description).append(",");
    } else {
      res.append(",");
    }
    res.append(this.locationToString().trim()).append(",");
    if (this.status == EventStatus.PRIVATE) {
      res.append("True");
    } else {
      res.append("False");
    }
    return res.toString();
  }
}


