package calendar.model;


import calendar.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.security.auth.Subject;


/**
 * Class implementing features outlined by ICalendarModel interface.
 */
public class CalendarModel implements CalendarModelInterface {

  private Map<String, Event> calendarEvents;
  private Map<String, Event> calendarDayTime;
  private Map<String, Set<Event>> calendarDay;
  private TimeZone timeZone;

  /**
   * public constructor class for CalendarModel.
   */
  public CalendarModel(TimeZone timeZone) {
    this.calendarEvents = new HashMap<>();
    this.calendarDayTime = new HashMap<>();
    this.calendarDay = new HashMap<>();
    this.timeZone = timeZone;
  }

  @Override
  public void createEvent(String subject, Date startDateTime, Date endDateTime)
      throws IllegalArgumentException {

    Event newEvent = new SingleEvent.SingleEventBuilder()
        .addstart(startDateTime)
        .addEnd(endDateTime)
        .addSubject(subject)
        .build();
    String calendarEventsKey = createCalendarEventsKey(startDateTime, endDateTime, subject);

    if (this.calendarEvents.get(calendarEventsKey) != null) {
      throw new IllegalArgumentException("Can not make events with matching start date/time, "
          + "end date/time, and subject.");
    }
    this.calendarEvents.put(calendarEventsKey, newEvent);
    String calendarDayTimeKey = createCalendarDayTimeKey(startDateTime);

    if (this.calendarDayTime.get(calendarDayTimeKey) != null) {
      Event currentEvent = this.calendarDayTime.get(calendarDayTimeKey);
      while (currentEvent.getNextEvent() != null) {
        currentEvent = currentEvent.getNextEvent();
      }
      newEvent.setPreviousEvent(currentEvent);
      currentEvent.setNextEvent(newEvent);

    } else {
      this.calendarDayTime.put(calendarDayTimeKey, newEvent);
    }
    Date currentDateTime = (Date) startDateTime.clone();
    currentDateTime.setHours(endDateTime.getHours());
    currentDateTime.setMinutes(endDateTime.getMinutes());

    while (!currentDateTime.after(endDateTime)) {
      String calendarDayKey = createCalendarDayKey(currentDateTime);

      Set<Event> daySet = this.calendarDay.get(calendarDayKey);

      if (daySet == null) {
        daySet = new HashSet<>();
      }

      daySet.add(newEvent);

      this.calendarDay.put(calendarDayKey, daySet);

      currentDateTime.setDate(currentDateTime.getDate() + 1);
    }

  }

  /**
   * Helper method generate key to index into calendarDay map.
   *
   * @param startDate start date used for key
   * @return string key
   */
  private String createCalendarDayKey(Date startDate) {
    return Integer.toString(startDate.getDate())
        + Integer.toString(startDate.getMonth())
        + Integer.toString(startDate.getYear());
  }

  /**
   * Helper method generate a key for calendarDayTime map.
   *
   * @param startDateTime start date/time used for key
   * @return string key
   */
  private String createCalendarDayTimeKey(Date startDateTime) {
    return startDateTime.toString();
  }

  /**
   * Helper method generate a key for calendarEvents map.
   *
   * @param startDateTime start date/time used for key
   * @param endDateTime   end date/time used for key
   * @param subject       subject used for key
   * @return string key
   */
  private String createCalendarEventsKey(Date startDateTime, Date endDateTime, String subject) {
    return startDateTime.toString() + endDateTime.toString() + subject;
  }

