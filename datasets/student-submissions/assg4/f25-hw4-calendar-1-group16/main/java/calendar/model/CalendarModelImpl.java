package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Calendar Model Implementation. It contains methods that contains the logic for every command a
 * calendar can currently support. A calendar stores events within two Maps, one by their series,
 * and one by their starting dates. It also contains a map of known attributes for events.
 */

public class CalendarModelImpl implements CalendarInterface {

  private final Map<String, List<EventInterface>> seriesMap;
  private final Map<LocalDate, List<EventInterface>> dateMap;
  private final Map<String, BiConsumer<String, List<EventInterface>>> knownAttributes;
  private String currentSeriesId;
  private final Map<String, DayOfWeek> daysOfWeek;

  /**
   * Constructs a Calendar Object, which creates the dateMap and seriesMap for storing events, adds
   * a map for the knownAttributes, and a map of weekdays, and sets the currentSeriesId to 1.
   */

  public CalendarModelImpl() {
    seriesMap = new HashMap<>();
    dateMap = new HashMap<>();

    knownAttributes = new HashMap<>();
    knownAttributes.put("subject", this::setSubject);
    knownAttributes.put("start", this::setStart);
    knownAttributes.put("end", this::setEnd);
    knownAttributes.put("description", this::setDescription);
    knownAttributes.put("location", this::setLocation);
    knownAttributes.put("status", this::setStatus);

    daysOfWeek = Map.of(
        "M", DayOfWeek.MONDAY,
        "T", DayOfWeek.TUESDAY,
        "W", DayOfWeek.WEDNESDAY,
        "R", DayOfWeek.THURSDAY,
        "F", DayOfWeek.FRIDAY,
        "S", DayOfWeek.SATURDAY,
        "U", DayOfWeek.SUNDAY
    );

    currentSeriesId = "1";
  }

  /**
   * Creates a single event with the given subject, start DateTime, and end DateTime. It then stores
   * the new event into the two maps accordingly. It throws an Exception if the newly created event
   * has the same subject, start, and end of an already existing event.
   *
   * @param subject the subject of the new event
   * @param start the start DateTime
   * @param end the end DateTime
   * @throws IllegalArgumentException if the newly created event equals an existing event
   */

  @Override
  public void createFromTo(String subject, LocalDateTime start, LocalDateTime end)
      throws IllegalArgumentException {
    EventInterface newEvent = new Event(subject, start, end);
    addEvent(newEvent, start.toLocalDate());
  }

  /**
   * Creates a series of events with the given subject, start DateTime, and endDatTime. It repeats
   * on the given weekdays, and repeats N times, defined by timesRepeated.
   *
   * @param subject the subject of the new event
   * @param start the start DateTime
   * @param end the end DateTime
   * @param weekdays the character assortment of days this event repeats
   * @param timesRepeated the amount of times this event repeat
   * @throws IllegalArgumentException if the event start and end span more than one day, or if an
   *                                  event that is a part of the series has the same subject,
   *                                  start, and end as another
   */

  @Override
  public void createFromToRepeatsFor(String subject, LocalDateTime start, LocalDateTime end,
                                     String weekdays, int timesRepeated)
      throws IllegalArgumentException {
    checkStartEnd(start, end);

    if (timesRepeated < 1) {
      throw  new IllegalArgumentException("N should be positive");
    }

    DayOfWeek[] weekdaysArray = getDaysOfWeek(weekdays);

    EventInterface newEvent = new Event(subject, start, end);
    newEvent.setSeriesId(currentSeriesId);
    List<EventInterface> tempSeries = new ArrayList<>();
    tempSeries.add(newEvent);

    LocalDateTime currentStartDate = start.plusDays(1);
    LocalDateTime currentEndDate = end.plusDays(1);
    int eventsCreated = 1;

    while (eventsCreated < timesRepeated) {
      if (Arrays.asList(weekdaysArray).contains(currentStartDate.getDayOfWeek())) {
        EventInterface nextEvent = new Event(subject, currentStartDate, currentEndDate);
        nextEvent.setSeriesId(currentSeriesId);
        tempSeries.add(nextEvent);
        eventsCreated += 1;
      }
      currentStartDate = currentStartDate.plusDays(1);
      currentEndDate = currentEndDate.plusDays(1);
    }
    validateSeries(tempSeries);
  }

