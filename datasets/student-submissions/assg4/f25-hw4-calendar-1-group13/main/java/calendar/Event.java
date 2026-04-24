package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single calendar event.
 * Can be standalone or part of an event series.
 */
public class Event implements Comparable<Event> {

  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private Boolean location;
  private boolean isPublic;
  private String seriesId;

  /**
   * Constructor for event which is not an all day event.
   *
   * @param subject       the subject of the event.
   * @param startDateTime the start date and time of the event.
   * @param endDateTime   the end date and time of the event.
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Date or time cannot be null");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start must be before end");
    }
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isPublic = true;
    this.seriesId = null;
  }

  /**
   * Constructor for an all day event.
   *
   * @param subject the subject for the event.
   * @param date    the date of the event.
   */
  public Event(String subject, LocalDate date) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    this.subject = subject;
    this.startDateTime = date.atTime(8, 0);
    this.endDateTime = date.atTime(17, 0);
    this.isPublic = true;
    this.seriesId = null;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public LocalDate getStartDate() {
    return startDateTime.toLocalDate();
  }

  public LocalTime getStartTime() {
    return startDateTime.toLocalTime();
  }

  public LocalDate getEndDate() {
    return endDateTime.toLocalDate();
  }

  public LocalTime getEndTime() {
    return endDateTime.toLocalTime();
  }

  public String getDescription() {
    return description;
  }

  /**
   * The getter for the location.
   *
   * @return the location.
   */
  public String getLocation() {
    if (location == null) {
      return null;
    }
    return location ? "Physical" : "Online";
  }

  public Boolean isPhysicalLocation() {
    return location;
  }

  public boolean isOnlineLocation() {
    return location != null && !location;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public String getSeriesId() {
    return seriesId;
  }

  public boolean isPartOfSeries() {
    return seriesId != null;
  }

  /**
   * Setter method to set Subject.
   *
   * @param subject teh subject.
   */
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    this.subject = subject;
  }