  @Override
  public void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime,
      int[] weekdays, int repeat) throws IllegalArgumentException {

    if (subject == null || startDateTime == null || endDateTime == null || weekdays == null) {
      throw new IllegalArgumentException("Subject, start date/time, and end date/time can not be "
          + "null.");
    }

    if (repeat < 1) {
      throw new IllegalArgumentException("Event must repeat at least once.");
    }

    if (weekdaysNotValid(weekdays)) {
      throw new IllegalArgumentException("Weekday array can not have duplicates, and can not "
          + "have values outside range 0-6.");
    }

    Date currentStartDateTime = (Date) startDateTime.clone();
    Date currentEndDateTime = (Date) endDateTime.clone();
    ReoccurringEvent previousEvent = null;

    for (int i = 0; i < repeat; i++) {
      Date finalCurrentStartDateTime = currentStartDateTime;
      while (Arrays.stream(weekdays)
          .noneMatch(day -> day == finalCurrentStartDateTime.getDay())) {
        currentStartDateTime.setDate(currentStartDateTime.getDate() + 1);
        currentEndDateTime.setDate(currentEndDateTime.getDate() + 1);
      }

      previousEvent =
          (ReoccurringEvent) createAndAddReoccurringEvent(currentStartDateTime, currentEndDateTime,
              subject, previousEvent);

      currentStartDateTime = (Date) currentStartDateTime.clone();
      currentStartDateTime.setDate(currentStartDateTime.getDate() + 1);
      currentEndDateTime = (Date) currentEndDateTime.clone();
      currentEndDateTime.setDate(currentEndDateTime.getDate() + 1);
    }

  }

  /**
   * Helper method creates singular instance of a reoccurring event and checks that creation doesn't
   * violate event attribute rules, then adds event to calendar maps.
   *
   * @param startDateTime start date/time of event
   * @param endDateTime   end date/time of event
   * @param subject       subject of event
   * @param previousEvent event to set as previous event in series
   * @return the new created event
   * @throws IllegalArgumentException if event violates attribute rules
   */
  private Event createAndAddReoccurringEvent(Date startDateTime, Date endDateTime,
      String subject, ReoccurringEvent previousEvent)
      throws IllegalArgumentException {
    String calendarEventsKey = createCalendarEventsKey(startDateTime, endDateTime, subject);

    ReoccurringEvent newEvent = new ReoccurringEvent.ReoccurringEventBuilder()
        .addstart(startDateTime)
        .addEnd(endDateTime)
        .addSubject(subject)
        .addPreviousEventInSeries(previousEvent)
        .build();

    if (this.calendarEvents.get(calendarEventsKey) != null) {
      throw new IllegalArgumentException("Can not make events with matching start date/time, "
          + "end date/time, and subject.");
    }
    this.calendarEvents.put(calendarEventsKey, newEvent);

    if (previousEvent != null) {
      previousEvent.setNextEventInSeries(newEvent);
    }
    String calendarDayTimeKey = createCalendarDayTimeKey(startDateTime);

    if (this.calendarDayTime.get(calendarDayTimeKey) != null) {
      Event currentEvent = this.calendarDayTime.get(calendarDayTimeKey);
      while (currentEvent.getNextEvent() != null) {
        currentEvent = currentEvent.getNextEvent();
      }
      newEvent.setPreviousEvent(currentEvent);
      currentEvent.setNextEvent(newEvent);

    } else {
      this.calendarDayTime.put(calendarDayTimeKey, newEvent);
    }

    String calendarDayKey = createCalendarDayKey(startDateTime);

    Set<Event> daySet = this.calendarDay.get(calendarDayKey);

    if (daySet == null) {
      daySet = new HashSet<>();
    }

    daySet.add(newEvent);

    this.calendarDay.put(calendarDayKey, daySet);

    return newEvent;

  }

  /**
   * Helper method checks given array of weekdays to see if it has duplicates, values outside range
   * 0-6 or is empty.
   *
   * @param weekdays the array to check
   * @return boolean value representing if it is valid
   */
  private boolean weekdaysNotValid(int[] weekdays) {
    return !(Arrays.stream(weekdays).distinct().count() == weekdays.length
        && Arrays.stream(weekdays).allMatch(day -> day >= 0 && day <= 6)
        && weekdays.length > 0);
  }

  @Override
  public void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate) throws IllegalArgumentException {

    if (subject == null || startDateTime == null || endDateTime == null
        || weekdays == null || untilDate == null) {
      throw new IllegalArgumentException("Subject, start date/time, and end date/time can not be "
          + "null.");
    }

    if (weekdaysNotValid(weekdays)) {
      throw new IllegalArgumentException("Weekday array can not have duplicates, and can not "
          + "have values outside range 0-6.");
    }

    Date inclusiveUntilDate = (Date) untilDate.clone();
    inclusiveUntilDate.setHours(23);
    inclusiveUntilDate.setMinutes(59);

    if (inclusiveUntilDate.before(startDateTime)) {
      throw new IllegalArgumentException("The event must start before or on the until date.");
    }

    ReoccurringEvent previousEvent = null;

    Date currentStartDateTime = (Date) startDateTime.clone();
    Date currentEndDateTime = (Date) endDateTime.clone();

    Date finalCurrentStartDateTime = currentStartDateTime;
    while (Arrays.stream(weekdays)
        .noneMatch(day -> day == finalCurrentStartDateTime.getDay())) {
      currentStartDateTime.setDate(currentStartDateTime.getDate() + 1);
      currentEndDateTime.setDate(currentEndDateTime.getDate() + 1);
    }

    while (!currentStartDateTime.after(inclusiveUntilDate)) {

      previousEvent =
          (ReoccurringEvent) createAndAddReoccurringEvent(currentStartDateTime, currentEndDateTime,
              subject, previousEvent);

      currentStartDateTime = (Date) currentStartDateTime.clone();
      currentStartDateTime.setDate(currentStartDateTime.getDate() + 1);
      currentEndDateTime = (Date) currentEndDateTime.clone();
      currentEndDateTime.setDate(currentEndDateTime.getDate() + 1);

      Date finalCurrentStartDateTime1 = currentStartDateTime;
      while (Arrays.stream(weekdays)
          .noneMatch(day -> day == finalCurrentStartDateTime1.getDay())) {
        currentStartDateTime.setDate(currentStartDateTime.getDate() + 1);
        currentEndDateTime.setDate(currentEndDateTime.getDate() + 1);
      }

    }


  }

  @Override
  public void createAllDayEvent(String subject, Date startDate) throws IllegalArgumentException {

    if (subject == null || startDate == null) {
      throw new IllegalArgumentException("Subject, start date/time, and end date/time can not be "
          + "null for an event.");
    }

    Date startDateTime = (Date) startDate.clone();
    startDateTime.setHours(8);
    Date endDateTime = (Date) startDateTime.clone();
    endDateTime.setHours(17);

    this.createEvent(subject, startDateTime, endDateTime);

  }

  @Override
  public void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat)
      throws IllegalArgumentException {

    if (subject == null || startDate == null) {
      throw new IllegalArgumentException("Subject, start date/time, and end date/time can not be "
          + "null for an event.");
    }

    Date startDateTime = (Date) startDate.clone();
    startDateTime.setHours(8);
    Date endDateTime = (Date) startDateTime.clone();
    endDateTime.setHours(17);

    this.createReoccurringEvent(subject, startDateTime, endDateTime, weekdays, repeat);

  }

  @Override
  public void createAllDayEventUntil(String subject, Date startDate, int[] weekdays,
      Date untilDate) {

    if (subject == null || startDate == null) {
      throw new IllegalArgumentException("Subject, start date/time, and end date/time can not be "
          + "null for an event.");
    }

    Date startDateTime = (Date) startDate.clone();
    startDateTime.setHours(8);
    Date endDateTime = (Date) startDateTime.clone();
    endDateTime.setHours(17);

    this.createEventUntil(subject, startDateTime, endDateTime, weekdays, untilDate);


  }

  @Override
  public void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty) throws IllegalArgumentException {

    String calendarEventsKey = createCalendarEventsKey(startDateTime, endDateTime, subject);

    Event eventSearched = this.calendarEvents.get(calendarEventsKey);

    if (eventSearched == null) {
      throw new IllegalArgumentException("Can not edit non-existent event.");
    }

    Event eventSearchedEdited = eventSearched.createEditedCopy(property, newProperty);

    if (editResultsInvalid(eventSearchedEdited, eventSearched)) {
      throw new IllegalArgumentException("Edit violates rules of event definition.");

    } else {
      editProperty(eventSearched, property, newProperty);
    }

  }

  /**
   * Helper method checks if edit violates rules, makes edit and updates required calendar maps.
   *
   * @param eventSearched the event to edit
   * @param property      the property to edit
   * @param newProperty   the new value of the property
   */
  private void editProperty(Event eventSearched, Property property, Object newProperty) {
    if (property == Property.START) {
      changeStartProperty(eventSearched, property, newProperty);

    } else if (property == Property.END) {
      Date currentDateTime = (Date) eventSearched.getStart().clone();
      currentDateTime.setHours(eventSearched.getEnd().getHours());
      currentDateTime.setMinutes(eventSearched.getEnd().getMinutes());

      while (!currentDateTime.after(eventSearched.getEnd())) {
        String calendarDayKey = createCalendarDayKey(currentDateTime);

        Set<Event> daySet = this.calendarDay.get(calendarDayKey);

        daySet.remove(eventSearched);

        currentDateTime.setDate(currentDateTime.getDate() + 1);
      }

      eventSearched.editProperty(property, newProperty);

      currentDateTime = (Date) eventSearched.getStart().clone();
      currentDateTime.setHours(eventSearched.getEnd().getHours());
      currentDateTime.setMinutes(eventSearched.getEnd().getMinutes());

      while (!currentDateTime.after(eventSearched.getEnd())) {
        String calendarDayKey = createCalendarDayKey(currentDateTime);

        Set<Event> daySet = this.calendarDay.get(calendarDayKey);

        if (daySet == null) {
          daySet = new HashSet<>();
        }

        daySet.add(eventSearched);

        this.calendarDay.put(calendarDayKey, daySet);

        currentDateTime.setDate(currentDateTime.getDate() + 1);
      }


    } else {
      eventSearched.editProperty(property, newProperty);
    }
  }

  /**
   * Helper method to specifically handle changing the start property of an event. Updates all
   * calendar maps.
   *
   * @param eventSearched the event to edit
   * @param property      always property.START
   * @param newProperty   the new value of start
   */
  private void changeStartProperty(Event eventSearched, Property property, Object newProperty) {
    String calendarEventsKey = createCalendarEventsKey(eventSearched.getStart(),
        eventSearched.getEnd(), eventSearched.getSubject());
    this.calendarEvents.remove(calendarEventsKey);

    String calendarDayTimeKey = createCalendarDayTimeKey(eventSearched.getStart());
    if (this.calendarDayTime.get(calendarDayTimeKey) == eventSearched) {
      if (eventSearched.getNextEvent() != null) {
        this.calendarDayTime.put(calendarDayTimeKey, eventSearched.getNextEvent());
      } else {
        this.calendarDayTime.remove(calendarDayTimeKey);
      }
    }
    if (eventSearched.getNextEvent() != null) {
      eventSearched.getNextEvent().setPreviousEvent(eventSearched.getPreviousEvent());
    }
    if (eventSearched.getPreviousEvent() != null) {
      eventSearched.getPreviousEvent().setNextEvent(eventSearched.getNextEvent());
    }
    Date currentDateTime = (Date) eventSearched.getStart().clone();
    currentDateTime.setHours(eventSearched.getEnd().getHours());
    currentDateTime.setMinutes(eventSearched.getEnd().getMinutes());

    while (!currentDateTime.after(eventSearched.getEnd())) {
      String calendarDayKey = createCalendarDayKey(currentDateTime);

      Set<Event> daySet = this.calendarDay.get(calendarDayKey);

      daySet.remove(eventSearched);

      currentDateTime.setDate(currentDateTime.getDate() + 1);
    }

    eventSearched.editProperty(property, newProperty);

    calendarEventsKey = createCalendarEventsKey(eventSearched.getStart(), eventSearched.getEnd(),
        eventSearched.getSubject());

    this.calendarEvents.put(calendarEventsKey, eventSearched);

    calendarDayTimeKey = createCalendarDayTimeKey(eventSearched.getStart());

    if (this.calendarDayTime.get(calendarDayTimeKey) != null) {
      Event currentEvent = this.calendarDayTime.get(calendarDayTimeKey);
      while (currentEvent.getNextEvent() != null) {
        currentEvent = currentEvent.getNextEvent();
      }
      eventSearched.setPreviousEvent(currentEvent);
      currentEvent.setNextEvent(eventSearched);

    } else {
      this.calendarDayTime.put(calendarDayTimeKey, eventSearched);
    }

    currentDateTime = (Date) eventSearched.getStart().clone();
    currentDateTime.setHours(eventSearched.getEnd().getHours());
    currentDateTime.setMinutes(eventSearched.getEnd().getMinutes());

    while (!currentDateTime.after(eventSearched.getEnd())) {
      String calendarDayKey = createCalendarDayKey(currentDateTime);

      Set<Event> daySet = this.calendarDay.get(calendarDayKey);

      if (daySet == null) {
        daySet = new HashSet<>();
      }

      daySet.add(eventSearched);

      this.calendarDay.put(calendarDayKey, daySet);

      currentDateTime.setDate(currentDateTime.getDate() + 1);
    }
  }

  /**
   * Helper method to check if edit would result in invalid event.
   *
   * @param eventSearchedEdited the edited copy of an event
   * @param originalEvent       the original event
   * @return boolean representing if edit is invalid
   */
  private boolean editResultsInvalid(Event eventSearchedEdited, Event originalEvent) {

    if (eventSearchedEdited.getStart() == null
        || eventSearchedEdited.getEnd() == null
        || eventSearchedEdited.getSubject() == null) {
      return true;
    }

    String calendarEventsKey = createCalendarEventsKey(eventSearchedEdited.getStart(),
        eventSearchedEdited.getEnd(), eventSearchedEdited.getSubject());

    return ((this.calendarEvents.get(calendarEventsKey) != null
        && this.calendarEvents.get(calendarEventsKey) != originalEvent)
        || eventSearchedEdited.getStart().after(eventSearchedEdited.getEnd()));

  }

  @Override
  public void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty) throws IllegalArgumentException {

    String calendarDayTimeKey = createCalendarDayTimeKey(startDateTime);

    Event currentEvent = this.calendarDayTime.get(calendarDayTimeKey);

    if (currentEvent == null) {
      throw new IllegalArgumentException("Can not edit series of event that does not exist.");
    }

    while (currentEvent != null) {

      editPropertyCascadeDown(currentEvent, property, newProperty);

      currentEvent = currentEvent.getNextEvent();
    }

  }

  /**
   * Helper method to edit property of event and all events after in series if applicable.
   *
   * @param currentEvent the event being edited
   * @param property     the property to edit
   * @param newProperty  the new value of the property
   * @throws IllegalArgumentException if edit results in invalid event
   */
  private void editPropertyCascadeDown(Event currentEvent, Property property,
      Object newProperty) throws IllegalArgumentException {

    List<Event> allAfterInSeries = new ArrayList<>();
    allAfterInSeries = currentEvent.addThisAndAllAfter(allAfterInSeries);

    for (int i = 0; i < allAfterInSeries.size(); i++) {
      Object correctPropertyToUse = newProperty;

      if (property == Property.START) {
        correctPropertyToUse = allAfterInSeries.get(i).getCorrectStartProperty(newProperty,
            currentEvent.getStart());
      } else if (property == Property.END) {
        correctPropertyToUse = allAfterInSeries.get(i).getCorrectEndProperty(newProperty,
            currentEvent.getEnd());
      }

      Event eventSearchedEdited = allAfterInSeries.get(i).createEditedCopy(property,
          correctPropertyToUse);

      if (editResultsInvalid(eventSearchedEdited, allAfterInSeries.get(i))) {
        throw new IllegalArgumentException("Edit violates rules of event definition.");

      } else {
        editProperty(allAfterInSeries.get(i), property, correctPropertyToUse);
        if (property == Property.START && i > 0) {
          ((ReoccurringEvent) allAfterInSeries.get(i)).setPreviousEventInSeries(
              (ReoccurringEvent) allAfterInSeries.get(i - 1));
          ((ReoccurringEvent) allAfterInSeries.get(i - 1)).setNextEventInSeries(
              (ReoccurringEvent) allAfterInSeries.get(i));
        }
      }
    }


  }

  @Override
  public void editSeries(Property property, String subject, Date startDateTime,
      Object newProperty) throws IllegalArgumentException {

    String calendarDayTimeKey = createCalendarDayTimeKey(startDateTime);

    Event currentEvent = this.calendarDayTime.get(calendarDayTimeKey);

    if (currentEvent == null) {
      throw new IllegalArgumentException("Can not edit series of event that does not exist.");
    }

    while (currentEvent != null) {

      editPropertyCascadeUpAndDown(currentEvent, property, newProperty);

      currentEvent = currentEvent.getNextEvent();
    }


  }

  /**
   * Helper method to edit all events in series of specified event.
   *
   * @param currentEvent the event to edit
   * @param property     the property to edit
   * @param newProperty  the new value of the property
   * @throws IllegalArgumentException if edit results in invalid event
   */
  private void editPropertyCascadeUpAndDown(Event currentEvent, Property property,
      Object newProperty) throws IllegalArgumentException {

    List<Event> allBeforeAndAfterInSeries = new ArrayList<>();
    Event firstEventInSeries = currentEvent.getFirstEventInSeries();
    allBeforeAndAfterInSeries = firstEventInSeries.addThisAndAllAfter(allBeforeAndAfterInSeries);

    for (int i = 0; i < allBeforeAndAfterInSeries.size(); i++) {

      Object correctPropertyToUse = newProperty;

      if (property == Property.START) {
        correctPropertyToUse =
            allBeforeAndAfterInSeries.get(i).getCorrectStartProperty(newProperty,
                currentEvent.getStart());
      } else if (property == Property.END) {
        correctPropertyToUse = allBeforeAndAfterInSeries.get(i).getCorrectEndProperty(newProperty,
            currentEvent.getStart());
      }

      Event eventSearchedEdited =
          allBeforeAndAfterInSeries.get(i).createEditedCopy(property, correctPropertyToUse);

      if (editResultsInvalid(eventSearchedEdited, allBeforeAndAfterInSeries.get(i))) {
        throw new IllegalArgumentException("Edit violates rules of event definition.");

      } else {
        editProperty(allBeforeAndAfterInSeries.get(i), property, correctPropertyToUse);
        if (property == Property.START && i > 0) {
          ((ReoccurringEvent) allBeforeAndAfterInSeries.get(i)).setPreviousEventInSeries(
              (ReoccurringEvent) allBeforeAndAfterInSeries.get(i - 1));
          ((ReoccurringEvent) allBeforeAndAfterInSeries.get(i - 1)).setNextEventInSeries(
              (ReoccurringEvent) allBeforeAndAfterInSeries.get(i));
        }
      }
    }

  }

  @Override
  public Set<Event> getEventsOnDate(Date date) {

    String calendarDayKey = createCalendarDayKey(date);
    return this.calendarDay.get(calendarDayKey);

  }

  @Override
  public Set<Event> getEventsInRange(Date startDateTime, Date endDateTime) {
    Set<Event> returnSet = new HashSet<>();
    Date currentDate = (Date) startDateTime.clone();
    currentDate.setHours(endDateTime.getHours());
    currentDate.setMinutes(endDateTime.getMinutes());

    while (!currentDate.after(endDateTime)) {
      String calendarDayKey = createCalendarDayKey(currentDate);
      Set<Event> daysEvents = this.calendarDay.get(calendarDayKey);

      if (daysEvents != null) {
        daysEvents.stream()
            .filter(event -> !event.getStart().after(endDateTime)
                && !event.getEnd().before(startDateTime))
            .forEach(returnSet::add);
      }

      currentDate.setDate(currentDate.getDate() + 1);
    }
    return returnSet;
  }

  @Override
  public boolean eventAt(Date dateTime) {
    return !this.getEventsInRange(dateTime, dateTime).isEmpty();
  }

  @Override
  public TimeZone getTimeZone() {
    return this.timeZone;
  }

  @Override
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }
}
