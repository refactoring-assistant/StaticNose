package calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class for creating Event Series.
 */
public class EventSeries {

  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String weekdays;
  private Integer occurrences;
  private LocalDate untilDate;
  private boolean allDay;

  /**
   * Creates an event series with occurrences.
   *
   * @param subject the subject of the event series.
   * @param startDateTime the start date and time.
   * @param endDateTime the end date and time.
   * @param weekdays the weekdays for repetition.
   * @param occurrences the number of occurrences.
   */
  public EventSeries(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String weekdays, int occurrences) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.weekdays = weekdays;
    this.occurrences = occurrences;
    this.allDay = false;
  }

  /**
   * Creates an event series with an end date.
   *
   * @param subject the subject of the event series.
   * @param startDateTime the start date and time.
   * @param endDateTime the end date and time.
   * @param weekdays the weekdays for repetition.
   * @param untilDate the end date for the series.
   */
  public EventSeries(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String weekdays, LocalDate untilDate) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.weekdays = weekdays;
    this.untilDate = untilDate;
    this.allDay = false;
  }

  /**
   * Creates an all-day event series with occurrences.
   *
   * @param subject the subject of the event series.
   * @param date the date for the series.
   * @param weekdays the weekdays for repetition.
   * @param occurrences the number of occurrences.
   */
  public EventSeries(String subject, LocalDate date, String weekdays, int occurrences) {
    this.subject = subject;
    this.startDateTime = date.atTime(8, 0);
    this.endDateTime = date.atTime(17, 0);
    this.weekdays = weekdays;
    this.occurrences = occurrences;
    this.allDay = true;
  }

  /**
   * Creates an all-day event series with an end date.
   *
   * @param subject the subject of the event series.
   * @param date the date for the series.
   * @param weekdays the weekdays for repetition.
   * @param untilDate the end date for the series.
   */
  public EventSeries(String subject, LocalDate date, String weekdays, LocalDate untilDate) {
    this.subject = subject;
    this.startDateTime = date.atStartOfDay();
    this.endDateTime = date.atTime(23, 59);
    this.weekdays = weekdays;
    this.untilDate = untilDate;
    this.allDay = true;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  public void setWeekdays(String weekdays) {
    this.weekdays = weekdays;
  }

  public void setAllDay(boolean allDay) {
    this.allDay = allDay;
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


  public String getWeekdays() {
    return weekdays;
  }

  public boolean isAllDay() {
    return allDay;
  }
}