  /**
   * Creates a series of events with the given subject, start DateTime, and endDatTime. It repeats
   * on the given weekdays, and repeats until a given date.
   *
   * @param subject the subject of the new event
   * @param start the start DateTime
   * @param end the end DateTime
   * @param weekdays the character assortment of days this event repeats
   * @param dateUntil the date this series of events repeats until
   * @throws IllegalArgumentException if the event start and end span more than one day, or if an
   *                                  event that is a part of the series has the same subject,
   *                                  start, and end as another
   */

  @Override
  public void createFromToRepeatsUntil(String subject, LocalDateTime start, LocalDateTime end,
                                       String weekdays, LocalDate dateUntil)
      throws IllegalArgumentException {
    checkStartEnd(start, end);

    DayOfWeek[] weekdaysArray = getDaysOfWeek(weekdays);

    EventInterface newEvent = new Event(subject, start, end);
    newEvent.setSeriesId(currentSeriesId);
    List<EventInterface> tempSeries = new ArrayList<>();
    tempSeries.add(newEvent);

    LocalDateTime currentStartDate = start.plusDays(1);
    LocalDateTime currentEndDate = end.plusDays(1);

    while (currentStartDate.toLocalDate().isBefore(dateUntil)) {
      if (Arrays.asList(weekdaysArray).contains(currentStartDate.getDayOfWeek())) {
        EventInterface nextEvent = new Event(subject, currentStartDate, currentEndDate);
        nextEvent.setSeriesId(currentSeriesId);
        tempSeries.add(nextEvent);
      }
      currentStartDate = currentStartDate.plusDays(1);
      currentEndDate = currentEndDate.plusDays(1);
    }
    validateSeries(tempSeries);
  }

  /**
   * Creates a single, all-day event with the given subject, and the given date the event is
   * occurring. It then stores the event into the two maps accordingly.
   *
   * @param subject the subject of the new event
   * @param date the date of this all-day event
   * @throws IllegalArgumentException if the new event is equal to another existing event
   */

  @Override
  public void createOn(String subject, LocalDate date) throws IllegalArgumentException {
    EventInterface newEvent = new Event(subject, date);
    addEvent(newEvent, date);
  }

  /**
   * Creates a series of all-day events with the given subject, start DateTime, and endDatTime. It
   * repeats on the given weekdays, and repeats N times, defined by timesRepeated.
   *
   * @param subject the subject of the new event
   * @param date the start Date
   * @param weekdays the assortment of characters of the days of the week this event repeats on
   * @param timesRepeated how many times this event series repeats
   * @throws IllegalArgumentException if the event start and end span more than one day, or if an
   *                                  event that is a part of the series has the same subject,
   *                                  start, and end as another
   */

  @Override
  public void createOnRepeatsFor(String subject, LocalDate date, String weekdays,
                                 int timesRepeated) throws IllegalArgumentException {

    if (timesRepeated < 1) {
      throw  new IllegalArgumentException("N should be positive");
    }

    DayOfWeek[] weekdaysArray = getDaysOfWeek(weekdays);

    EventInterface newEvent = new Event(subject, date);
    newEvent.setSeriesId(currentSeriesId);
    List<EventInterface> tempSeries = new ArrayList<>();
    tempSeries.add(newEvent);

    LocalDate currentStartDate = date.plusDays(1);
    int eventsCreated = 1;

    while (eventsCreated < timesRepeated) {
      if (Arrays.asList(weekdaysArray).contains(currentStartDate.getDayOfWeek())) {
        EventInterface nextEvent = new Event(subject, currentStartDate);
        nextEvent.setSeriesId(currentSeriesId);
        tempSeries.add(nextEvent);
        eventsCreated += 1;
      }
      currentStartDate = currentStartDate.plusDays(1);
    }
    validateSeries(tempSeries);
  }

  /**
   * Creates a series of all-day events with the given subject, start DateTime, and endDatTime. It
   * repeats on the given weekdays, and repeats until a given date.
   *
   * @param subject the subject of the new event
   * @param date the start Date
   * @param weekdays the assortment of characters of the days of the week this event repeats on
   * @param dateUntil the date that this event series repeats until
   * @throws IllegalArgumentException if the event start and end span more than one day, or if an
   *                                  event that is a part of the series has the same subject,
   *                                  start, and end as another
   */

