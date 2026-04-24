package calendar.model;

import calendar.calendarentity.CalendarEntity;
import calendar.calendarentity.CalendarEntityInterface;
import calendar.event.Event;
import calendar.event.EventContext;
import calendar.event.EventInterface;
import calendar.event.EventSeries;
import calendar.event.EventWrapper;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class implements interface MultiCalendarManagerInterface and offers create, edit a calendar
 * and copy its event(s) to another calendar.
 */
public class MultiCalendarManager implements MultiCalendarManagerInterface {
  private Map<String, CalendarEntityInterface> calendarManager;
  private CalendarEntityInterface calendarEntity;

  /**
   * Constructor for MultiCalendarManager.
   */
  public MultiCalendarManager() {
    this.calendarManager = new HashMap<>();
    this.calendarEntity = null;
  }

  @Override
  public CalendarEntityInterface createCalendar(String calendarName, String timeZone)
      throws IllegalArgumentException {
    if (calendarName == null || calendarName.isEmpty() || timeZone == null || timeZone.isEmpty()) {
      throw new IllegalArgumentException("calendar name and timezone cannot be empty");
    }
    ZoneId zoneId = null;
    try {
      zoneId = ZoneId.of(timeZone);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid time zone: " + timeZone);
    }
    if (this.calendarManager.containsKey(calendarName)) {
      throw new IllegalArgumentException("Calendar with name already exists: " + calendarName);
    }
    Calendar calendar = new Calendar();
    CalendarEntityInterface entity = new CalendarEntity.CalendarEntityBuilder()
        .calendarName(calendarName)
        .timezone(zoneId)
        .calendar(calendar)
        .build();
    this.calendarManager.put(calendarName, entity);
    return entity;
  }

  @Override
  public CalendarEntityInterface editCalendar(String calendarName, String property,
                                              String propertyValue)
      throws IllegalArgumentException {
    if (calendarName == null || calendarName.isEmpty()) {
      throw new IllegalArgumentException("calendar name cannot be empty");
    }
    if (property == null || property.isEmpty() || propertyValue == null
        || propertyValue.isEmpty()) {
      throw new IllegalArgumentException("property to be updated and value cannot be empty");
    }
    if (!this.calendarManager.containsKey(calendarName)) {
      throw new IllegalArgumentException("Calendar with name does not exist: " + calendarName);
    }
    CalendarEntityInterface oldEntity = this.calendarManager.get(calendarName);

    if (property.equals("name")) {
      CalendarEntityInterface newEntity = new CalendarEntity.CalendarEntityBuilder()
          .calendarName(propertyValue.trim())
          .timezone(oldEntity.getTimeZone())
          .calendar((Calendar) oldEntity.getCalendar())
          .build();
      this.calendarManager.put(propertyValue.trim(), newEntity);
      this.calendarManager.remove(calendarName);
      this.calendarEntity = newEntity;
      return newEntity;
    } else if (property.equals("timezone")) {
      ZoneId oldZone = oldEntity.getTimeZone();
      ZoneId newZone;
      try {
        newZone = ZoneId.of(propertyValue.trim());
      } catch (DateTimeException e) {
        throw new IllegalArgumentException("Invalid time zone: " + propertyValue);
      }
      CalendarInterface calendar = oldEntity.getCalendar();
      calendar.convertTimezone(oldZone, newZone);

      CalendarEntityInterface newEntity = new CalendarEntity.CalendarEntityBuilder()
          .calendarName(calendarName)
          .timezone(newZone)
          .calendar((Calendar) calendar)
          .build();

      this.calendarManager.replace(calendarName, newEntity);
      if (this.calendarEntity != null
          && this.calendarEntity.getCalendarName().equals(calendarName)) {
        this.calendarEntity = newEntity;
      }
      return newEntity;
    } else {
      throw new IllegalArgumentException("Invalid property input: " + property);
    }
  }

  @Override
  public CalendarEntityInterface getCurrentCalendarEntity() {
    return this.calendarEntity;
  }

  @Override
  public CalendarEntityInterface getCalendarEntity(String calendarName) {
    if (this.calendarManager.containsKey(calendarName.trim())) {
      return this.calendarManager.get(calendarName.trim());
    }
    return null;
  }

  @Override
  public ZoneId getCalendarTimeZone(String calendarName) throws IllegalArgumentException {
    if (!this.calendarManager.containsKey(calendarName.trim())) {
      throw new IllegalArgumentException("Calendar with name does not exist: " + calendarName);
    }
    return this.calendarManager.get(calendarName.trim()).getTimeZone();
  }

  @Override
  public void useThisCalendarEntity(CalendarEntityInterface calendarEntity) {
    this.calendarEntity = calendarEntity;
  }

