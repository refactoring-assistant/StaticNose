package calendar.model;

import calendar.enums.UserStatus;
import calendar.event.Event;
import calendar.event.EventContext;
import calendar.event.EventSeries;
import calendar.util.CsvExporter;
import calendar.util.IcalExporter;
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
import java.util.Set;

/**
 * The implementation of CalendarInterface that creates and edit single event and event series.
 */
public class Calendar implements CalendarInterface {

  Map<EventKey, Event> calendar;
  Map<String, EventSeries> seriesManager;

  /**
   * Constructor for Calendar Class.
   */
  public Calendar() {
    calendar = new HashMap<>();
    seriesManager = new HashMap<>();
  }

  @Override
  public Event createSingleEvent(EventContext context, String seriesId)
      throws IllegalArgumentException {
    String subject = context.getSubject();
    String startDateTime = context.getStartDateTime();
    if (subject == null || subject.isEmpty() || startDateTime == null || startDateTime.isEmpty()) {
      throw new IllegalArgumentException("subject or startDateTime cannot be empty");
    }
    LocalDateTime start;
    if (startDateTime.length() <= 10) {
      startDateTime = startDateTime + "T08:00";
    }
    try {
      start = LocalDateTime.parse(startDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid start date/time: " + startDateTime);
    }
    LocalDateTime end = null;
    boolean isAllDay = false;
    String endDateTime = context.getEndDateTime();
    if (endDateTime == null || endDateTime.isEmpty()) {
      isAllDay = true;
      LocalDate date = start.toLocalDate();
      start = LocalDateTime.of(date, LocalTime.of(8, 0));
      end = LocalDateTime.of(date, LocalTime.of(17, 0));
    } else {
      try {
        end = LocalDateTime.parse(endDateTime);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time: " + endDateTime);
      }
    }
    String description = context.getDescription();
    String location = context.getLocation();
    String eventStatus = context.getEventStatus();
    Event event = new Event.EventBuilder(subject, start)
        .end(end)
        .description(description)
        .location(location)
        .status(eventStatus)
        .seriesId(seriesId)
        .build();
    EventKey key = new EventKey(subject, start, end);
    if (calendar.containsKey(key)) {
      throw new IllegalArgumentException("Event already exists");
    }
    calendar.put(key, event);
    return event;
  }

  @Override
  public EventSeries createEventSeries(EventContext context,
                                       String weekdays, int repeatTimes, String seriesEndDateTime)
      throws IllegalArgumentException {
    String subject = context.getSubject();
    String startDateTime = context.getStartDateTime();
    if (subject == null || subject.isEmpty() || startDateTime == null || startDateTime.isEmpty()) {
      throw new IllegalArgumentException("subject or startDateTime cannot be empty");
    }
    LocalDateTime start;
    if (startDateTime.length() <= 10) {
      startDateTime = startDateTime + "T08:00";
    }
    try {
      start = LocalDateTime.parse(startDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid start date/time: " + startDateTime);
    }
    LocalDateTime end = null;
    String endDateTime = context.getEndDateTime();
    if (endDateTime != null && !endDateTime.isEmpty()) {
      try {
        end = LocalDateTime.parse(endDateTime);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time: " + endDateTime);
      }
    } else {
      endDateTime = startDateTime.substring(0, 10) + "T17:00";
      end = LocalDateTime.parse(endDateTime);
    }

    LocalDate seriesEndDate = null;
    if (seriesEndDateTime != null && !seriesEndDateTime.isEmpty()) {
      try {
        seriesEndDate = LocalDate.parse(seriesEndDateTime);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid series end date/time: " + seriesEndDateTime);
      }
    }
    String description = context.getDescription();
    String location = context.getLocation();
    String eventStatus = context.getEventStatus();

    EventSeries.EventSeriesBuilder seriesBuilder = EventSeries.builder(subject, start, weekdays)
        .description(description)
        .location(location)
        .status(eventStatus);
    if (end != null) {
      seriesBuilder.end(end);
    }
    if (repeatTimes > 0) {
      seriesBuilder.occurrences(new Integer(repeatTimes));
    } else if (seriesEndDate != null) {
      seriesBuilder.setEndDate(seriesEndDate);
    } else {
      throw new IllegalArgumentException("Must specify either occurrences or end date");
    }

    EventSeries newSeries = seriesBuilder.build();
    String seriesId = newSeries.getSeriesId();
    Set<EventKey> keys = newSeries.getSeriesKeys();

    for (EventKey key : keys) {
      Event eventInstance = new Event.EventBuilder(key.getSubject(), key.getStartDateTime())
          .end(key.getEndDateTime())
          .description(description)
          .location(location)
          .status(eventStatus)
          .seriesId(seriesId)
          .build();
      if (calendar.containsKey(key)) {
        throw new IllegalArgumentException("Event already exists");
      }
      calendar.put(key, eventInstance);
    }
    if (seriesManager.containsKey(seriesId)) {
      throw new IllegalArgumentException("Event series already exists");
    }
    seriesManager.put(seriesId, newSeries); // put this series into Manager
    return newSeries;
  }

  @Override
  public Event getSingleEvent(String subject, String startDateTime, String endDateTime)
      throws IllegalArgumentException {
    if (subject == null || subject.isEmpty() || startDateTime == null || startDateTime.isEmpty()) {
      throw new IllegalArgumentException("subject or startDateTime cannot be empty");
    }
    LocalDateTime start;
    try {
      start = LocalDateTime.parse(startDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid start date/time: " + startDateTime);
    }
    LocalDateTime end = null;
    if (endDateTime != null && !endDateTime.isEmpty()) {
      try {
        end = LocalDateTime.parse(endDateTime);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time: " + endDateTime);
      }
    }
    EventKey key = new EventKey(subject, start, end);
    return calendar.get(key);
  }

  @Override
  public Event editSingleEvent(String subject, String startDateTime, String endDateTime,
                               EventContext context) throws IllegalArgumentException {
    if (subject == null || subject.isEmpty() || startDateTime == null || startDateTime.isEmpty()) {
      throw new IllegalArgumentException("subject or startDateTime cannot be empty");
    }
    LocalDateTime oldStart;
    try {
      oldStart = LocalDateTime.parse(startDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid start date/time: " + startDateTime);
    }


    LocalDateTime oldEnd = null;
    try {
      oldEnd = LocalDateTime.parse(endDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid end date/time: " + endDateTime);
    }

    EventKey oldKey = new EventKey(subject, oldStart, oldEnd);
    Event oldEvent = calendar.get(oldKey);
    // The old Event does not exist
    if (oldEvent == null) {
      throw new IllegalArgumentException("Event does not exist, subject: " + subject
          + ", start: " + startDateTime + ", end: " + endDateTime);
    }

    String newSubject = context.getSubject();
    String newStartDateTime = context.getStartDateTime();
    String newEndDateTime = context.getEndDateTime();
    boolean isKeyChanged =
        (newSubject != null && !newSubject.isEmpty())
            || (newStartDateTime != null && !newStartDateTime.isEmpty())
            || (newEndDateTime != null && !newEndDateTime.isEmpty());

    String newDescription = context.getDescription();
    String newLocation = context.getLocation();
    String newEventStatus = context.getEventStatus();
    if (!isKeyChanged) {
      if (newDescription != null && !newDescription.isEmpty()) {
        oldEvent.editDescription(newDescription);
      }
      if (newLocation != null && !newLocation.isEmpty()) {
        oldEvent.editLocation(newLocation);
      }
      if (newEventStatus != null && !newEventStatus.isEmpty()) {
        if (!newEventStatus.equals("private") && !newEventStatus.equals("public")) {
          throw new IllegalArgumentException("Invalid event status: " + newEventStatus);
        }
        oldEvent.editEventStatus(newEventStatus);
      }
      return oldEvent;

    } else {
      String finalNewSubject =
          (newSubject != null && !newSubject.isEmpty()) ? newSubject : oldEvent.getSubject();
      LocalDateTime finalNewStart;
      if (newStartDateTime != null && !newStartDateTime.isEmpty()) {
        try {
          finalNewStart = LocalDateTime.parse(newStartDateTime);
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException("Invalid new start date/time: " + newStartDateTime);
        }
      } else {
        finalNewStart = oldEvent.getStartDateTime();
      }
      LocalDateTime finalNewEnd;
      if (newEndDateTime != null && !newEndDateTime.isEmpty()) {
        try {
          finalNewEnd = LocalDateTime.parse(newEndDateTime);
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException("Invalid new end date/time: " + newEndDateTime);
        }
      } else {
        finalNewEnd = oldEvent.getEndDateTime();
      }
      // The new inputs clash with existing event, the edition should not occur
      EventKey newKey = new EventKey(finalNewSubject, finalNewStart, finalNewEnd);
      if (calendar.get(newKey) != null) {
        throw new IllegalArgumentException(
            "Event on the new subject, start date/time, end date/time already exists");
      }
      EventContext newContext =
          new EventContext(finalNewSubject, finalNewStart.toString(), finalNewEnd.toString(),
              newDescription, newLocation, newEventStatus);
      Event newEvent =
          createSingleEvent(newContext, null);
      calendar.remove(oldKey);
      calendar.put(newKey, newEvent);

      return newEvent;
    }

  }

  @Override
  public EventSeries editEventSeries(String subject, String startDateTime, String endDateTime,
                                     EventContext context) throws IllegalArgumentException {

    if (subject == null || subject.isEmpty() || startDateTime == null || startDateTime.isEmpty()) {
      throw new IllegalArgumentException("subject or startDateTime cannot be empty");
    }

    LocalDateTime oldStart;
    try {
      oldStart = LocalDateTime.parse(startDateTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid start date/time: " + startDateTime);
    }

    LocalDateTime oldEnd = null;
    boolean hasEnd = false;
    if (endDateTime != null && !endDateTime.isEmpty()) {
      try {
        oldEnd = LocalDateTime.parse(endDateTime);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time: " + endDateTime);
      }
      hasEnd = true;
    }
    if (!hasEnd) {
      for (Event event : this.getEvents()) {
        if (event.getSubject().equals(subject)
            && event.getStartDateTime().toString().equals(startDateTime)) {
          oldEnd = event.getEndDateTime();
          endDateTime = oldEnd.toString();
          break;
        }
      }
    }

    EventKey oldKey = new EventKey(subject, oldStart, oldEnd);
    Event oldEvent = calendar.get(oldKey);
    if (oldEvent == null) {
      throw new IllegalArgumentException("Event does not exist, subject: "
          + subject + ", start: " + startDateTime + ", end: " + endDateTime);
    }

    String newSubject = context.getSubject();
    String newStartDateTime = context.getStartDateTime();
    String newEndDateTime = context.getEndDateTime();
    boolean isKeyChanged =
        (newSubject != null && !newSubject.isEmpty())
            || (newStartDateTime != null && !newStartDateTime.isEmpty())
            || (newEndDateTime != null && !newEndDateTime.isEmpty());

    String seriesId = oldEvent.getSeriesId();

    String newDescription = context.getDescription();
    String newLocation = context.getLocation();
    String newEventStatus = context.getEventStatus();

    // This event does not belong to any series
    if (seriesId == null) {
      EventContext newContext =
          new EventContext(newSubject, newStartDateTime, newEndDateTime, newDescription,
              newLocation, newEventStatus);
      editSingleEvent(subject, startDateTime, endDateTime, newContext);
      return null;
    }

    EventSeries series = seriesManager.get(seriesId);
    if (series == null) {
      throw new IllegalStateException("The series with subject: " + subject
          + ", start: " + startDateTime + ", end: " + endDateTime + " does not exist");
    }
    if (!isKeyChanged) {
      for (EventKey eventKey : new HashSet<>(series.getEventKeys())) {
        Event eventInSeries = calendar.get(eventKey);
        if (eventInSeries != null) {
          if (newDescription != null && !newDescription.isEmpty()) {
            eventInSeries.editDescription(newDescription);
          }
          if (newLocation != null && !newLocation.isEmpty()) {
            eventInSeries.editLocation(newLocation);
          }
          if (newEventStatus != null && !newEventStatus.isEmpty()) {
            if (!newEventStatus.equals("private") && !newEventStatus.equals("public")) {
              throw new IllegalArgumentException("Invalid event series status: "
                  + newEventStatus);
            }
            eventInSeries.editEventStatus(newEventStatus);
          }
        }
      }
      if (newDescription != null && !newDescription.isEmpty()) {
        series.editDescription(newDescription);
      }
      if (newLocation != null && !newLocation.isEmpty()) {
        series.editLocation(newLocation);
      }
      if (newEventStatus != null && !newEventStatus.isEmpty()) {
        series.editEventStatus(newEventStatus);
      }
      return series;
    } else {
      Set<EventKey> keysToDelete = new HashSet<>();
      for (EventKey key : series.getEventKeys()) {
        if (!key.getStartDateTime().isBefore(oldStart)) {
          keysToDelete.add(key);
        }
      }
      for (EventKey keyToDel : keysToDelete) {
        calendar.remove(keyToDel);
      }

      for (EventKey keyToDel : keysToDelete) {
        series.removeEventKey(keyToDel);
      }

      LocalTime originalStartTime = series.getStartDateTime().toLocalTime();
      LocalTime originalEndTime = series.getEndDateTime().toLocalTime();
      LocalTime finalStartTime = originalStartTime;
      LocalTime finalEndTime = originalEndTime;
      if (newStartDateTime != null && !newStartDateTime.isEmpty()) {
        try {
          LocalDateTime newStart = LocalDateTime.parse(newStartDateTime);
          finalStartTime = newStart.toLocalTime();
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException(
              "Invalid new start date/time: " + newStartDateTime);
        }
      }

      if (newEndDateTime != null && !newEndDateTime.isEmpty()) {
        try {
          LocalDateTime newEnd = LocalDateTime.parse(newEndDateTime);
          finalEndTime = newEnd.toLocalTime();
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException("Invalid new end date/time: " + newEndDateTime);
        }
      }
      LocalDate newSeriesStartDate = oldStart.toLocalDate();
      LocalDateTime finalNewStart = LocalDateTime.of(newSeriesStartDate, finalStartTime);
      LocalDateTime finalNewEnd = LocalDateTime.of(newSeriesStartDate, finalEndTime);

      String finalDescription = (newDescription != null && !newDescription.isEmpty())
          ? newDescription : series.getDescription();
      String finalLocation = (newLocation != null && !newLocation.isEmpty())
          ? newLocation : series.getLocation();
      String finalEventStatus = (newEventStatus != null && !newEventStatus.isEmpty())
          ? newEventStatus : series.getEventStatus().toString();

      String newWeekdays = series.getWeekdays();

      int newRepeatTimes;
      if (series.getOccurrences() != null) {
        int totalOccurrences = series.getOccurrences();
        int passedOccurrences = keysToDelete.size();
        newRepeatTimes = totalOccurrences - (series.getSeriesKeys().size() - passedOccurrences);
        newRepeatTimes = totalOccurrences - series.getSeriesKeys().size();
      } else {
        newRepeatTimes = 0;
      }

      String finalNewSubject =
          (newSubject != null && !newSubject.isEmpty()) ? newSubject : oldEvent.getSubject();
      String newSeriesEndDate =
          series.getEndDate() == null ? "" : series.getEndDate().toString();

      EventContext newContext = new EventContext(
          finalNewSubject,
          finalNewStart.toString(),
          finalNewEnd.toString(),
          finalDescription,
          finalLocation,
          finalEventStatus
      );
      EventSeries newSeries = createEventSeries(
          newContext,
          newWeekdays,
          newRepeatTimes,
          newSeriesEndDate
      );
      return newSeries;
    }
  }


  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    List<Event> result = new ArrayList<>();
    for (Event event : calendar.values()) {
      LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
      LocalDate eventEndDate = event.getEndDateTime() != null
          ? event.getEndDateTime().toLocalDate() : eventStartDate;
      boolean isNotBeforeStart = !date.isBefore(eventStartDate);
      boolean isNotAfterEnd = !date.isAfter(eventEndDate);
      if (isNotBeforeStart && isNotAfterEnd) {
        result.add(event);
      }
    }
    result.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));
    return result;
  }

  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    List<Event> result = new ArrayList<>();
    for (Event event : calendar.values()) {
      LocalDateTime eventStart = event.getStartDateTime();
      LocalDateTime eventEnd = event.getEndDateTime();
      if (!(eventEnd.isBefore(start) || eventStart.isAfter(end))) {
        result.add(event);
      }
    }
    result.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));
    return result;
  }