  @Override
  public void createOnRepeatsUntil(String subject, LocalDate date, String weekdays,
                                   LocalDate dateUntil) throws IllegalArgumentException {
    DayOfWeek[] weekdaysArray = getDaysOfWeek(weekdays);

    EventInterface newEvent = new Event(subject, date);
    newEvent.setSeriesId(currentSeriesId);
    List<EventInterface> tempSeries = new ArrayList<>();
    tempSeries.add(newEvent);

    LocalDate currentStartDate = date.plusDays(1);

    while (currentStartDate.isBefore(dateUntil)) {
      if (Arrays.asList(weekdaysArray).contains(currentStartDate.getDayOfWeek())) {
        EventInterface nextEvent = new Event(subject, currentStartDate);
        nextEvent.setSeriesId(currentSeriesId);
        tempSeries.add(nextEvent);
      }
      currentStartDate = currentStartDate.plusDays(1);
    }
    validateSeries(tempSeries);
  }

  /**
   * Edits an event's specified propertyType that has the given subject, start, and end. If the
   * event does not exist, or the result of the edit breaks the rule of event's subject, start, and
   * end, it will throw an Exception. It will also throw an Exception if the start or end is not in
   * a valid format. Otherwise, it replaces the old event with a new event with the updated property
   * value.
   *
   * @param propertyType the property of the event that is being edited
   * @param subject the subject of the event being edited
   * @param start the start DateTime of the event being edited
   * @param end the end DateTime of the event being edited
   * @param newPropertyValue the new property value
   * @throws IllegalArgumentException if the data is invalid or the resulting edit causes duplicates
   */

  @Override
  public void editEventFromToWith(String propertyType, String subject, LocalDateTime start,
                                  LocalDateTime end, String newPropertyValue)
      throws IllegalArgumentException {
    EventInterface dummy = new Event(subject, start, end);
    List<EventInterface> currentDate = dateMap.get(start.toLocalDate());
    if (currentDate == null) {
      throw new IllegalArgumentException("There is no event at this date.");
    }
    BiConsumer<String, List<EventInterface>> updater
        = knownAttributes.get(propertyType);
    for (int i = 0; i < currentDate.size(); i++) {
      EventInterface event = currentDate.get(i);
      if (event.equals(dummy)) {
        List<EventInterface> events = new ArrayList<>();
        events.add(event);
        updater.accept(newPropertyValue, events);
        if (propertyType.equals("start")) {
          currentDate.remove(i);
          addEvent(event, event.getStartDateTime().toLocalDate());
          if (!Objects.equals(event.getSeriesId(), "None")) {
            List<EventInterface> eventSeries = seriesMap.get(event.getSeriesId());
            eventSeries.remove(event);
            event.setSeriesId(null);
          }
        }
        return;
      }
    }
    throw new IllegalArgumentException("Event not found.");
  }

  /**
   * Edits event(s) on given start DateTime with the given property type and given property value.
   * If the event being edited is a part of a series, then it edits all events within that series
   * after that given series, including the edited one. If the start time is edited on an event in
   * a series, a new series is attempted to be made.
   *
   * @param propertyType the property of the events that is being edited
   * @param subject the subject of the events being edited
   * @param start the start DateTime of the events being edited
   * @param newPropertyValue the new property value
   * @throws IllegalArgumentException if the data is invalid or the resulting edit causes duplicates
   */

