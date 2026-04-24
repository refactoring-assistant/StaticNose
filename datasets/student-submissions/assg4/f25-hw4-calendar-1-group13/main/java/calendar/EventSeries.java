package calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a recurring event series that generates multiple Event instances.
 */
public class EventSeries {

  private String seriesId;
  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private Boolean location;
  private boolean isPublic;

  private List<DayOfWeek> repeatDays;
  private Integer repeatCount;
  private LocalDate untilDate;

  /**
   * Constructor for event series with specific date/time.
   * Events in a series must be single-day.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param repeatDaysStr string of day codes
   * @param repeatCount   number of occurrences
   * @param untilDate     last date for occurrences
   */
  public EventSeries(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String repeatDaysStr, Integer repeatCount, LocalDate untilDate) {

    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Date or time cannot be null");
    }
    if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException("Series events must be single-day");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start must be before end");
    }
    if (repeatCount == null && untilDate == null) {
      throw new IllegalArgumentException("Must specify either repeat count or until date");
    }
    if (repeatCount != null && repeatCount <= 0) {
      throw new IllegalArgumentException("Repeat count must be positive");
    }

    this.seriesId = UUID.randomUUID().toString();
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.isPublic = true;
    this.repeatCount = repeatCount;
    this.untilDate = untilDate;
    this.repeatDays = parseDayString(repeatDaysStr);
  }

  /**
   * Constructor for all-day event series.
   *
   * @param subject       the subject of the event
   * @param date          the starting date
   * @param repeatDaysStr string of day codes
   * @param repeatCount   number of occurrences
   * @param untilDate     last date for occurrences
   */
  public EventSeries(String subject, LocalDate date, String repeatDaysStr,
                     Integer repeatCount, LocalDate untilDate) {

    this(subject, date.atTime(8, 0), date.atTime(17, 0),
        repeatDaysStr, repeatCount, untilDate);
  }

  /**
   * Parses day string into list of DayOfWeek.
   * M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday
   */
  private List<DayOfWeek> parseDayString(String days) {

    if (days == null || days.isEmpty()) {
      throw new IllegalArgumentException("Must give at least one repeat day");
    }

    List<DayOfWeek> dayList = new ArrayList<>();
    days = days.toUpperCase();

    for (char c : days.toCharArray()) {
      switch (c) {
        case 'M':
          if (!dayList.contains(DayOfWeek.MONDAY)) {
            dayList.add(DayOfWeek.MONDAY);
          }
          break;
        case 'T':
          if (!dayList.contains(DayOfWeek.TUESDAY)) {
            dayList.add(DayOfWeek.TUESDAY);
          }
          break;
        case 'W':
          if (!dayList.contains(DayOfWeek.WEDNESDAY)) {
            dayList.add(DayOfWeek.WEDNESDAY);
          }
          break;
        case 'R':
          if (!dayList.contains(DayOfWeek.THURSDAY)) {
            dayList.add(DayOfWeek.THURSDAY);
          }
          break;
        case 'F':
          if (!dayList.contains(DayOfWeek.FRIDAY)) {
            dayList.add(DayOfWeek.FRIDAY);
          }
          break;
        case 'S':
          if (!dayList.contains(DayOfWeek.SATURDAY)) {
            dayList.add(DayOfWeek.SATURDAY);
          }
          break;
        case 'U':
          if (!dayList.contains(DayOfWeek.SUNDAY)) {
            dayList.add(DayOfWeek.SUNDAY);
          }
          break;
        default:
          throw new IllegalArgumentException("Invalid day character: " + c);
      }
    }
    return dayList;
  }

  public String getSeriesId() {
    return seriesId;
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

  public String getDescription() {
    return description;
  }

  public Boolean getLocation() {
    return location;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public List<DayOfWeek> getRepeatDays() {
    return new ArrayList<>(repeatDays);
  }

  public Integer getRepeatCount() {
    return repeatCount;
  }

  public LocalDate getUntilDate() {
    return untilDate;
  }

  /**
   * The setter for Subject.
   *
   * @param subject sets the subject value.
   */
  public void setSubject(String subject) {

    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }
    this.subject = subject;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(Boolean location) {
    this.location = location;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Generates all Event instances for this series.
   * Each event is created with the same seriesId, linking them together.
   *
   * @return list of Event objects representing all occurrences in this series
   */
  public List<Event> generateEvents() {

    List<Event> events = new ArrayList<>();
    LocalDate currentDate = startDateTime.toLocalDate();
    int occurrenceCount = 0;

    LocalDate stopDate = (untilDate != null) ? untilDate : LocalDate.MAX;
    int maxOccurrences = (repeatCount != null) ? repeatCount : Integer.MAX_VALUE;

    while (occurrenceCount < maxOccurrences && !currentDate.isAfter(stopDate)) {

      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = currentDate.atTime(startDateTime.toLocalTime());
        LocalDateTime eventEnd = currentDate.atTime(endDateTime.toLocalTime());

        Event event = new Event(subject, eventStart, eventEnd);
        event.setDescription(description);
        event.setLocation(location);
        event.setPublic(isPublic);
        event.setSeriesId(seriesId);

        events.add(event);
        occurrenceCount++;
      }
      currentDate = currentDate.plusDays(1);
    }

    return events;
  }

  /**
   * Checks if this series would conflict with a single event.
   * Generates events temporarily to check for conflicts.
   *
   * @param event the event to check against
   * @return true if any occurrence would conflict
   */
  public boolean conflictsWithEvent(Event event) {

    List<Event> tempEvents = generateEvents();
    for (Event generatedEvent : tempEvents) {
      if (generatedEvent.conflictsWith(event)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this series would conflict with another series.
   *
   * @param other the other series to check against
   * @return true if any occurrences would conflict
   */
  public boolean conflictsWithSeries(EventSeries other) {

    List<Event> thisEvents = this.generateEvents();
    List<Event> otherEvents = other.generateEvents();

    for (Event thisEvent : thisEvents) {
      for (Event otherEvent : otherEvents) {
        if (thisEvent.conflictsWith(otherEvent)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    StringBuilder sb = new StringBuilder();
    sb.append("Series: ").append(subject);
    sb.append(" from ").append(startDateTime.toLocalTime().format(timeFormatter));
    sb.append(" to ").append(endDateTime.toLocalTime().format(timeFormatter));
    sb.append(" on ");

    StringBuilder daysStr = new StringBuilder();
    for (DayOfWeek day : repeatDays) {
      switch (day) {
        case MONDAY:
          daysStr.append("M");
          break;
        case TUESDAY:
          daysStr.append("T");
          break;
        case WEDNESDAY:
          daysStr.append("W");
          break;
        case THURSDAY:
          daysStr.append("R");
          break;
        case FRIDAY:
          daysStr.append("F");
          break;
        case SATURDAY:
          daysStr.append("S");
          break;
        case SUNDAY:
          daysStr.append("U");
          break;
        default:
          throw new IllegalArgumentException("Invalid day character: ");
      }
    }
    sb.append(daysStr);

    if (repeatCount != null) {
      sb.append(" for ").append(repeatCount).append(" times");
    } else {
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      sb.append(" until ").append(untilDate.format(dateFormatter));
    }

    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    EventSeries that = (EventSeries) obj;
    return seriesId.equals(that.seriesId);
  }

  @Override
  public int hashCode() {
    return seriesId.hashCode();
  }

  /**
   * Returns all events from this series that occur within the specified date/time range.
   *
   * @param rangeStart the start of the range
   * @param rangeEnd   the end of the range
   * @return list of events from this series that fall within the range
   */
  public List<Event> getEventsInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {

    if (rangeStart == null || rangeEnd == null) {
      throw new IllegalArgumentException("Range cannot be null");
    }
    if (rangeStart.isAfter(rangeEnd)) {
      throw new IllegalArgumentException("Range start must be before or equal to range end");
    }

    List<Event> eventsInRange = new ArrayList<>();
    List<Event> allEvents = generateEvents();

    for (Event event : allEvents) {
      if (event.occursInRange(rangeStart, rangeEnd)) {
        eventsInRange.add(event);
      }
    }

    return eventsInRange;
  }

  /**
   * Prints all events from this series that occur within the specified date/time range.
   * Each event is printed on a separate line in the standard format.
   *
   * @param rangeStart the start of the range
   * @param rangeEnd   the end of the range
   */
  public void printEventsInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {

    List<Event> eventsInRange = getEventsInRange(rangeStart, rangeEnd);

    if (eventsInRange.isEmpty()) {
      System.out.println("No events from series '" + subject + "' in the specified range.");
      return;
    }

    System.out.println("Events from series '" + subject + "' in range:");
    for (Event event : eventsInRange) {
      System.out.println(event.toString());
    }
  }
}