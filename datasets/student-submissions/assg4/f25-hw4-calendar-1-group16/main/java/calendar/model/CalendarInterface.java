package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Calendar Model Interface. Event subject can be more than one word, must be wrapped in "". A
 * single event in a series cannot span over multiple days. When given start/end, must convert
 * LocalDateTime from String to LocalDateTime in Controller, property value will be handled in the
 * model.
 */

public interface CalendarInterface {
  /**
   * This method contains the logic for handling the {@code create event <eventSubject> from
   * <dateStringTtimeString> to <dateStringTtimeString>} command.
   *
   * @param subject the subject of the new event
   * @param start the start DateTime
   * @param end the end DateTime
   */
  void createFromTo(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * This method contains the logic for handling the {@code create event <eventSubject> from
   * <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times} command.
   *
   * @param subject the subject of the new event
   * @param start the start DateTime
   * @param end the end DateTime
   * @param weekdays the character assortment of days this event repeats
   * @param timesRepeated the amount of times this event repeat
   */

  void createFromToRepeatsFor(
      String subject, LocalDateTime start, LocalDateTime end, String weekdays, int timesRepeated);

  /**
   * This method contains logic for handling the {@code create event <eventSubject> from
   * <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateString>}
   * command.
   *
   * @param subject the subject of the new event
   * @param start the start DateTime
   * @param end the end DateTime
   * @param weekdays the character assortment of days this event repeats
   * @param dateUntil the date this series of events repeats until
   */

  void createFromToRepeatsUntil(
      String subject, LocalDateTime start, LocalDateTime end, String weekdays, LocalDate dateUntil);

  /**
   * This method contains logic for handling the {@code create event <eventSubject> on <dateString>}
   * command.
   *
   * @param subject the subject of the new event
   * @param date the date of this all-day event
   */

  void createOn(String subject, LocalDate date);

  /**
   * This method contains logic for handling the {@code create event <eventSubject> on <dateString>
   *   repeats <weekdays> for <N> times} command.
   *
   * @param subject the subject of the new event
   * @param date the start Date
   * @param weekdays the assortment of characters of the days of the week this event repeats on
   * @param timesRepeated how many times this event series repeats
   */

  void createOnRepeatsFor(String subject, LocalDate date, String weekdays, int timesRepeated);

  /**
   * This method contains logic for handling the {@code create event <eventSubject> on <dateString>
   *   repeats <weekdays> until <dateString>} command.
   *
   * @param subject the subject of the new event
   * @param date the start Date
   * @param weekdays the assortment of characters of the days of the week this event repeats on
   * @param dateUntil the date that this event series repeats until
   */

  void createOnRepeatsUntil(String subject, LocalDate date, String weekdays, LocalDate dateUntil);

  /**
   * This method contains logic for handling the {@code edit event <property> <eventSubject> from
   * <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>} command.
   *
   * @param propertyType the property of the event that is being edited
   * @param subject the subject of the event being edited
   * @param start the start DateTime of the event being edited
   * @param end the end DateTime of the event being edited
   * @param newPropertyValue the new property value
   */

  void editEventFromToWith(
      String propertyType, String subject, LocalDateTime start, LocalDateTime end,
      String newPropertyValue);

  /**
   * This method contains logic for handling the {@code edit events <property> <eventSubject> from
   * <dateStringTtimeString> with <NewPropertyValue>} command.
   *
   * @param propertyType the property of the events that is being edited
   * @param subject the subject of the events being edited
   * @param start the start DateTime of the events being edited
   * @param newPropertyValue the new property value
   */

  void editEventsFromWith(String propertyType, String subject, LocalDateTime start,
                          String newPropertyValue);

  /**
   * This method contains the logic for handling the {@code edit series <property> <eventSubject>
   *   from <dateStringTtimeString> with <NewPropertyValue>} command.
   *
   * @param propertyType the property of the series that is being edited
   * @param subject the subject of the series being edited
   * @param start the start DateTime of the series being edited
   * @param newPropertyValue the new property value
   */

  void editSeriesFromWith(String propertyType, String subject, LocalDateTime start,
                          String newPropertyValue);

  /**
   * This method contains the logic for handling the {@code print events on <dateString>} command.
   *
   * @param date the start Date of the event
   * @return a list of the events on the given Date
   */

  List<String[]> printOn(LocalDate date);

  /**
   * This method contains the logic for handling the {@code print events from
   * <dateStringTtimeString> to <dateStringTtimeString>} command.
   *
   * @param start the start DateTime
   * @param end the end DateTime
   * @return a list of the events that partially or completely lie in between start and end
   */

  List<String[]> printFromTo(LocalDateTime start, LocalDateTime end);

  /**
   * This method contains the logic for handling the {@code export cal fileName.csv} command.
   */

  List<String[]> exportCalendar();

  /**
   * This method contains the logic for handling the {@code show status on <dateStringTtimeString>}
   * command.
   *
   * @param date the DateTime of which we are checking for events
   * @return a boolean value true if busy, otherwise false if available
   */

  boolean showStatus(LocalDateTime date);
}