  @Override
  public void editEventsFromWith(String propertyType, String subject, LocalDateTime start,
                                 String newPropertyValue) throws IllegalArgumentException {
    List<EventInterface> currentDate = dateMap.get(start.toLocalDate());
    if (currentDate == null) {
      throw new  IllegalArgumentException("There is no event at this date.");
    }

    List<EventInterface> events = new ArrayList<>();
    BiConsumer<String, List<EventInterface>> updater
        = knownAttributes.get(propertyType);
    for (EventInterface event : currentDate) {
      if (!Objects.equals(event.getStartDateTime(), start)
          || !Objects.equals(event.getSubject(), subject)) {
        continue;
      }
      events.add(event);
    }
    if (events.isEmpty()) {
      throw new IllegalArgumentException("There is no event at this date.");
    }
    for (EventInterface event : events) {
      if (Objects.equals(event.getSeriesId(), "None")) {
        List<EventInterface> singleEvent = new ArrayList<>();
        singleEvent.add(event);
        updater.accept(newPropertyValue, singleEvent);
        if (propertyType.equals("start")) {
          currentDate.remove(event);
          addEvent(event, event.getStartDateTime().toLocalDate());
        }
      } else {
        List<EventInterface> eventSeries = seriesMap.get(event.getSeriesId());
        List<EventInterface> newEventSeries = new ArrayList<>();
        for (int j = 0; j < eventSeries.size(); j++) {
          if (eventSeries.get(j).equals(event)) {
            newEventSeries = new ArrayList<>(eventSeries.subList(j, eventSeries.size()));
            break;
          }
        }
        int indexOfFirstValidEvent = eventSeries.indexOf(newEventSeries.get(0));
        updater.accept(newPropertyValue, newEventSeries);
        if (propertyType.equals("start")) {
          if (indexOfFirstValidEvent == 0) {
            continue;
          } else if (indexOfFirstValidEvent == eventSeries.size() - 1) {
            newEventSeries.get(0).setSeriesId(null);
            eventSeries.remove(newEventSeries.get(0));
          } else {
            for (EventInterface newEvent : newEventSeries) {
              newEvent.setSeriesId(currentSeriesId);
              eventSeries.remove(newEvent);
            }
            validateSeries(newEventSeries);
          }
          for (EventInterface updateEvent : newEventSeries) {
            List<EventInterface> eventsDates = dateMap.get(
                updateEvent.getStartDateTime().toLocalDate());
            eventsDates.remove(updateEvent);
            addEvent(updateEvent, updateEvent.getStartDateTime().toLocalDate());
          }
        }
      }
    }
  }

  /**
   * Edits event(s) on given start DateTime with the given property type and given property value.
   * If the event being edited is a part of a series, then it edits all events within that series.
   * If the start time is edited on an event in a series, a new series is attempted to be made.
   *
   * @param propertyType the property of the series that is being edited
   * @param subject the subject of the series being edited
   * @param start the start DateTime of the series being edited
   * @param newPropertyValue the new property value
   * @throws IllegalArgumentException if the data is invalid or the resulting edit causes duplicates
   */

  @Override
  public void editSeriesFromWith(String propertyType, String subject, LocalDateTime start,
                                 String newPropertyValue) throws IllegalArgumentException {
    List<EventInterface> currentDate = dateMap.get(start.toLocalDate());
    if (currentDate == null) {
      throw new  IllegalArgumentException("There is no event at this date.");
    }

    List<EventInterface> events = new ArrayList<>();
    BiConsumer<String, List<EventInterface>> updater
        = knownAttributes.get(propertyType);
    for (EventInterface event : currentDate) {
      if (!Objects.equals(event.getStartDateTime(), start)
          || !Objects.equals(event.getSubject(), subject)) {
        continue;
      }
      events.add(event);
    }
    if (events.isEmpty()) {
      throw new IllegalArgumentException("There is no event at this date.");
    }
    for (EventInterface event : events) {
      if (Objects.equals(event.getSeriesId(), "None")) {
        List<EventInterface> singleEvent = new ArrayList<>();
        singleEvent.add(event);
        updater.accept(newPropertyValue, singleEvent);
        if (propertyType.equals("start")) {
          currentDate.remove(event);
          addEvent(event, event.getStartDateTime().toLocalDate());
        }
      } else {
        List<EventInterface> eventSeries = seriesMap.get(event.getSeriesId());
        updater.accept(newPropertyValue, eventSeries);
        if (propertyType.equals("start")) {
          for (EventInterface updateEvent : eventSeries) {
            List<EventInterface> eventsDates = dateMap.get(
                updateEvent.getStartDateTime().toLocalDate());
            eventsDates.remove(updateEvent);
            addEvent(updateEvent, updateEvent.getStartDateTime().toLocalDate());
          }
        }
      }
    }
  }

  /**
   * Gets all events that fall on the given date, and returns them as a list.
   *
   * @param date the start Date of the event
   * @return the list of events that fall on that date
   * @throws IllegalArgumentException if the given date has no events
   */

  @Override
  public List<String[]> printOn(LocalDate date) throws IllegalArgumentException {
    if (!dateMap.containsKey(date)) {
      throw new  IllegalArgumentException("No events on this date.");
    }
    List<EventInterface> events = dateMap.get(date);
    List<String[]> formattedEvents = new ArrayList<>();
    for (EventInterface event : events) {
      if (event.getLocation() == null) {
        formattedEvents.add(new String[]{event.getSubject(), event.getStartDateTime().toString(),
            event.getEndDateTime().toString(), ""});
        continue;
      }
      formattedEvents.add(new String[]{event.getSubject(), event.getStartDateTime().toString(),
          event.getEndDateTime().toString(), event.getLocation()});
    }
    return formattedEvents;
  }

