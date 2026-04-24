package calendar.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a calendar event with a subject, start and end dates/times, description, location,
 * status, and series ID. Supports a builder pattern for convenient construction.
 */
public final class Event implements InterfaceEvent {
  private String subject;
  private LocalDate startDate;
  private LocalTime startTime;
  private LocalDate endDate;
  private LocalTime endTime;
  private String description;
  private String location;
  private String status;
  private int seriesId;

  /**
   * Private constructor called by the Builder.
   *
   * @param builder the Builder instance used to construct the calendar.Model.Event
   */
  private Event(Builder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.startTime = builder.startTime;
    this.endDate = builder.endDate;
    this.endTime = builder.endTime;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.seriesId = builder.seriesId;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public LocalDate getStartDate() {
    return startDate;
  }

  void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  @Override
  public LocalDate getEndDate() {
    return endDate;
  }

  void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  @Override
  public LocalTime getStartTime() {
    return startTime;
  }

  void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  @Override
  public LocalTime getEndTime() {
    return endTime;
  }

  void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  @Override
  public String getDescription() {
    return description;
  }

  void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  void setLocation(String location) {
    this.location = location;
  }

  @Override
  public String getStatus() {
    return status;
  }

  void setStatus(String status) {
    this.status = status;
  }

  @Override
  public int getSeriesId() {
    return seriesId;
  }

  void setSeriesId(int seriesId) {
    this.seriesId = seriesId;
  }

  Event copy() {
    return new Builder(this.getSubject(), this.getStartDate(), this.getStartTime(),
        this.getEndDate(), this.getEndTime()).description(this.getDescription())
        .location(this.getLocation()).status(this.getStatus()).seriesId(this.getSeriesId()).build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceEvent)) {
      return false;
    }
    InterfaceEvent that = (InterfaceEvent) o;
    return Objects.equals(startDate, that.getStartDate())
        && Objects.equals(endDate, that.getEndDate())
        && Objects.equals(startTime, that.getStartTime())
        && Objects.equals(endTime, that.getEndTime())
        && Objects.equals(subject, that.getSubject());
  }


  @Override
  public int hashCode() {
    return Objects.hash(startDate, endDate, startTime, endTime, subject);
  }

  /**
   * Builder class for constructing calendar.Model.Event instances with required and
   * optional fields.
   */
  static class Builder {
    private final String subject;
    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate endDate;
    private final LocalTime endTime;
    private String description = "null";
    private String location = "null";
    private String status = "null";
    private int seriesId = -1;

    /**
     * Constructs a Builder with required event fields.
     *
     * @param subject   the subject of the event
     * @param startDate the start date of the event
     * @param startTime the start time of the event
     * @param endDate   the end date of the event
     * @param endTime   the end time of the event
     */
    Builder(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
            LocalTime endTime) {
      this.subject = subject;
      this.startDate = startDate;
      this.startTime = startTime;
      this.endDate = endDate;
      this.endTime = endTime;
    }

    /**
     * This is the description setter.
     *
     * @param description setting description
     * @return this
     */
    Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * This is the location setter.
     *
     * @param location setting description
     * @return this
     */
    Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * This is the status setter.
     *
     * @param status setting description
     * @return this
     */
    Builder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * This is the seriesID setter.
     *
     * @param seriesId setting description
     * @return this
     */
    Builder seriesId(int seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds the calendar.Model.Event instance.
     *
     * @return a new calendar.Model.Event object
     */
    Event build() {
      return new Event(this);
    }
  }
}
