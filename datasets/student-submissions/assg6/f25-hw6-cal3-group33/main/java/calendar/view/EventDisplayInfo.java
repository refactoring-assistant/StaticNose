package calendar.view;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for displaying event information in the view.
 * Represents formatted, view-ready event data passed from controller to view.
 * Immutable to prevent unintended modifications.
 */
public class EventDisplayInfo {

  private final String eventName;
  private final String timeRange;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final LocalDate eventEndDate;
  private final String location;
  private final String description;
  private final String status;
  private final boolean isRecurring;
  private final String recurrenceInfo;
  private final Color calendarColor;

  /**
   * Constructs an EventDisplayInfo with all event display data.
   *
   * @param eventName      name/title of the event
   * @param timeRange      formatted time range (e.g., "10:00 AM - 11:00 AM")
   * @param startDateTime  start date and time of the event
   * @param endDateTime    end date and time of the event
   * @param eventEndDate   end date for multi-day events (can be null if single-day)
   * @param location       location of the event (can be null or empty)
   * @param description    description of the event (can be null or empty)
   * @param status         event status ("PUBLIC" or "PRIVATE")
   * @param isRecurring    whether this is a recurring event
   * @param recurrenceInfo formatted recurrence description (e.g., "Repeats weekly")
   * @param calendarColor  color representing the calendar this event belongs to
   */
  public EventDisplayInfo(String eventName, String timeRange,
                          LocalDateTime startDateTime, LocalDateTime endDateTime,
                          LocalDate eventEndDate,
                          String location, String description, String status,
                          boolean isRecurring, String recurrenceInfo,
                          Color calendarColor) {
    this.eventName = eventName;
    this.timeRange = timeRange;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.eventEndDate = eventEndDate;
    this.location = location;
    this.description = description;
    this.status = status;
    this.isRecurring = isRecurring;
    this.recurrenceInfo = recurrenceInfo;
    this.calendarColor = calendarColor;
  }

  public String getEventName() {
    return eventName;
  }

  public String getTimeRange() {
    return timeRange;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public LocalDate getEventEndDate() {
    return eventEndDate;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public String getRecurrenceInfo() {
    return recurrenceInfo;
  }

  public Color getCalendarColor() {
    return calendarColor;
  }
}