  /**
   * Gets all the events that fall within the given DateTime start and DateTime end, and returns
   * them as a list.
   *
   * @param start the start DateTime
   * @param end the end DateTime
   * @return the list of events that fall within that DateTime range
   * @throws IllegalArgumentException if the start date is after the end date
   */

  @Override
  public List<String[]> printFromTo(LocalDateTime start, LocalDateTime end)
      throws IllegalArgumentException {
    if (start.isAfter(end)) {
      throw new  IllegalArgumentException("Start date must be after end date.");
    }
    List<EventInterface> events = new ArrayList<>();
    LocalDate startInterval = start.toLocalDate();
    LocalDate endInterval = end.toLocalDate();

    while (!startInterval.isAfter(endInterval)) {
      List<EventInterface> dayEvents = dateMap.get(startInterval);
      if (dayEvents != null) {
        for (EventInterface event : dayEvents) {
          if (eventsOverlap(event.getStartDateTime(), event.getEndDateTime(), start, end)) {
            events.add(event);
          }
        }
      }
      startInterval = startInterval.plusDays(1);
    }
    List<String[]> formattedEvents = new ArrayList<>();
    for (EventInterface event : events) {
      if (event.getLocation() == null) {
        formattedEvents.add(new String[]{event.getSubject(),
            event.getStartDateTime().toLocalDate().toString(),
            event.getStartDateTime().toLocalTime().toString(),
            event.getEndDateTime().toLocalDate().toString(),
            event.getEndDateTime().toLocalTime().toString(),
            ""
        });
        continue;
      }
      formattedEvents.add(new String[]{event.getSubject(),
          event.getStartDateTime().toLocalDate().toString(),
          event.getStartDateTime().toLocalTime().toString(),
          event.getEndDateTime().toLocalDate().toString(),
          event.getEndDateTime().toLocalTime().toString(),
          event.getLocation()
      });
    }

    return formattedEvents;
  }

  /**
   * Exports all events in this calendar to a csv file within a List of String Arrays, which
   * contains all event's subject, start Date and Time, and end Date and Time.
   */

  @Override
  public List<String[]> exportCalendar() {
    List<String[]> result = new ArrayList<>();
    result.add(new String[] {"Subject", "Start Date", "Start Time", "End Date",
        "End Time", "All Day Event", "Description", "Location", "Private"});
    for (List<EventInterface> events : dateMap.values()) {
      events.sort(Comparator.comparing(EventInterface::getStartDateTime));
      for (EventInterface event : events) {
        String startTime = formatTimeForGoogle(event.getStartDateTime().toLocalTime());
        String endTime = formatTimeForGoogle(event.getEndDateTime().toLocalTime());
        boolean isAllDay = event.getStartDateTime().toLocalTime().toString().equals("08:00")
            && event.getEndDateTime().toLocalTime().toString().equals("17:00");
        result.add(new String[] {event.getSubject(),
            event.getStartDateTime().toLocalDate().toString(),
            startTime,
            event.getEndDateTime().toLocalDate().toString(),
            endTime,
            isAllDay ? "True" : "False",
            event.getDescription() != null ? event.getDescription() : "",
            event.getLocation() != null ? event.getLocation() : "",
            event.getStatus() == EventStatus.PRIVATE ? "True" : "False"});
      }
    }
    return result;
  }

  /**
   * Shows the status of an individual's schedule. If there are events during the given DateTime,
   * then the user is available and returns true, otherwise the user is busy, and returns false.
   *
   * @param date the DateTime of which we are checking for events
   * @return true for available, false for busy
   */

  @Override
  public boolean showStatus(LocalDateTime date) {
    if (dateMap.containsKey(date.toLocalDate())) {
      List<EventInterface> events = dateMap.get(date.toLocalDate());
      for (EventInterface event : events) {
        if (!event.getStartDateTime().isAfter(date) && !event.getEndDateTime().isBefore(date)) {
          return true;
        }
      }
    }
    return false;

  }


