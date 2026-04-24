package calendar.view.dialog;

import java.util.Objects;
import java.util.Set;

/**
 * Represents the result of an event creation/edit dialog.
 * This is a data transfer object (DTO) that encapsulates
 * all the information needed to create or edit an event.
 * Uses only primitives and Strings to maintain MVC separation.
 */
public class EventDialogResult {
  private final String subject;
  private final int startYear;
  private final int startMonth;
  private final int startDay;
  private final int startHour;
  private final int startMinute;
  private final int endYear;
  private final int endMonth;
  private final int endDay;
  private final int endHour;
  private final int endMinute;
  private final String description;
  private final String location;  // "PHYSICAL", "ONLINE", or null
  private final String status;    // "PUBLIC", "PRIVATE", or null
  private final boolean isRecurring;
  private final Set<String> repeatDays;  // "MONDAY", "TUESDAY", etc.
  private final Integer repeatUntilYear;
  private final Integer repeatUntilMonth;
  private final Integer repeatUntilDay;
  private final Integer repeatOccurrences;

  /**
   * Constructs an event dialog result for a single (non-recurring) event.
   *
   * @param subject     the event subject
   * @param startYear   the start year
   * @param startMonth  the start month
   * @param startDay    the start day
   * @param startHour   the start hour
   * @param startMinute the start minute
   * @param endYear     the end year
   * @param endMonth    the end month
   * @param endDay      the end day
   * @param endHour     the end hour
   * @param endMinute   the end minute
   * @param description the description (can be null)
   * @param location    the location string (can be null)
   * @param status      the status string (can be null)
   */
  public EventDialogResult(String subject,
                           int startYear, int startMonth, int startDay,
                           int startHour, int startMinute,
                           int endYear, int endMonth, int endDay,
                           int endHour, int endMinute,
                           String description, String location, String status) {
    this(subject, startYear, startMonth, startDay, startHour, startMinute,
        endYear, endMonth, endDay, endHour, endMinute,
        description, location, status, false, null, null, null, null, null);
  }

  /**
   * Constructs an event dialog result for a recurring event.
   *
   * @param subject           the event subject
   * @param startYear         the start year
   * @param startMonth        the start month
   * @param startDay          the start day
   * @param startHour         the start hour
   * @param startMinute       the start minute
   * @param endYear           the end year
   * @param endMonth          the end month
   * @param endDay            the end day
   * @param endHour           the end hour
   * @param endMinute         the end minute
   * @param description       the description (can be null)
   * @param location          the location string (can be null)
   * @param status            the status string (can be null)
   * @param isRecurring       whether this is a recurring event
   * @param repeatDays        the days to repeat on (for recurring events)
   * @param repeatUntilYear   the end year for recurrence (can be null if using occurrences)
   * @param repeatUntilMonth  the end month for recurrence (can be null if using occurrences)
   * @param repeatUntilDay    the end day for recurrence (can be null if using occurrences)
   * @param repeatOccurrences the number of occurrences (can be null if using until date)
   */
  public EventDialogResult(String subject,
                           int startYear, int startMonth, int startDay,
                           int startHour, int startMinute,
                           int endYear, int endMonth, int endDay,
                           int endHour, int endMinute,
                           String description, String location, String status,
                           boolean isRecurring, Set<String> repeatDays,
                           Integer repeatUntilYear, Integer repeatUntilMonth,
                           Integer repeatUntilDay, Integer repeatOccurrences) {
    this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
    this.startYear = startYear;
    this.startMonth = startMonth;
    this.startDay = startDay;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.endYear = endYear;
    this.endMonth = endMonth;
    this.endDay = endDay;
    this.endHour = endHour;
    this.endMinute = endMinute;
    this.description = description;
    this.location = location;
    this.status = status;
    this.isRecurring = isRecurring;
    this.repeatDays = repeatDays;
    this.repeatUntilYear = repeatUntilYear;
    this.repeatUntilMonth = repeatUntilMonth;
    this.repeatUntilDay = repeatUntilDay;
    this.repeatOccurrences = repeatOccurrences;
  }

  public String getSubject() {
    return subject;
  }

  public int getStartYear() {
    return startYear;
  }

  public int getStartMonth() {
    return startMonth;
  }

  public int getStartDay() {
    return startDay;
  }

  public int getStartHour() {
    return startHour;
  }

  public int getStartMinute() {
    return startMinute;
  }

  public int getEndYear() {
    return endYear;
  }

  public int getEndMonth() {
    return endMonth;
  }

  public int getEndDay() {
    return endDay;
  }

  public int getEndHour() {
    return endHour;
  }

  public int getEndMinute() {
    return endMinute;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public Set<String> getRepeatDays() {
    return repeatDays;
  }

  public Integer getRepeatUntilYear() {
    return repeatUntilYear;
  }

  public Integer getRepeatUntilMonth() {
    return repeatUntilMonth;
  }

  public Integer getRepeatUntilDay() {
    return repeatUntilDay;
  }

  public Integer getRepeatOccurrences() {
    return repeatOccurrences;
  }
}