  /**
   * The setter for startdate and time sets the startdatetim.
   *
   * @param startDateTime the correct starttimedate.
   */
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (endDateTime != null && startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start date/time must be before end date/time");
    }
    this.startDateTime = startDateTime;
  }

  /**
   * The setter for enddate and time sets the enddatetim.
   *
   * @param endDateTime the correct starttimedate.
   */
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (startDateTime != null && endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("End date/time must be after start date/time");
    }
    this.endDateTime = endDateTime;
  }

  /**
   * The setter for description.
   *
   * @param description the correct description..
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(Boolean isPhysical) {
    this.location = isPhysical;
  }

  /**
   * Sets the physical Location.
   */
  public void setLocationPhysical() {
    this.location = true;
  }

  /**
   * Sets the location online.
   */
  public void setLocationOnline() {
    this.location = false;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Checks if event is an all day event.
   */
  public boolean isAllDayEvent() {
    return startDateTime.toLocalTime().equals(LocalTime.of(8, 0))
        && endDateTime.toLocalTime().equals(LocalTime.of(17, 0))
        && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Validates that event spans only one day.
   */
  public boolean isSingleDay() {
    return startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Checks if this event conflicts with another.
   */
  public boolean conflictsWith(Event other) {
    return this.subject.equals(other.subject)
        && this.startDateTime.equals(other.startDateTime)
        && this.endDateTime.equals(other.endDateTime);
  }

  /**
   * Checks if this event overlaps with a specific date/time.
   */
  public boolean occursAt(LocalDateTime dateTime) {
    return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
  }

  /**
   * Checks if this event falls within a date range.
   */
  public boolean occursInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return !startDateTime.isAfter(rangeEnd) && !endDateTime.isBefore(rangeStart);
  }

  /**
   * Checks if this event occurs on a specific date.
   */
  public boolean occursOnDate(LocalDate date) {
    return !startDateTime.toLocalDate().isAfter(date)
        && !endDateTime.toLocalDate().isBefore(date);
  }

  /**
   * Checks if this event matches search criteria.
   */
  public boolean matches(String subject, LocalDateTime startDateTime) {
    boolean subjectMatches = (subject == null || this.subject.equals(subject));
    boolean startMatches = (startDateTime == null || this.startDateTime.equals(startDateTime));
    return subjectMatches && startMatches;
  }

  /**
   * Updates a specific property by name.
   * Used by edit commands to modify event properties.
   *
   * @param property the name of the property to update (subject,
   *                 start, end, description, location, status)
   * @param value    the new value as a string
   * @throws IllegalArgumentException if property or value is invalid
   */
  public void updateProperty(String property, String value) {
    if (property == null || value == null) {
      throw new IllegalArgumentException("Property and value cannot be null");
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    switch (property.toLowerCase()) {
      case "subject":
        setSubject(value);
        break;

      case "start":
        setStartDateTime(LocalDateTime.parse(value, dateTimeFormatter));
        break;

      case "end":
        setEndDateTime(LocalDateTime.parse(value, dateTimeFormatter));
        break;

      case "description":
        setDescription(value);
        break;

      case "location":
        if (value.equalsIgnoreCase("physical")
            || value.equalsIgnoreCase("true")) {
          setLocationPhysical();
        } else if (value.equalsIgnoreCase("online")
            || value.equalsIgnoreCase("false")) {
          setLocationOnline();
        } else {
          throw new IllegalArgumentException("Location must be 'physical' or 'online'");
        }
        break;

      case "status":
        if (value.equalsIgnoreCase("public")) {
          setPublic(true);
        } else if (value.equalsIgnoreCase("private")) {
          setPublic(false);
        } else {
          throw new IllegalArgumentException("Status must be 'public' or 'private'");
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Creates a deep copy of this event.
   */
  public Event copy() {
    Event copy = new Event(this.subject, this.startDateTime, this.endDateTime);
    copy.setDescription(this.description);
    copy.setLocation(this.location);
    copy.setPublic(this.isPublic);
    copy.setSeriesId(this.seriesId);
    return copy;
  }

  /**
   * Returns start date in Google Calendar CSV format (MM/dd/yyyy).
   */
  public String getStartDateForCsv() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    return startDateTime.toLocalDate().format(formatter);
  }

  /**
   * Returns end date in Google Calendar CSV format (MM/dd/yyyy).
   */
  public String getEndDateForCsv() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    return endDateTime.toLocalDate().format(formatter);
  }

  /**
   * Returns start time in Google Calendar CSV format (hh:mm a).
   */
  public String getStartTimeForCsv() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
    return startDateTime.toLocalTime().format(formatter);
  }

  /**
   * Returns end time in Google Calendar CSV format (hh:mm a).
   */
  public String getEndTimeForCsv() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
    return endDateTime.toLocalTime().format(formatter);
  }

  /**
   * Returns "True" or "False" for CSV private field.
   */
  public String getPrivateForCsv() {
    return isPublic ? "False" : "True";
  }

  /**
   * Returns single-line format for range queries.
   */
  @Override
  public String toString() {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    StringBuilder sb = new StringBuilder();
    sb.append(subject).append(" starting on ");
    sb.append(getStartDate().format(dateFormatter)).append(" at ");
    sb.append(getStartTime().format(timeFormatter)).append(", ending on ");
    sb.append(getEndDate().format(dateFormatter)).append(" at ");
    sb.append(getEndTime().format(timeFormatter));

    if (location != null) {
      sb.append(" at ").append(getLocation());
    }

    return sb.toString();
  }

  /**
   * Returns bullet format for daily event listings.
   */
  public String toBulletFormat() {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    StringBuilder sb = new StringBuilder();
    sb.append(this).append("\n");
    if (location != null) {
      sb.append(" at ").append(getLocation());
    }
    return sb.toString();
  }


  /**
   * Compares events chronologically for sorting.
   */
  @Override
  public int compareTo(Event other) {
    int dateComparison = this.startDateTime.compareTo(other.startDateTime);
    if (dateComparison != 0) {
      return dateComparison;
    }
    return this.subject.compareTo(other.subject);
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Event event = (Event) obj;
    return subject.equals(event.subject)
        && startDateTime.equals(event.startDateTime)
        && endDateTime.equals(event.endDateTime);
  }

  @Override
  public int hashCode() {
    int result = subject.hashCode();
    result = 31 * result + startDateTime.hashCode();
    result = 31 * result + endDateTime.hashCode();
    return result;
  }
}