  @Override
  public void copyEvent(String subject, String startDateTime, String targetCalendarName,
                        String targetDateTime) throws IllegalArgumentException {
    if (subject == null || startDateTime == null || targetCalendarName == null
        || targetDateTime == null || subject.isEmpty() || startDateTime.isEmpty()
        || targetCalendarName.isEmpty() || targetDateTime.isEmpty()) {
      throw new IllegalArgumentException("event name, event start date/time, target calendar name,"
          + " and target start date/time cannot be empty");
    }
    verifyInputAndGetCalendar(targetCalendarName);

    LocalDateTime targetStart;
    try {
      targetStart = LocalDateTime.parse(targetDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + targetDateTime);
    }
    try {
      LocalDateTime.parse(startDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + startDateTime);
    }

    for (Event event : this.calendarEntity.getCalendar().getEvents()) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().toString().equals(startDateTime)) {
        Duration duration = Duration.between(event.getStartDateTime(), event.getEndDateTime());
        LocalDateTime targetEnd = targetStart.plus(duration);

        createSingleEventWithException(event, targetStart, targetEnd, targetCalendarName);
      }
    }

  }

  @Override
  public void copyEventsOnThatDay(String specificDate, String targetCalendarName,
                                  String targetDay) throws IllegalArgumentException {
    if (specificDate == null || targetCalendarName == null || targetDay == null
        || specificDate.isEmpty() || targetCalendarName.isEmpty() || targetDay.isEmpty()) {
      throw new IllegalArgumentException("events day, target calendar name and target day"
          + " cannot be empty");
    }
    verifyInputAndGetCalendar(targetCalendarName);

    ZoneId oldZoneId = this.calendarEntity.getTimeZone();
    ZoneId newZoneId = this.getCalendarTimeZone(targetCalendarName);
    CalendarInterface newCalendar = this.getCalendarEntity(targetCalendarName).getCalendar();
    LocalDateTime oldStart = LocalDateTime.parse(specificDate + "T00:00");
    LocalDateTime oldEnd = LocalDateTime.parse(specificDate + "T23:59");

    List<Event> oldEventsCopy = new ArrayList<>(this.calendarEntity.getCalendar().getEvents());
    for (Event event : oldEventsCopy) {
      LocalDateTime eventStart = event.getStartDateTime();
      LocalDateTime eventEnd = event.getEndDateTime();

      if (!(eventEnd.isBefore(oldStart) || eventStart.isAfter(oldEnd))) {
        LocalDateTime newStart = event.getStartDateTime().atZone(oldZoneId)
            .withZoneSameInstant(newZoneId).toLocalDateTime();
        LocalDateTime newEnd = event.getEndDateTime().atZone(oldZoneId)
            .withZoneSameInstant(newZoneId).toLocalDateTime();

        createSingleEventWithException(event, newStart, newEnd, targetCalendarName);
      }
    }

  }

  @Override
  public void copyEventsBetweenDays(String startDate, String endDate, String targetCalendarName,
                                    String targetDay) throws IllegalArgumentException {
    if (startDate == null || endDate == null || targetCalendarName == null || targetDay == null
        || startDate.isEmpty() || endDate.isEmpty() || targetCalendarName.isEmpty()
        || targetDay.isEmpty()) {
      throw new IllegalArgumentException("events start day, events end day, target calendar name"
          + " and target day cannot be empty");
    }
    verifyInputAndGetCalendar(targetCalendarName);

    LocalDateTime oldStart;
    try {
      LocalDate.parse(startDate);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + startDate);
    }
    oldStart = LocalDateTime.parse(startDate + "T00:00");
    LocalDateTime oldEnd;
    try {
      LocalDate.parse(endDate);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + endDate);
    }
    oldEnd = LocalDateTime.parse(endDate + "T23:59");
    Collection<String> mappedSeriesIds = new HashSet<>();
    LocalDate intervalStartDate = LocalDate.parse(startDate);
    LocalDate newTargetDate;
    try {
      newTargetDate = LocalDate.parse(targetDay);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + targetDay);
    }
    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(intervalStartDate, newTargetDate);

    ZoneId oldZoneId = this.calendarEntity.getTimeZone();
    ZoneId newZoneId = this.getCalendarTimeZone(targetCalendarName);
    CalendarInterface newCalendar = this.getCalendarEntity(targetCalendarName).getCalendar();
    List<Event> oldEventsCopy = new ArrayList<>(this.calendarEntity.getCalendar().getEvents());
    for (Event event : oldEventsCopy) {
      LocalDateTime eventStart = event.getStartDateTime();
      LocalDateTime eventEnd = event.getEndDateTime();

      if (!(eventEnd.isBefore(oldStart) || eventStart.isAfter(oldEnd))) {
        // single event that doesn't belong to a series
        if (event.getSeriesId() == null) {
          LocalDateTime newStart = eventStart.plusDays(dayOffset).atZone(oldZoneId)
              .withZoneSameInstant(newZoneId).toLocalDateTime();
          LocalDateTime newEnd = eventEnd.plusDays(dayOffset).atZone(oldZoneId)
              .withZoneSameInstant(newZoneId).toLocalDateTime();

          createSingleEventWithException(event, newStart, newEnd, targetCalendarName);
        } else {

          String oldSeriesId = event.getSeriesId();
          if (!mappedSeriesIds.contains(oldSeriesId)) {
            mappedSeriesIds.add(oldSeriesId);
          } else {
            continue; // has already processed this series, so skip
          }
          EventSeries oldSeries = this.calendarEntity.getCalendar().getEventSeries(oldSeriesId);

          LocalTime oldSeriesStart = oldSeries.getStartDateTime().toLocalTime();
          LocalTime oldSeriesEnd = oldSeries.getEndDateTime().toLocalTime();
          LocalDate newSeriesFirstDate = LocalDate.parse(targetDay);

          LocalDateTime oldSeriesPrunedStart =
              LocalDateTime.of(newSeriesFirstDate, oldSeriesStart);
          LocalDateTime oldSeriesPrunedEnd = LocalDateTime.of(newSeriesFirstDate, oldSeriesEnd);

          LocalDateTime newSeriesStart = oldSeriesPrunedStart.atZone(oldZoneId)
              .withZoneSameInstant(newZoneId).toLocalDateTime();
          LocalDateTime newSeriesEnd = oldSeriesPrunedEnd.atZone(oldZoneId)
              .withZoneSameInstant(newZoneId).toLocalDateTime();
          if (newSeriesStart.isBefore(
              LocalDateTime.parse(newSeriesEnd.toString().substring(0, 10) + "T00:00"))
              || newSeriesEnd.isAfter(
              LocalDateTime.parse(newSeriesStart.toString().substring(0, 10) + "T23:59"))) {
            throw new IllegalArgumentException(
                "New event in a series should not cover more than one day "
                    + "after being copied to the new calendar");
          }
          int newOccurrence = 0;
          for (EventKey key : oldSeries.getSeriesKeys()) {
            LocalDateTime eventStartTemp = key.getStartDateTime();
            LocalDateTime eventEndTemp = key.getEndDateTime();
            if (!(eventEndTemp.isBefore(oldStart) || eventStartTemp.isAfter(oldEnd))) {
              newOccurrence++;
            }
          }
          if (newOccurrence == 0) {
            continue;
          }
          try {
            EventContext context = new EventContext(oldSeries.getSubject(),
                newSeriesStart.toString(), newSeriesEnd.toString(), oldSeries.getDescription(),
                oldSeries.getLocation(), oldSeries.getEventStatus().toString());
            newCalendar.createEventSeries(
                context,
                oldSeries.getWeekdays(),
                newOccurrence,
                null
            );
          } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Event already exists")) {
              throw new IllegalArgumentException("Calendar " + targetCalendarName
                  + " already has an event with the name " + oldSeries.getSubject()
                  + " in conflict with events to be copied ");
            } else {
              throw e;
            }
          }
        }
      }
    }
  }

  @Override
  public Collection<CalendarEntityInterface> getAllCalendars() {
    return this.calendarManager.values();
  }

  @Override
  public Collection<EventWrapper> getEventsAcrossCalendar(String keyword) {
    Collection<EventWrapper> eventResult = new ArrayList<>();
    Collection<CalendarEntityInterface> calendarList = this.getAllCalendars();
    for (CalendarEntityInterface calendarEntity : calendarList) {
      Collection<Event> eventList = calendarEntity.getCalendar().getEvents();
      for (EventInterface event : eventList) {
        if (event.getSubject().equals(keyword)) {
          eventResult.add(new EventWrapper(calendarEntity.getCalendarName(),
              calendarEntity.getTimeZone().toString(), event));
        }
      }
    }
    if (!eventResult.isEmpty()) {
      return eventResult;
    }
    return null;
  }

  private void verifyInputAndGetCalendar(String targetCalendarName)
      throws IllegalArgumentException {
    if (this.getCalendarEntity(targetCalendarName) == null) {
      throw new IllegalArgumentException(
          "Calendar with name does not exist: " + targetCalendarName);
    }
    if (this.calendarEntity == null) {
      throw new IllegalArgumentException("Current calendar does not exist");
    }
  }

  private void createSingleEventWithException(Event event,
                                              LocalDateTime newStart, LocalDateTime newEnd,
                                              String targetCalendarName)
      throws IllegalArgumentException {
    try {
      EventContext context = new EventContext(event.getSubject().trim(),
          newStart.toString(), newEnd.toString(), event.getDescription(),
          event.getLocation(), event.getEventStatus().toString());
      this.getCalendarEntity(targetCalendarName).getCalendar().createSingleEvent(
          context, null);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().equals("Event already exists")) {
        throw new IllegalArgumentException("Calendar " + targetCalendarName
            + " already has an event with the name " + event.getSubject().trim()
            + ", the start date/time " + newStart.toString()
            + ", the end date/time " + newEnd.toString() + " existed ");
      }
    }
  }

}
