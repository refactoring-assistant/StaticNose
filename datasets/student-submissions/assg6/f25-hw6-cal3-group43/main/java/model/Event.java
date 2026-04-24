package model;

import static java.time.ZoneOffset.UTC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import messaging.Messages;

/**
 * This class represents an Event. An event requires a subject and a start date and time.
 * An end date and time, description, location (physical/online), status (public/private)
 * are optional aspects of an event. However, if an end date and time are not provided, an
 * event becomes and All Day event that lasts from 8AM to 5PM.
 *
 */
public class Event {
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String description;
  private String subject;
  private String location;
  private String status;
  private boolean isAllDay;
  private EventSeries eventSeries;

  /**
   * If there is no end date and time provided, then sets the event to be an all day event.
   * An all day event entails a start time of 8AM and an end time of 5PM for a given day.
   *
   * @param date    the event start date
   * @param subject the event subject
   */
  public Event(LocalDate date, String subject) {
    this.subject = subject;
    isAllDay = true;
    startTime = LocalDateTime.of(date.getYear(), date.getMonth(),
        date.getDayOfMonth(), 8, 0, 0);
    endTime = LocalDateTime.of(date.getYear(), date.getMonth(),
        date.getDayOfMonth(), 17, 0, 0);
    eventSeries = null;
    description = null;
    location = null;
    status = null;
  }