  @Override
  public UserStatus getUserStatus(LocalDateTime queryTime) {
    for (Event event : calendar.values()) {
      LocalDateTime eventStart = event.getStartDateTime();
      LocalDateTime eventEnd = event.getEndDateTime();
      if (!queryTime.isBefore(eventStart) && !queryTime.isAfter(eventEnd)) {
        return UserStatus.BUSY;
      }
    }
    return UserStatus.AVAILABLE;
  }

  @Override
  public String exportToCsv() {
    List<Event> sortedEvents = new ArrayList<>(calendar.values());
    sortedEvents.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));
    return CsvExporter.exportToCsv(sortedEvents);
  }

  @Override
  public String exportToIcal(String calendarName, ZoneId timezone) {
    List<Event> sortedEvents = new ArrayList<>(calendar.values());
    sortedEvents.sort((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()));
    return IcalExporter.exportToIcal(sortedEvents, calendarName, timezone);
  }

  @Override
  public Collection<Event> getEvents() {
    return this.calendar.values();
  }

  @Override
  public EventSeries getEventSeries(String seriesId) {
    return this.seriesManager.get(seriesId);
  }

  @Override
  public void convertTimezone(ZoneId oldZone, ZoneId newZone) {
    if (oldZone.equals(newZone)) {
      return;
    }
    final Map<EventKey, Event> oldCalendar = new HashMap<>(this.calendar);
    final Map<String, EventSeries> oldSeriesManager = new HashMap<>(this.seriesManager);

    this.calendar.clear();
    this.seriesManager.clear();

    for (EventSeries oldSeries : oldSeriesManager.values()) {
      LocalDateTime oldStartDateTime = oldSeries.getStartDateTime();
      LocalDateTime oldEndDateTime = oldSeries.getEndDateTime();

      LocalDateTime newStartDateTime = oldStartDateTime
          .atZone(oldZone)
          .withZoneSameInstant(newZone)
          .toLocalDateTime();

      LocalDateTime newEndDateTime = oldEndDateTime
          .atZone(oldZone)
          .withZoneSameInstant(newZone)
          .toLocalDateTime();

      EventSeries.EventSeriesBuilder builder = EventSeries.builder(
              oldSeries.getSubject(),
              newStartDateTime,
              oldSeries.getWeekdays()
          )
          .description(oldSeries.getDescription())
          .location(oldSeries.getLocation())
          .status(oldSeries.getEventStatus().toString())
          .withExistingId(oldSeries.getSeriesId())
          .withExistingRemovedDates(oldSeries.getRemovedDates());

      if (newEndDateTime != null) {
        builder.end(newEndDateTime);
      }
      if (oldSeries.getOccurrences() != null) {
        builder.occurrences(oldSeries.getOccurrences());
      } else if (oldSeries.getEndDate() != null) {
        builder.setEndDate(oldSeries.getEndDate());
      }

      EventSeries newSeries = builder.build();
      String seriesId = newSeries.getSeriesId();
      Set<EventKey> newKeys = newSeries.getSeriesKeys();
      for (EventKey key : newKeys) {
        Event eventInstance = new Event.EventBuilder(key.getSubject(), key.getStartDateTime())
            .end(key.getEndDateTime())
            .description(oldSeries.getDescription())
            .location(oldSeries.getLocation())
            .status(oldSeries.getEventStatus().toString())
            .seriesId(seriesId)
            .build();
        this.calendar.put(key, eventInstance);
      }

      this.seriesManager.put(seriesId, newSeries);
    }

    for (Map.Entry<EventKey, Event> entry : oldCalendar.entrySet()) {
      Event oldEvent = entry.getValue();

      if (oldEvent.getSeriesId() != null) {
        continue;
      }
      LocalDateTime oldStartDateTime = oldEvent.getStartDateTime();
      LocalDateTime oldEndDateTime = oldEvent.getEndDateTime();

      LocalDateTime newStartDateTime = oldStartDateTime
          .atZone(oldZone)
          .withZoneSameInstant(newZone)
          .toLocalDateTime();

      LocalDateTime newEndDateTime = oldEndDateTime
          .atZone(oldZone)
          .withZoneSameInstant(newZone)
          .toLocalDateTime();

      Event newEvent = new Event.EventBuilder(oldEvent.getSubject(), newStartDateTime)
          .end(newEndDateTime)
          .description(oldEvent.getDescription())
          .location(oldEvent.getLocation())
          .status(oldEvent.getEventStatus().toString())
          .seriesId(null)
          .build();

      EventKey newKey = new EventKey(
          newEvent.getSubject(),
          newStartDateTime,
          newEndDateTime
      );
      this.calendar.put(newKey, newEvent);
    }
  }
}