  private void checkStartEnd(LocalDateTime start, LocalDateTime end) {
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException("An event in a series must occur on one day.");
    }
  }

  private DayOfWeek[] getDaysOfWeek(String weekdays) {
    String[] weekdaysCharArray = weekdays.split("");
    DayOfWeek[] weekdaysArray = new DayOfWeek[weekdaysCharArray.length];
    for (int i = 0; i < weekdaysCharArray.length; i++) {
      weekdaysArray[i] = daysOfWeek.get(weekdaysCharArray[i]);
      if (weekdaysArray[i] == null) {
        throw new IllegalArgumentException("Invalid weekday character: " + weekdaysCharArray[i]);
      }
    }
    return weekdaysArray;
  }

  private void validateSeries(List<EventInterface> events) {
    for (EventInterface event : events) {
      validateEvent(event);
    }
    for (EventInterface tempEvent : events) {
      addEvent(tempEvent, tempEvent.getStartDateTime().toLocalDate());
    }
    seriesMap.put(currentSeriesId, events);
    currentSeriesId = String.valueOf(Integer.parseInt(currentSeriesId) + 1);
  }

  private void addEvent(EventInterface event, LocalDate date) {
    if (dateMap.containsKey(date)) {
      if (!validateEvent(event)) {
        throw new IllegalArgumentException("Event already exists.");
      }
      List<EventInterface> currentDate = dateMap.get(date);
      currentDate.add(event);
    } else {
      dateMap.put(date, new ArrayList<>());
      List<EventInterface> currentDate = dateMap.get(date);
      currentDate.add(event);
    }
  }

  private boolean validateEvent(EventInterface event) {
    List<EventInterface> currentDate = dateMap.get(event.getStartDateTime().toLocalDate());
    if (currentDate == null) {
      return true;
    }
    for (EventInterface currentEvent : currentDate) {
      if (event.equals(currentEvent)) {
        return false;
      }
    }
    return true;
  }

  private void setSubject(String subject, List<EventInterface> events) {
    for (EventInterface event : events) {
      EventInterface editedEvent = new Event(subject, event.getStartDateTime(),
          event.getEndDateTime(), event.getLocation(), event.getDescription(),
          event.getStatus(), event.getSeriesId());
      if (!validateEvent(editedEvent)) {
        throw new IllegalArgumentException("Edit would cause duplicate events.");
      }
    }
    for (EventInterface event : events) {
      event.setSubject(subject);
    }
  }

  private void setStart(String start, List<EventInterface> events) {
    for (EventInterface event : events) {
      EventInterface editedEvent = new Event(event.getSubject(), LocalDateTime.parse(start),
          event.getEndDateTime(), event.getLocation(), event.getDescription(),
          event.getStatus(), event.getSeriesId());
      if (!validateEvent(editedEvent)) {
        throw new IllegalArgumentException("Edit would cause duplicate events.");
      }
    }
    for (EventInterface event : events) {
      event.setStartDateTime(LocalDateTime.parse(start));
    }
  }

  private void setEnd(String end, List<EventInterface> events) {
    for (EventInterface event : events) {
      EventInterface editedEvent = new Event(event.getSubject(), event.getStartDateTime(),
          LocalDateTime.parse(end), event.getLocation(), event.getDescription(),
          event.getStatus(), event.getSeriesId());
      if (!validateEvent(editedEvent)) {
        throw new IllegalArgumentException("Edit would cause duplicate events.");
      }
    }
    for (EventInterface event : events) {
      event.setEndDateTime(LocalDateTime.parse(end));
    }
  }

  private void setDescription(String description, List<EventInterface> events) {
    for (EventInterface event : events) {
      event.setDescription(description);
    }
  }

  private void setLocation(String location, List<EventInterface> events) {
    for (EventInterface event : events) {
      event.setLocation(location);
    }
  }

  private void setStatus(String status, List<EventInterface> events) {
    for (EventInterface event : events) {
      if (Objects.equals(status, "public")) {
        event.setStatus(EventStatus.PUBLIC);
      } else if (Objects.equals(status, "private")) {
        event.setStatus(EventStatus.PRIVATE);
      } else {
        throw new IllegalArgumentException("Invalid event status");
      }
    }
  }

  private boolean eventsOverlap(LocalDateTime start, LocalDateTime end,
                                LocalDateTime startInterval, LocalDateTime endInterval) {
    return !start.isAfter(endInterval) && !end.isBefore(startInterval);
  }

  private String formatTimeForGoogle(java.time.LocalTime time) {
    int hour = time.getHour();
    int minute = time.getMinute();
    String amPm = hour >= 12 ? "PM" : "AM";
    if (hour == 0) {
      hour = 12;
    } else if (hour > 12) {
      hour = hour - 12;
    }
    return String.format("%d:%02d %s", hour, minute, amPm);
  }

}