  /**
   * Creates and Event object and initializes the start date and time, end date and time, and
   * subject to it.
   *
   * @param startTime the start date and time of the event
   * @param endTime   the end date and time of the event
   * @param subject   the subject of the event
   */
  public Event(LocalDateTime startTime, LocalDateTime endTime, String subject) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.subject = subject;
    isAllDay = false;
    eventSeries = null;
    description = null;
    location = null;
    status = null;
  }

  /**
   * Returns an event series.
   *
   * @return event series
   */
  public EventSeries getEventSeries() {
    return eventSeries;
  }

  /**
   * Sets an event series.
   *
   * @param eventSeries an event series
   */
  protected void setEventSeries(EventSeries eventSeries) {
    this.eventSeries = eventSeries;
  }

  /**
   * Returns if the event is an all day event or not.
   *
   * @return a boolean if it is all day or not
   */
  public boolean getIsAllDay() {
    return isAllDay;
  }

  /**
   * If an event is an all day event, changes the start and end date and times appropriately so that
   * it is the same day with a start time of 8AM and an end time of 5PM.
   *
   * @param isAllDay a boolean if the event is all day or not
   */
  protected void setIsAllDay(boolean isAllDay) {
    this.isAllDay = isAllDay;
    if (isAllDay) {
      startTime = LocalDateTime.of(startTime.getYear(), startTime.getMonth(),
          startTime.getDayOfMonth(), 8, 0, 0);
      endTime = LocalDateTime.of(startTime.getYear(), startTime.getMonth(),
          startTime.getDayOfMonth(), 17, 0, 0);
    }
  }

  /**
   * Returns the start date and time of the event.
   *
   * @return start date and time of an event
   */
  public LocalDateTime getStartTime() {
    return startTime;
  }

  /**
   * Set the start date and time of the event.
   *
   * @param startTime start date and time
   */
  protected void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return end date and time of an event
   */
  public LocalDateTime getEndTime() {
    return endTime;
  }

  /**
   * Set the end date and time of the event.
   *
   * @param endTime end date and time
   */
  protected void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  /**
   * Returns the description of the event.
   *
   * @return description of an event
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of the event.
   *
   * @param description the description
   */
  protected void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the subject of the event.
   *
   * @return subject of an event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Set the subject of the event.
   *
   * @param subject the subject
   */
  protected void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the location of the event.
   *
   * @return location of an event
   */
  public String getLocation() {
    return location;
  }

  /**
   * Set the location of the event, which can be either physical or online.
   *
   * @param location the location
   */
  protected void setLocation(String location) {
    if (location == null || location.trim().isEmpty() || location.equalsIgnoreCase("none")) {
      this.location = null;
      return;
    }
    if (!location.equalsIgnoreCase("physical") && !location.equalsIgnoreCase("online")) {
      Messages.error("Location must be physical or online");
      return;
    }
    this.location = location.toLowerCase();
  }

  /**
   * Returns the status of the event.
   *
   * @return statue of an event
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set the status of the event, which can be public or private.
   *
   * @param status the status
   */
  protected void setStatus(String status) {
    if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("none")) {
      this.status = null;
      return;
    }
    if (!status.equalsIgnoreCase("public") && !status.equalsIgnoreCase("private")) {
      Messages.error("Status must be public or private");
      return;
    }
    this.status = status.toLowerCase();
  }

  /**
   * A helper function that determines if an event falls on a date.
   *
   * @param givenDate a date that we want to find the events for
   * @return a boolean if it is on that date or not
   */
  public boolean onDate(LocalDate givenDate) {
    LocalDate startDate = this.getStartTime().toLocalDate();
    LocalDate endDate = this.getEndTime().toLocalDate();

    return givenDate.isEqual(startDate) || givenDate.isEqual(endDate)
        || (givenDate.isAfter(startDate) && givenDate.isBefore(endDate));
  }

  /**
   * A helper function that determines if an event falls in a given span of time.
   *
   * @param startSpan the start date and time for the timeframe
   * @param endSpan   the end date and time for the timeframe
   */
  public boolean inSpan(LocalDateTime startSpan, LocalDateTime endSpan) {
    LocalDateTime eventStart = this.getStartTime();
    LocalDateTime eventEnd = this.getEndTime();

    return (eventStart.isBefore(endSpan) || eventStart.isEqual(endSpan))
        && (eventEnd.isAfter(startSpan) || eventEnd.isEqual(startSpan));

  }

  /**
   * Update a single event's start date and time if valid. Returns true if updated;
   * false if validation failed (and prints a reason).
   *
   * @param newStart the new start date and time
   * @return a boolean representing if the event start date and time was updated or not
   */
  protected boolean updateStartTime(LocalDateTime newStart) {
    if (this.getIsAllDay()) {
      this.setIsAllDay(false);
    }
    LocalDateTime end = this.getEndTime();
    if (newStart.isBefore(end)) {
      this.setStartTime(newStart);
      return true;
    } else {
      Messages.error("Start time must be before end time " + end);
      return false;
    }
  }


  /**
   * Update a single event's end date and time if valid. Returns true if updated;
   * false if validation failed (and prints a reason).
   *
   * @param newEnd the new end date and time
   * @return a boolean representing if the event end date and time was updated or not
   */
  protected boolean updateEndTime(LocalDateTime newEnd) {
    if (this.getIsAllDay()) {
      Messages.error("Skipped all-day event on " + this.getStartTime().toLocalDate());
      return false;
    }
    LocalDateTime start = this.getStartTime();
    if (newEnd.isAfter(start)) {
      this.setEndTime(newEnd);
      return true;
    } else {
      Messages.error("End time must be after start time " + start);
      return false;
    }
  }

  /**
   * A helper function that prints out events in the correct format.
   */
  public void printEvent() {
    String bulletEvent = "*" + this.getSubject() + " starting on "
        + this.getStartTime().toLocalDate() + " at "
        + this.getStartTime().toLocalTime() + ", ending on "
        + this.getEndTime().toLocalDate() + " at "
        + this.getEndTime().toLocalTime();

    if (this.getLocation() != null) {
      String location = this.getLocation();
      Messages.info(bulletEvent + " located at " + location);
    } else {
      Messages.info(bulletEvent);
    }
  }


  /**
   * A method that converts an event into the format needed for a Google Calendar CSV.
   */
  public String convertCsv() {
    DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("h:mm a");

    boolean csvAllDay = this.getIsAllDay();

    // required components of an event
    String csvSubject = this.getSubject();
    String csvStartDate = this.getStartTime().toLocalDate().format(formatDate);
    String csvStartTime = this.getStartTime().toLocalTime().format(formatTime);
    String csvEndDate = this.getEndTime().toLocalDate().format(formatDate);
    String csvEndTime = this.getEndTime().toLocalTime().format(formatTime);

    String csvDescription = "";
    if (this.getDescription() != null) {
      csvDescription = this.getDescription();
    }

    String csvLocation = "";
    if (this.getLocation() != null) {
      csvLocation = this.getLocation();
    }

    // private is determined by a true/false value
    boolean csvStatusPrivate =
        this.getStatus() != null && this.getStatus().equalsIgnoreCase("Private");

    return String.join(",", "\"" + csvSubject + "\"",
        csvStartDate, csvStartTime, csvEndDate, csvEndTime, String.valueOf(csvAllDay),
        "\"" + csvDescription + "\"", "\"" + csvLocation + "\"",
        String.valueOf(csvStatusPrivate)
    );

  }

  /**
   * A private helper function that formats the ical fields appropriately.
   */
  private String icalForm(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")
        .replace("\r", "\\n");
  }

  /**
   * A method that converts an event into the format needed for a Google Calendar ical (ics).
   */
  public String convertIcal() {
    final java.time.format.DateTimeFormatter allDayDate =
        java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
    final java.time.format.DateTimeFormatter utcZoneDate =
        java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    final String subject = icalForm(getSubject());
    final String description = icalForm(getDescription());
    final String location = icalForm(getLocation());
    String uid = subject + "_" + getStartTime() + "_" + getEndTime();
    String dtStampUtc =
        java.time.ZonedDateTime.now(UTC).format(utcZoneDate);

    java.util.List<String> lines = new java.util.ArrayList<>();

    lines.add("BEGIN:VEVENT");
    lines.add("UID:" + uid);
    lines.add("DTSTAMP:" + dtStampUtc);
    lines.add("SUMMARY:" + subject);

    if (!description.isEmpty()) {
      lines.add("DESCRIPTION:" + description);
    }
    if (!location.isEmpty()) {
      lines.add("LOCATION:" + location);
    }

    if (getIsAllDay()) {
      java.time.LocalDate startAllDay = getStartTime().toLocalDate();
      java.time.LocalDate endAllDay = getEndTime().toLocalDate().plusDays(1);
      lines.add("DTSTART;VALUE=DATE:" + startAllDay.format(allDayDate));
      lines.add("DTEND;VALUE=DATE:" + endAllDay.format(allDayDate));
    } else {
      String startUtc = getStartTime().atOffset(java.time.ZoneOffset.UTC).format(utcZoneDate);
      String endUtc = getEndTime().atOffset(java.time.ZoneOffset.UTC).format(utcZoneDate);
      lines.add("DTSTART:" + startUtc);
      lines.add("DTEND:" + endUtc);
    }

    String status = "Public";
    if (getStatus() != null && getStatus().equalsIgnoreCase("private")) {
      status = "Private";
    }
    lines.add("CLASS:" + status);
    lines.add("END:VEVENT");

    return String.join("\r\n", lines) + "\r\n";
  }

  /**
   * Creates a new Event with the same metadata as this one, but with the provided times.
   *
   * @param newStart new start time
   * @param newEnd   new end time
   * @return duplicated event
   */
  public Event duplicateWithTimes(LocalDateTime newStart, LocalDateTime newEnd) {
    Event clone = this.getIsAllDay()
        ? new Event(newStart.toLocalDate(), this.getSubject())
        : new Event(newStart, newEnd, this.getSubject());
    clone.setDescription(this.getDescription());
    clone.setLocation(this.getLocation());
    clone.setStatus(this.getStatus());
    if (this.getIsAllDay()) {
      clone.setIsAllDay(true);
    }
    return clone;
  }

  @Override
  public String toString() {
    if (getIsAllDay()) {
      return subject + " (All day)";
    }
    DateTimeFormatter eventListDisplay = DateTimeFormatter.ofPattern("hh:mm a");
    return subject + " starts at: " + startTime.format(eventListDisplay);
  }


}
