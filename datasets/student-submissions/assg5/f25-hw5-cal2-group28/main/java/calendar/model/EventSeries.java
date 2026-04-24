package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing a recurring event series.
 * This class holds the rules for a series (e.g., "repeats every Monday for 5 weeks")
 * and can generate all the individual events in that series.
 */
public class EventSeries {

  private final String seriesId;
  private Set<WeekDays> repeatDays;
  private int occur;
  private LocalDate endDate;
  private Events templateEvent;

  /**
   * Constructor to initialise the event series.
   *
   * @param seriesId the unique identifier of the event series
   * @param templateEvent the prototype event to be used as a template for all events in the series
   */
  public EventSeries(String seriesId, Events templateEvent) {
    this.seriesId = seriesId;
    this.repeatDays = new HashSet<>();
    this.occur = 0;
    this.templateEvent = templateEvent;
  }

  /**
   * Sets the repeating days of the week from a string (e.g., "MRU").
   * 'M' = Monday, 'T' = Tuesday, 'W' = Wednesday, 'R' = Thursday,
   * 'F' = Friday, 'S' = Saturday, 'U' = Sunday
   *
   * @param repeatDays A string representing the days of the week.
   */
  public void setRepeatDays(Set<WeekDays> repeatDays) {
    this.repeatDays = repeatDays;
  }

  /**
   * Setter function for number of occurrences of the event.
   *
   * @param count the number of occurrences
   */
  public void setOccur(int count) {
    this.occur = count;
  }

  /**
   * Setter function for event's end date.
   *
   * @param endDate the event's end date
   */
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  /**
   * Sets the template for the series based on a prototype event.
   *
   * @param event the event to assign in the series
   * */
  public void setTemplateEvent(Events event) {
    this.templateEvent = event;
  }

  /**
   * Gets the ID of this series.
   *
   * @return The series ID.
   */
  public String getSeriesId() {
    return this.seriesId;
  }

  /**
   * Function to generate series of events.
   * This logic iterates from the first event date, respects the repeat days,
   * and stops after EITHER N occurrences or the end date.
   *
   * @param calendar The calendar object, used to check for event conflicts.
   * @return A list of all generated Events in the series.
   */
  public List<Events> genEvents(Calendar calendar) throws IllegalStateException {
    if (templateEvent.getStartTime() == null || templateEvent.getEndTime() == null) {
      throw new IllegalStateException("Template event missing start or end time");
    }
    if (repeatDays.isEmpty() || (occur == 0 && endDate == null)) {
      throw new IllegalStateException("Recurrence rules are not set");
    }

    try {
      List<Events> finalEvents = new ArrayList<>();
      LocalTime templateStartTime = templateEvent.getStartTime().toLocalTime();
      LocalTime templateEndTime = templateEvent.getEndTime().toLocalTime();
      LocalDate startSeriesDate = templateEvent.getStartTime().toLocalDate();
      LocalDate endSeriesDate = (endDate != null) ? endDate : startSeriesDate.plusYears(
          100
      );

      LocalDate currentDate = startSeriesDate;
      int count = 0;

      while (!currentDate.isAfter(endSeriesDate) && (occur == 0 || count < occur)) {
        WeekDays curDay = convertDayOfWeek(currentDate.getDayOfWeek());
        if (repeatDays.contains(curDay)) {
          LocalDateTime startDateTime = currentDate.atTime(templateStartTime);
          LocalDateTime endDateTime = currentDate.atTime(templateEndTime);
          Events newEvent = new Events(templateEvent, startDateTime, endDateTime);
          newEvent.setIdSeries(this.seriesId);
          newEvent.setInitStart(templateEvent.getStartTime());

          if (!calendar.eventExists(newEvent.getId())) {
            finalEvents.add(newEvent);
            count++;
          }
        }
        currentDate = currentDate.plusDays(1);
      }
      return finalEvents;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate events due to: " + e.getMessage(), e);
    }
  }

  /**
   * Function to get current day from the Enum.
   *
   * @param day the current day as an instance of DayOfWeek
   * @return the day of the week from enum
   * */
  private WeekDays convertDayOfWeek(DayOfWeek day) {
    switch (day) {
      case MONDAY:
        return WeekDays.MONDAY;
      case TUESDAY:
        return WeekDays.TUESDAY;
      case WEDNESDAY:
        return WeekDays.WEDNESDAY;
      case THURSDAY:
        return WeekDays.THURSDAY;
      case FRIDAY:
        return WeekDays.FRIDAY;
      case SATURDAY:
        return WeekDays.SATURDAY;
      default:
        return WeekDays.SUNDAY;
    }
  }
}