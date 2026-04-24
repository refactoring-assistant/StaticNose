package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a recurring series .
 * Holds a base event, repeat rules (days), and either a count or an end date.
 */
public interface EventSeriesInterface {

  /**
   * getId.
   */
  UUID getId();

  /**
   * getBaseEvent.
   */
  Event getBaseEvent();

  /**
   * getRepeatDays.
   */
  Set<DayOfWeek> getRepeatDays();

  /**
   * getOccurrences.
   */
  int getOccurrences();

  /**
   * getEndDate.
   */
  LocalDate getEndDate();

  /**
   * getEvents.
   */
  List<Event> getEvents();

  /**
   * setBaseEvent.
   */
  void setBaseEvent(Event baseEvent);

  /**
   * setRepeatDays.
   */
  void setRepeatDays(Set<DayOfWeek> repeatDays);

  /**
   * setOccurrences.
   */
  void setOccurrences(Integer occurrences);

  /**
   * setEndDate.
   */
  void setEndDate(LocalDate endDate);

  /**
   * generateEvents .
   */
  List<Event> generateEvents();

  /**
   * modifySeries.
   */
  void modifySeries(String newSubject, String newDescription, String newLocation,
                    String newStatus, boolean newRecurring);

  /**
   * contains.
   */
  boolean contains(Event e);
}
