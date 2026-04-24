package calendar.model;

import calendar.enums.UserStatus;
import calendar.event.Event;
import calendar.event.EventSeries;
import calendar.util.CsvExporter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
  public Event createSingleEvent(String subject, String startDateTime, String endDateTime,
                                 String description, String location, String eventStatus,
                                 String seriesId)
      throws IllegalArgumentException {
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
  public EventSeries createEventSeries(String subject, String startDateTime, String endDateTime,
                                       String description, String location, String eventStatus,
                                       String weekdays, int repeatTimes, String seriesEndDateTime)
      throws IllegalArgumentException {
    if (subject == null || subject.isEmpty() || startDateTime == null  || startDateTime.isEmpty()) {
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
                               String newSubject, String newStartDateTime, String newEndDateTime,
                               String newDescription, String newLocation,
                               String newEventStatus) throws IllegalArgumentException {
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
    boolean isKeyChanged =
        (newSubject != null && !newSubject.isEmpty())
            || (newStartDateTime != null && !newStartDateTime.isEmpty())
            || (newEndDateTime != null && !newEndDateTime.isEmpty());

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
      if (calendar.get(newKey) != null) { // !newKey.equals(oldKey) && calendar.containsKey(newKey)
        throw new IllegalArgumentException(
            "Event on the new subject, start date/time, end date/time already exists");
      }
      Event newEvent =
          createSingleEvent(finalNewSubject, finalNewStart.toString(), finalNewEnd.toString(),
              newDescription,
              newLocation, newEventStatus, null);
      calendar.remove(oldKey);
      calendar.put(newKey, newEvent);

      return newEvent;
    }

  }

  @Override
  public EventSeries editEventSeries(String subject, String startDateTime, String endDateTime,
                                     String newSubject, String newStartDateTime,
                                     String newEndDateTime,
                                     String newDescription, String newLocation,
                                     String newEventStatus) throws IllegalArgumentException {
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
    if (endDateTime != null && !endDateTime.isEmpty()) {
      try {
        oldEnd = LocalDateTime.parse(endDateTime);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time: " + endDateTime);
      }
    } else {
      endDateTime = startDateTime.substring(0, 10) + "T17:00";
      oldEnd = LocalDateTime.parse(endDateTime);
    }

    EventKey oldKey = new EventKey(subject, oldStart, oldEnd);
    Event oldEvent = calendar.get(oldKey);
    if (oldEvent == null) {
      throw new IllegalArgumentException("Event does not exist, subject: "
          + subject + ", start: " + startDateTime + ", end: " + endDateTime);
    }
    boolean isKeyChanged =
        (newSubject != null && !newSubject.isEmpty())
            || (newStartDateTime != null && !newStartDateTime.isEmpty())
            || (newEndDateTime != null && !newEndDateTime.isEmpty());
    String seriesId = oldEvent.getSeriesId();
    // This event does not belong to any series
    if (seriesId == null) {
      editSingleEvent(subject, startDateTime, endDateTime, newSubject, newStartDateTime,
          newEndDateTime,
          newDescription, newLocation, newEventStatus);
    } else {
      EventSeries series = seriesManager.get(seriesId);
      if (series == null) {
        throw new IllegalStateException("The series with subject: " + subject
            + ", start: " + startDateTime + ", end: " + endDateTime + "does not exit");
      } else {
        if (!isKeyChanged) {
          for (EventKey eventKey : series.getEventKeys()) {
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

          series = seriesManager.get(seriesId);
          for (EventKey keyToDel : series.getEventKeys()) {
            calendar.remove(keyToDel);
          }
          if (series != null) {
            series.removeEventKey(oldKey);
          }

          String finalNewSubject =
              (newSubject != null && !newSubject.isEmpty()) ? newSubject : oldEvent.getSubject();
          LocalDateTime finalNewStart;
          if (newStartDateTime != null && !newStartDateTime.isEmpty()) {
            try {
              finalNewStart = LocalDateTime.parse(newStartDateTime);
            } catch (DateTimeParseException e) {
              throw new IllegalArgumentException(
                  "Invalid new start date/time: " + newStartDateTime);
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
          EventKey newKey = new EventKey(finalNewSubject, finalNewStart, finalNewEnd);
          String newSeriesId = series.getSeriesId();
          newDescription = series.getDescription();
          newLocation = series.getLocation();
          newEventStatus = series.getEventStatus().toString();
          String newWeekdays = series.getWeekdays();
          int newRepeatTimes =
              (series.getOccurrences() == null ? 0 : series.getOccurrences().intValue());
          String newSeriesEndDate =
              series.getEndDate() == null ? "" : series.getEndDate().toString();
          EventSeries newSeries = createEventSeries(finalNewSubject,
              finalNewStart.toString(), finalNewEnd.toString(), newDescription, newLocation,
              newEventStatus,
              newWeekdays, newRepeatTimes, newSeriesEndDate);
          return newSeries;
        }
      }

    }


    return null;
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
}
