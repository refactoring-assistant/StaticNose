package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents an All-day event from 8AM to 5PM.
 */
public class AllDayEvent extends Event {

  private final LocalDate date;


  /**
   * Represents an All-day event.
   *
   * @param eventName the event name
   * @param notes notes
   * @param location location
   * @param status status whether private or public
   * @param seriesId the seriesID
   * @param date the date
   */
  public AllDayEvent(String eventName,  String notes,
                    String location, String status, String seriesId, LocalDate date) {
    super(eventName, notes, location, status, seriesId);

    this.date = date;
  }

  @Override
  public LocalDateTime getStartTime() {
    return LocalDateTime.of(date, LocalTime.of(8, 0));
  }

  @Override
  public LocalDateTime getEndTime() {
    return LocalDateTime.of(date, LocalTime.of(17, 0));
  }


  @Override
  public boolean isAllDay() {
    return true;
  }

  @Override
  public boolean occursOn(LocalDate date) {
    return this.date.isEqual(date);
  }

  @Override
  public Event copy(LocalDateTime newStartTime, LocalDateTime newEndTime, String newSeriesId) {
    return new AllDayEvent(
        this.getEventName(),
        this.getNotes(),
        this.getLocation(),
        this.getStatus(),
        newSeriesId,
        newStartTime.toLocalDate()
    );
  }

  @Override
  public boolean occursInInterval(LocalDateTime start, LocalDateTime end) {
    LocalDateTime eventStart = this.getStartTime();
    LocalDateTime eventEnd = this.getActualEndTime();

    return eventStart.isBefore(end) && eventEnd.isAfter(start);
  }

}
