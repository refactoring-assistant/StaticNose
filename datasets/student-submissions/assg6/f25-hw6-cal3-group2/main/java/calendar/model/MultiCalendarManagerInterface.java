package calendar.model;

import calendar.calendarentity.CalendarEntityInterface;
import calendar.event.EventWrapper;
import java.time.ZoneId;
import java.util.Collection;

/**
 * This interface contains necessary operations that a Multi Calendar Manager should support.
 */
public interface MultiCalendarManagerInterface {

  /**
   * To create a calendar entity with name and timezone.
   *
   * @param calendarName the name of the calendar entity
   * @param timeZone     the timezone of the calendar entity
   * @return the calendar entity with name and timezone
   */
  CalendarEntityInterface createCalendar(String calendarName, String timeZone);

  /**
   * To update a property of the calendar entity with new value.
   *
   * @param calendarName the name of the calendar entity
   * @param property the property of the calendar entity
   * @param propertyValue the new value of the property
   * @return the new calendar entity with name and timezone
   */
  CalendarEntityInterface editCalendar(String calendarName, String property, String propertyValue);

  /**
   * To get the current calendar entity after use.
   *
   * @return the current calendar entity
   */
  CalendarEntityInterface getCurrentCalendarEntity();

  /**
   * To get a calendar entity with given name.
   *
   * @param calendarName the name of the calendar entity
   * @return the CalendarEntity with that name
   */
  CalendarEntityInterface getCalendarEntity(String calendarName);

  /**
   * To get the timezone of the calendar entity with given name.
   *
   * @param calendarName the name of the calendar entity
   * @return the timezone of the calendar entity
   */
  ZoneId getCalendarTimeZone(String calendarName);

  /**
   * To designate the calendarEntity as the context calendar entity.
   *
   * @param calendarEntity the context calendar entity
   */
  void useThisCalendarEntity(CalendarEntityInterface calendarEntity);

  /**
   * To copy a single event with given start date/time from one calendar entity
   * to another calendar entity falling on specific target date/time.
   *
   * @param subject            the subject of the single event
   * @param startDateTime      the start date/time of the event to-be-copied
   * @param targetCalendarName the name of the target calendar entity
   * @param targetDateTime     the target date/time of the target calendar entity
   */
  void copyEvent(String subject, String startDateTime, String targetCalendarName,
                 String targetDateTime);

  /**
   * To copy multiple events on the specific date from one calendar entity
   * to another calendar entity falling on specific target day.
   *
   * @param specificDate       the start day on which the multiple events start
   * @param targetCalendarName the name of the target calendar entity
   * @param targetDay          the target date of the target calendar entity
   */
  void copyEventsOnThatDay(String specificDate, String targetCalendarName, String targetDay);

  /**
   * To copy multiple events from a start date to an end date
   * from one calendar entity to another calendar entity falling on the specific target start day.
   *
   * @param startDate          the start day on which the multiple events start
   * @param endDate            the end day on which the multiple events start
   * @param targetCalendarName the name of the target calendar entity
   * @param targetDay          the target date of the target calendar entity
   */
  void copyEventsBetweenDays(String startDate, String endDate, String targetCalendarName,
                             String targetDay);

  /**
   * Get all the calendar entities in the manager.
   *
   * @return the collection of calendar entities
   */
  Collection<CalendarEntityInterface> getAllCalendars();

  /**
   * Get the event collection of events with subject matching the given keyword.
   *
   * @param keyword the searching keyword for matching the event subject
   * @return a collection of events with subject matching the given keyword
   */
  Collection<EventWrapper> getEventsAcrossCalendar(String keyword);
}
