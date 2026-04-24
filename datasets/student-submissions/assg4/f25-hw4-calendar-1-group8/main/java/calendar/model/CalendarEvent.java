package calendar.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Class that represent a single and unique instance of an event on calendar.
 */
public class CalendarEvent {
  private static final Duration ALL_DAY_DURATION = Duration.ofHours(9);
  private static final DateTimeFormatter CSV_DATE_FORMAT =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  private final String id;
  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
  private final String description;
  private final EventLocation location;
  private final EventStatus status;

  private String seriesMasterId;

  /**
   * Private constructor of the CalendarEvent class which uses the builder to set properties of the
   * event.
   *
   * @param builder CalendarEventBuilder object that builds object of CalendarEvent
   */
  private CalendarEvent(CalendarEventBuilder builder) {
    this.id = builder.id;
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesMasterId = builder.seriesMasterId;
  }

  /**
   * Static method to calculate the end time for an all day event (8am - 5pm).
   *
   * @param start ZonedDateTime object representing the start of event (8am for All Day Event)
   * @return ZonedDateTime object representing the end of the event (5pm for All Day Event)
   */
  private static ZonedDateTime calculateAllDayEnd(ZonedDateTime start) {
    return start.plus(ALL_DAY_DURATION);
  }

  /**
   * Private helper method to format the string in accordance with the CSV format. This is useful
   * to format the event as a comma separated string.
   *
   * @param field The string to escape.
   * @return A CSV-safe string.
   */
  private static String escapeCsvField(String field) {
    if (field == null || field.isEmpty()) {
      return "";
    }
    String processed = field;
    if (processed.contains("\"") || processed.contains(",") || processed.contains("\n")) {
      processed = processed.replace("\"", "\"\"");
      processed = "\"" + processed + "\"";
    }
    return processed;
  }

  /**
   * Getter method to get the unique internal identifier of the event.
   *
   * @return String object that represents the UUID of the event
   */
  public String getId() {
    return id;
  }

  /**
   * Getter method to get the subject of the event.
   *
   * @return String object that represents the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Getter method to get the start date and time of the event.
   *
   * @return ZonedDateTime object representing the start time of the event
   */
  public ZonedDateTime getStart() {
    return start;
  }

  /**
   * Getter method to get the end date and time of the event.
   *
   * @return ZonedDateTime object representing the end time of the event
   */
  public ZonedDateTime getEnd() {
    return end;
  }

  /**
   * Getter method to get the description of the event.
   *
   * @return String object containing the description of the event
   */
  public String getDescription() {
    return description;
  }

  /**
   * Getter method to get location of the event.
   *
   * @return String object representing location of the event (PHYSICAL or ONLINE)
   */
  public EventLocation getLocation() {
    return location;
  }

  /**
   * Getter method to get the status of the event.
   *
   * @return Enum object representing the status of the event (PRIVATE or PUBLIC)
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Getter method to get the identifier of the series this event belongs to.
   *
   * @return String UUID, or null if it's not part of a series.
   */
  public String getSeriesMasterId() {
    return seriesMasterId;
  }

  /**
   * Boolean method that checks if the event is recurring or not.
   *
   * @return true is the event is recurring event, otherwise false
   */
  public boolean isRecurringEvent() {
    return this.seriesMasterId != null;
  }

  /**
   * Setter method to set or unset the seriesId of the event to signify the event being part of
   * the series or not.
   *
   * @param seriesMasterId String object representing the seriesId or null.
   */
  public void setSeriesMasterId(String seriesMasterId) {
    this.seriesMasterId = seriesMasterId;
  }

  /**
   * Method to format and store the event's data into a single row for a comma-separated value
   * format.
   *
   * @return A comma-separated string compatible required format.
   */
  public String toCsvRow() {
    String startDate = start.format(CSV_DATE_FORMAT);
    String startTime = start.format(CSV_TIME_FORMAT);
    String endDate = end.format(CSV_DATE_FORMAT);
    String endTime = end.format(CSV_TIME_FORMAT);

    String isPrivate = (status == EventStatus.PRIVATE) ? "True" : "False";

    String locationStr = (location == EventLocation.PHYSICAL) ? "Physical" : "Online";

    return String.join(",",
        escapeCsvField(subject),
        escapeCsvField(startDate),
        escapeCsvField(startTime),
        escapeCsvField(endDate),
        escapeCsvField(endTime),
        escapeCsvField(description),
        escapeCsvField(locationStr),
        escapeCsvField(isPrivate)
    );
  }

  /**
   * Static inner class that represents the builder for the CalendarEvent class.
   */
  public static class CalendarEventBuilder {
    private final String id;
    private final String subject;
    private final ZonedDateTime start;
    private ZonedDateTime end;
    private String description = "";
    private EventLocation location = EventLocation.ONLINE;
    private EventStatus status = EventStatus.PUBLIC;
    private String seriesMasterId = null;

    /**
     * Constructor to construct a builder object when the subject and start date and time of event
     * is passed as arguments.
     *
     * @param subject Subject of the event
     * @param start   Start date time of the event
     * @throws IllegalArgumentException when either of the two parameters is null
     */
    public CalendarEventBuilder(String subject, ZonedDateTime start)
        throws IllegalArgumentException {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }
      if (start == null) {
        throw new IllegalArgumentException("Start date and time is required");
      }

      this.id = UUID.randomUUID().toString();
      this.subject = subject;
      this.start = start.withSecond(0).withNano(0);

      if (start.getHour() == 8 && start.getMinute() == 0) {
        this.end = calculateAllDayEnd(this.start);
      } else {
        this.end = this.start.plusHours(1);
      }
    }

    /**
     * Constructor to construct a builder object when end date and time of the event
     * is passed as arguments.
     *
     * @param end ZonedDateTime object that represents a end date time of the event
     */
    public CalendarEventBuilder withEnd(ZonedDateTime end) {
      if (end != null) {
        this.end = end.withSecond(0).withNano(0);
      }
      return this;
    }

    /**
     * Constructor to construct a builder object when description of the event
     * is passed as argument.
     *
     * @param description String object that contains description of the event
     */
    public CalendarEventBuilder withDescription(String description) {
      this.description = description != null ? description : "";
      return this;
    }

    /**
     * Constructor to construct a builder object when location of the event
     * is passed as argument.
     *
     * @param location String object that represents location of the event
     */
    public CalendarEventBuilder withLocation(EventLocation location) {
      this.location = location != null ? location : EventLocation.ONLINE;
      return this;
    }

    /**
     * Constructor to construct a builder object when status of the event
     * is passed as argument.
     *
     * @param status Enum object that represents status of the event
     */
    public CalendarEventBuilder withStatus(EventStatus status) {
      this.status = status != null ? status : EventStatus.PUBLIC;
      return this;
    }

    /**
     * Constructor to construct a builder object when identifier of the series is passed
     * as an argument.
     *
     * @param seriesMasterId String object identifier of the parent series.
     */
    public CalendarEventBuilder withSeriesMasterId(String seriesMasterId) {
      this.seriesMasterId = seriesMasterId;
      return this;
    }

    /**
     * Method to construct the object of CalendarEvent class using its builder.
     *
     * @return new CalendarEvent object
     * @throws IllegalArgumentException when end date time is less or equal to start date time
     */
    public CalendarEvent build() throws IllegalArgumentException {
      if (end.isBefore(start) || end.isEqual(start)) {
        throw new IllegalArgumentException("End time must be after start time");
      }
      return new CalendarEvent(this);
    }
  }
}