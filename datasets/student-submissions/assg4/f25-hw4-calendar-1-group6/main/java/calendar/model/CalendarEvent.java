package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single calendar event instance.
 * For series instances, seriesId is present. For single events, it's empty.
 * All-day event: represented as start and end on the same date.
 * Checks: end >= start, subject non-empty, if part of series - must be single-day.
 */
public final class CalendarEvent {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final Optional<String> description;
  private final Optional<String> location;
  private final Optional<String> status;
  private final Optional<SeriesId> seriesId;

  private CalendarEvent(String subject,
                        LocalDateTime start,
                        LocalDateTime end,
                        Optional<String> description,
                        Optional<String> location,
                        Optional<String> status,
                        Optional<SeriesId> seriesId) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be empty.");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("Event end cannot be before start.");
    }
    if (seriesId.isPresent()) {
      LocalDate s = start.toLocalDate();
      LocalDate e = end.toLocalDate();
      if (!s.equals(e)) {
        throw new IllegalArgumentException("Series events must start and end on the same day.");
      }
    }
    this.subject = subject.trim();
    this.start = start;
    this.end = end;
    this.description = description.map(String::trim);
    this.location = location.map(String::trim);
    this.status = status.map(String::trim);
    this.seriesId = seriesId;
  }

  /**
   * To get a Builder for making a new CalendarEvent.
   *
   * @return new builder object
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Get the subject of the calendar event.
   *
   * @return subject, a string.
   */
  public String subject() {
    return subject;
  }

  /**
   * Get starting date and time of calendar event.
   *
   * @return start, formatted string having start date and time
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * Get end date and time of calendar event.
   *
   * @return start, formatted string having end date and time
   */
  public LocalDateTime end() {
    return end;
  }

  /**
   * Get calendar event description, if it is added.
   *
   * @return description, a string
   */
  public Optional<String> description() {
    return description;
  }

  /**
   * Get location of calendar event, if it is provided.
   *
   * @return description, a string
   */
  public Optional<String> location() {
    return location;
  }

  /**
   * Get status of calendar event, if it is provided.
   *
   * @return description, a string
   */
  public Optional<String> status() {
    return status;
  }

  /**
   * Get the id of event if it is scheduled in series.
   * this is optional field, may or may not be setup
   *
   * @return seriesId indicating id of series
   */
  public Optional<SeriesId> seriesId() {
    return seriesId;
  }

  /**
   * Returns a new CalendarEvent with the specified subject.
   *
   * @param newSubject the new subject for the event
   * @return a new CalendarEvent instance with the updated subject
   */
  public CalendarEvent withSubject(String newSubject) {
    return new CalendarEvent(newSubject, start, end, description, location, status, seriesId);
  }

  /**
   * Returns a new CalendarEvent with the specified start time.
   *
   * @param newStart the new start date and time for the event
   * @return a new CalendarEvent instance with the updated start time
   */
  public CalendarEvent withStart(LocalDateTime newStart) {
    return new CalendarEvent(subject, newStart, end, description, location, status, seriesId);
  }

  /**
   * Returns a new CalendarEvent with the specified end time.
   *
   * @param newEnd the new end date and time for the event
   * @return a new CalendarEvent instance with the updated end time
   */
  public CalendarEvent withEnd(LocalDateTime newEnd) {
    return new CalendarEvent(subject, start, newEnd, description, location, status, seriesId);
  }

  /**
   * Returns a new CalendarEvent with the specified description.
   *
   * @param newDesc the new description for the event; may be null
   * @return a new CalendarEvent instance with the updated description
   */
  public CalendarEvent withDescription(String newDesc) {
    return new CalendarEvent(subject, start, end, Optional.ofNullable(newDesc), location, status,
        seriesId);
  }


  /**
   * Returns a new CalendarEvent with the specified location.
   *
   * @param newLoc the new location for the event; may be null
   * @return a new CalendarEvent instance with the updated location
   */
  public CalendarEvent withLocation(String newLoc) {
    return new CalendarEvent(subject, start, end, description, Optional.ofNullable(newLoc), status,
        seriesId);
  }

  /**
   * Returns a new CalendarEvent with the specified status.
   *
   * @param newStatus the new status for the event; may be null
   * @return a new CalendarEvent instance with the updated status
   */
  public CalendarEvent withStatus(String newStatus) {
    return new CalendarEvent(subject, start, end, description, location,
        Optional.ofNullable(newStatus), seriesId);
  }

  /**
   * Returns a new CalendarEvent with the specified series ID.
   *
   * @param sid the new series ID; may be null
   * @return a new CalendarEvent instance with the updated series ID
   */
  public CalendarEvent withSeriesId(SeriesId sid) {
    return new CalendarEvent(subject, start, end, description, location, status,
        Optional.ofNullable(sid));
  }

  /**
   * Check whether an event is held all day long or not.
   *
   * @return True if it is all day event, False otherwise
   */
  public boolean isAllDay() {
    return start.toLocalTime().getHour() == 8
        && start.toLocalTime().getMinute() == 0
        && end.toLocalTime().getHour() == 17
        && end.toLocalTime().getMinute() == 0
        && start.toLocalDate().equals(end.toLocalDate());
  }

  /**
   * Returns the date part of the event's start time.
   *
   * @return the LocalDate representing the day of the event's start time
   */
  public LocalDate day() {
    return start.toLocalDate();
  }

  @Override
  public String toString() {
    return "CalendarEvent{" + subject + " " + start + "→" + end
        + seriesId.map(s -> " sid=" + s).orElse("") + "}";
  }

  /**
   * A builder class for constructing instances of CalendarEvent.
   */
  public static final class Builder {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String description;
    private String location;
    private String status;
    private SeriesId seriesId;

    /**
     * Sets the subject of the event.
     *
     * @param s the subject to set
     * @return this Builder instance for method chaining
     */
    public Builder subject(String s) {
      this.subject = s;
      return this;
    }


    /**
     * Sets the start date and time of the event.
     *
     * @param s the start date and time to set
     * @return this Builder instance for method chaining
     */
    public Builder start(LocalDateTime s) {
      this.start = s;
      return this;
    }

    /**
     * Sets the end date and time of the event.
     *
     * @param e the end date and time to set
     * @return this Builder instance for method chaining
     */
    public Builder end(LocalDateTime e) {
      this.end = e;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param d the description to set; may be null
     * @return this Builder instance for method chaining
     */
    public Builder description(String d) {
      this.description = d;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param l the location to set; may be null
     * @return this Builder instance for method chaining
     */
    public Builder location(String l) {
      this.location = l;
      return this;
    }

    /**
     * Sets the status of the event.
     *
     * @param st the status to set; may be null
     * @return this Builder instance for method chaining
     */
    public Builder status(String st) {
      this.status = st;
      return this;
    }

    /**
     * Sets the series ID associated with the event.
     *
     * @param sid the series ID to set; may be null
     * @return this Builder instance for method chaining
     */
    public Builder seriesId(SeriesId sid) {
      this.seriesId = sid;
      return this;
    }

    /**
     * Builds and returns a new CalendarEvent instance.
     * Required and must be non-null fileds: subject, start, end
     * Other fields are optional and may be null.
     *
     * @return a new CalendarEvent instance with the configured properties
     * @throws NullPointerException if the subject, start, or end fields are not set
     */
    public CalendarEvent build() {
      Objects.requireNonNull(subject, "subject required");
      Objects.requireNonNull(start, "start required");
      Objects.requireNonNull(end, "end required");
      return new CalendarEvent(
          subject,
          start,
          end,
          Optional.ofNullable(description),
          Optional.ofNullable(location),
          Optional.ofNullable(status),
          Optional.ofNullable(seriesId)
      );
    }
  }
}

