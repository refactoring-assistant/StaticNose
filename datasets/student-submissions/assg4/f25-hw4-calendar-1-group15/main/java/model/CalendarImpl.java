package model;

import static model.CalendarConstants.ALL_DAY_END;
import static model.CalendarConstants.ALL_DAY_START;
import static model.CalendarConstants.CSV_DATE_FORMATTER;
import static model.CalendarConstants.CSV_HEADER;
import static model.CalendarConstants.CSV_TIME_FORMATTER;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the ICalendar interface providing full calendar functionality.
 * All date/time values are interpreted as Eastern Standard Time (EST).
 * This class manages both single events and recurring event series with
 * comprehensive conflict detection and editing capabilities.
 */
public class CalendarImpl implements Icalendar {

  private final Map<LocalDate, List<SingleEvent>> singleEvents;
  private final Map<String, List<SeriesEvent>> seriesEvents;
  private final Set<EventSignature> eventSignatures;

  /**
   * Constructs an empty calendar.
   */
  public CalendarImpl() {
    this.singleEvents = new HashMap<>();
    this.seriesEvents = new HashMap<>();
    this.eventSignatures = new HashSet<>();
  }

  @Override
  public void addSingleEvent(SingleEvent event) {
    EventSignature signature = new EventSignature(
        event.getSubject(),
        event.getStartDateTime(),
        event.getEndDateTime()
    );
    validateNoConflict(signature);
    LocalDate date = event.getStartDateTime().toLocalDate();
    singleEvents.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
    eventSignatures.add(signature);
  }

  @Override
  public void addEventSeries(EventSeriesManager series) {
    String seriesId = series.getSeriesId();
    List<SeriesEvent> events = series.generateAllEvents();
    Set<EventSignature> newSignatures = new HashSet<>();
    for (SeriesEvent event : events) {
      EventSignature signature = new EventSignature(
          event.getSubject(),
          event.getStartDateTime(),
          event.getEndDateTime()
      );

      if (eventSignatures.contains(signature) || newSignatures.contains(signature)) {
        throw new IllegalArgumentException(
            "Event conflicts with existing event at: " + event.getStartDateTime());
      }
      newSignatures.add(signature);
    }


    seriesEvents.put(seriesId, events);
    eventSignatures.addAll(newSignatures);
  }


  @Override
  public List<Ievent> getEventsOnDate(LocalDate date) {
    List<Ievent> result = new ArrayList<>();

    for (List<SingleEvent> eventList : singleEvents.values()) {
      for (SingleEvent event : eventList) {
        if (isEventOnDate(event, date)) {
          result.add(event);
        }
      }
    }

    for (List<SeriesEvent> seriesEventList : this.seriesEvents.values()) {
      for (SeriesEvent event : seriesEventList) {
        if (event.getStartDateTime().toLocalDate().equals(date)) {
          result.add(event);
        }
      }
    }

    return result;
  }

  @Override
  public List<Ievent> getEventsInRange(LocalDate start, LocalDate end) {
    List<Ievent> result = new ArrayList<>();

    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      List<SingleEvent> dayEvents = singleEvents.get(date);
      if (dayEvents != null) {
        for (SingleEvent event : dayEvents) {
          LocalDate eventStart = event.getStartDateTime().toLocalDate();
          LocalDate eventEnd = event.getEndDateTime().toLocalDate();
          if (!eventEnd.isBefore(start) && !eventStart.isAfter(end)) {
            result.add(event);
          }
        }
      }
    }

    for (List<SeriesEvent> series : seriesEvents.values()) {
      for (SeriesEvent event : series) {
        LocalDate eventDate = event.getStartDateTime().toLocalDate();
        if (!eventDate.isBefore(start) && !eventDate.isAfter(end)) {
          result.add(event);
        }
      }
    }
    return result;
  }

  @Override
  public void editEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                        Map<String, Object> changes) {
    Ievent uniqueEvent = findUniqueEvent(subject, startDateTime);

    if (endDateTime != null && !uniqueEvent.getEndDateTime().equals(endDateTime)) {
      throw new IllegalArgumentException("No event found with specified criteria");
    }


    if (uniqueEvent.isPartOfSeries()) {
      editSeriesEvent(subject, startDateTime, endDateTime, changes, EditScope.SINGLE);
      return;
    }

    SingleEvent abstractEvent = (SingleEvent) uniqueEvent;

    EventSignature oldSig = new EventSignature(
        abstractEvent.getSubject(),
        abstractEvent.getStartDateTime(),
        abstractEvent.getEndDateTime()
    );

    String newSubject = changes.containsKey("subject")
        ? (String) changes.get("subject") : abstractEvent.getSubject();
    LocalDateTime newStart = changes.containsKey("startDateTime")
        ? (LocalDateTime) changes.get("startDateTime") : abstractEvent.getStartDateTime();
    LocalDateTime newEnd = changes.containsKey("endDateTime")
        ? (LocalDateTime) changes.get("endDateTime") : abstractEvent.getEndDateTime();

    EventSignature newSig = new EventSignature(newSubject, newStart, newEnd);

    if (!oldSig.equals(newSig) && eventSignatures.contains(newSig)) {
      throw new IllegalArgumentException("Edit would create a conflict with existing event");
    }

    LocalDateTime finalStart = changes.containsKey("startDateTime")
        ? (LocalDateTime) changes.get("startDateTime") : abstractEvent.getStartDateTime();
    LocalDateTime finalEnd = changes.containsKey("endDateTime")
        ? (LocalDateTime) changes.get("endDateTime") : abstractEvent.getEndDateTime();

    if (!finalEnd.isAfter(finalStart)) {
      throw new IllegalArgumentException(
          "Edit would create invalid event: end time before start time");
    }
    LocalDate oldDate = abstractEvent.getStartDateTime().toLocalDate();
    applyChangesToEvent(abstractEvent, changes);
    LocalDate newDate = abstractEvent.getStartDateTime().toLocalDate();

    if (!oldDate.equals(newDate)) {
      List<SingleEvent> oldDateEvents = singleEvents.get(oldDate);
      if (oldDateEvents != null) {
        oldDateEvents.remove(abstractEvent);
      }
      List<SingleEvent> newDateEvents =
          singleEvents.computeIfAbsent(newDate, k -> new ArrayList<>());
      newDateEvents.add(abstractEvent);
    }

    eventSignatures.remove(oldSig);
    eventSignatures.add(newSig);
  }

  /**
   * Finds a unique event by its subject and start time.
   *
   * @param subject       the event subject
   * @param startDateTime the event start time
   * @return the matching event
   * @throws IllegalArgumentException if no event or multiple events match
   */
  private Ievent findUniqueEvent(String subject, LocalDateTime startDateTime) {
    List<Ievent> matches = new ArrayList<>();

    LocalDate date = startDateTime.toLocalDate();
    List<SingleEvent> dayEvents = singleEvents.get(date);
    if (dayEvents != null) {
      for (SingleEvent event : dayEvents) {
        if (event.getSubject().equals(subject)
            && event.getStartDateTime().equals(startDateTime)) {
          matches.add(event);
        }
      }
    }

    for (List<SeriesEvent> series : seriesEvents.values()) {
      for (SeriesEvent event : series) {
        if (event.getSubject().equals(subject)
            && event.getStartDateTime().equals(startDateTime)) {
          matches.add(event);
        }
      }
    }

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with specified criteria");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events match the criteria. Please be more specific.");
    }

    return matches.get(0);
  }

  /**
   * Applies a set of changes to an event, handling the order of operations
   * to prevent validation errors when changing both start and end times.
   *
   * @param event   the event to modify
   * @param changes a map containing property names as keys and new values
   */
  private void applyChangesToEvent(AbstractEvent event, Map<String, Object> changes) {
    boolean changingBoth =
        changes.containsKey("startDateTime") && changes.containsKey("endDateTime");

    if (changingBoth) {
      LocalDateTime newStart = (LocalDateTime) changes.get("startDateTime");
      LocalDateTime newEnd = (LocalDateTime) changes.get("endDateTime");

      if (newStart.isAfter(event.getStartDateTime())) {
        event.setEndDateTime(newEnd);
        event.setStartDateTime(newStart);
      } else {
        event.setStartDateTime(newStart);
        event.setEndDateTime(newEnd);
      }
    } else {
      if (changes.containsKey("startDateTime")) {
        event.setStartDateTime((LocalDateTime) changes.get("startDateTime"));
      }
      if (changes.containsKey("endDateTime")) {
        event.setEndDateTime((LocalDateTime) changes.get("endDateTime"));
      }
    }

    if (changes.containsKey("subject")) {
      event.setSubject((String) changes.get("subject"));
    }
    if (changes.containsKey("description")) {
      event.setDescription((String) changes.get("description"));
    }
    if (changes.containsKey("location")) {
      event.setLocation((String) changes.get("location"));
    }
    if (changes.containsKey("isPublic")) {
      event.setIsPublic((boolean) changes.get("isPublic"));
    }
  }

  @Override
  public void editSeriesEvent(String subject, LocalDateTime startTime, LocalDateTime endDateTime,
                              Map<String, Object> changes,
                              EditScope scope) {
    String targetSeriesId = null;
    SeriesEvent targetEvent = null;

    for (Map.Entry<String, List<SeriesEvent>> entry : seriesEvents.entrySet()) {
      for (SeriesEvent event : entry.getValue()) {
        if (event.getSubject().equals(subject)
            && event.getStartDateTime().equals(startTime)) {
          targetSeriesId = entry.getKey();
          targetEvent = event;
          break;
        }
      }
      if (targetSeriesId != null) {
        break;
      }
    }

    if (targetSeriesId == null) {
      throw new IllegalArgumentException("Event not found in any series");
    }

    if (endDateTime != null && !targetEvent.getEndDateTime().equals(endDateTime)) {
      throw new IllegalArgumentException("No event found with specified criteria");
    }

    List<SeriesEvent> seriesEventList = seriesEvents.get(targetSeriesId);

    boolean isTimeChange =
        changes.containsKey("startDateTime") || changes.containsKey("endDateTime");

    switch (scope) {
      case SINGLE:
        if (isTimeChange) {
          convertToSingleEvent(targetEvent);
          editEvent(subject, startTime, endDateTime, changes);
        } else {
          for (SeriesEvent event : seriesEventList) {
            if (event.getStartDateTime().equals(startTime)) {
              EventSignature oldSig = new EventSignature(event);

              String newSubject = changes.containsKey("subject")
                  ? (String) changes.get("subject") : event.getSubject();
              LocalDateTime newStart = event.getStartDateTime();
              LocalDateTime newEnd = event.getEndDateTime();

              EventSignature newSig = new EventSignature(newSubject, newStart, newEnd);

              if (!oldSig.equals(newSig) && eventSignatures.contains(newSig)) {
                throw new IllegalArgumentException(
                    "Edit would create a conflict with existing event");
              }

              applyChangesToEvent(event, changes);

              eventSignatures.remove(oldSig);
              eventSignatures.add(newSig);
              break;
            }
          }
        }
        break;

      case FROM_THIS:
        if (isTimeChange) {
          splitSeriesFromPoint(targetSeriesId, startTime, changes);
        } else {
          for (SeriesEvent event : seriesEventList) {
            if (!event.getStartDateTime().isBefore(startTime)) {
              EventSignature oldSig = new EventSignature(event);
              applyChangesToEvent(event, changes);
              eventSignatures.remove(oldSig);
              eventSignatures.add(new EventSignature(event));
            }
          }
        }
        break;

      case ALL_IN_SERIES:
        if (isTimeChange) {
          String newSeriesId = UUID.randomUUID().toString();
          List<SeriesEvent> newSeriesList = new ArrayList<>();

          for (SeriesEvent event : seriesEventList) {
            eventSignatures.remove(new EventSignature(event));

            Map<String, Object> adjustedChanges = adjustChangesForSeriesEvent(event, changes);
            applyChangesToEvent(event, adjustedChanges);

            SeriesEvent newEvent = new SeriesEvent(
                event.getSubject(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getDescription(),
                event.getLocation(),
                event.isPublic(),
                newSeriesId
            );

            newSeriesList.add(newEvent);
            eventSignatures.add(new EventSignature(newEvent));
          }

          seriesEvents.remove(targetSeriesId);
          seriesEvents.put(newSeriesId, newSeriesList);

        } else {
          for (SeriesEvent event : seriesEventList) {
            if (event.getSubject().equals(subject)) {
              EventSignature oldSig = new EventSignature(event);

              String newSubject = changes.containsKey("subject")
                  ? (String) changes.get("subject") : event.getSubject();
              EventSignature newSig = new EventSignature(newSubject,
                  event.getStartDateTime(), event.getEndDateTime());

              if (!oldSig.equals(newSig) && eventSignatures.contains(newSig)) {
                throw new IllegalArgumentException("Edit would create conflict");
              }

              applyChangesToEvent(event, changes);

              eventSignatures.remove(oldSig);
              eventSignatures.add(new EventSignature(event));
            }
          }
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid scope " + scope);
    }
  }

  /**
   * Splits a series at a specified point, applying changes to events from that point forward.
   * This method is used when a time change is applied with FROM_THIS scope, creating
   * two separate series: one with the original events before the split point, and
   * another with the modified events from the split point onwards.
   *
   * @param seriesId   the ID of the series to split
   * @param splitPoint the date/time at which to split the series
   * @param changes    the changes to apply to events from the split point forward
   */
  private void splitSeriesFromPoint(String seriesId, LocalDateTime splitPoint,
                                    Map<String, Object> changes) {
    List<SeriesEvent> originalSeries = seriesEvents.get(seriesId);
    List<SeriesEvent> beforeSplit = new ArrayList<>();
    List<SeriesEvent> afterSplitNew = new ArrayList<>();


    String newSeriesId = UUID.randomUUID().toString();

    for (SeriesEvent event : originalSeries) {
      if (event.getStartDateTime().isBefore(splitPoint)) {
        beforeSplit.add(event);
      } else {
        eventSignatures.remove(new EventSignature(event));


        Map<String, Object> adjustedChanges = adjustChangesForSeriesEvent(event, changes);

        LocalDateTime newStart = adjustedChanges.containsKey("startDateTime")
            ? (LocalDateTime) adjustedChanges.get("startDateTime") : event.getStartDateTime();
        LocalDateTime newEnd = adjustedChanges.containsKey("endDateTime")
            ? (LocalDateTime) adjustedChanges.get("endDateTime") : event.getEndDateTime();

        String newSubject = changes.containsKey("subject")
            ? (String) changes.get("subject") : event.getSubject();
        String newDescription = changes.containsKey("description")
            ? (String) changes.get("description") : event.getDescription();
        String newLocation = changes.containsKey("location")
            ? (String) changes.get("location") : event.getLocation();
        boolean newIsPublic = changes.containsKey("isPublic")
            ? (boolean) changes.get("isPublic") : event.isPublic();

        SeriesEvent newEvent = new SeriesEvent(
            newSubject,
            newStart,
            newEnd,
            newDescription,
            newLocation,
            newIsPublic,
            newSeriesId
        );

        afterSplitNew.add(newEvent);
        eventSignatures.add(new EventSignature(newEvent));
      }
    }

    seriesEvents.remove(seriesId);

    if (!beforeSplit.isEmpty()) {
      seriesEvents.put(seriesId, beforeSplit);
    }
    if (!afterSplitNew.isEmpty()) {
      seriesEvents.put(newSeriesId, afterSplitNew);
    }
  }

  /**
   * Adjusts time-related changes for a series event to maintain the event's
   * original date while applying new times. This is necessary because series
   * events on different dates need to maintain their dates while adopting
   * the new time from the change request.
   *
   * @param event   the series event for which to adjust changes
   * @param changes the original changes requested
   * @return a new map with adjusted time changes that preserve the event's date
   */
  private Map<String, Object> adjustChangesForSeriesEvent(SeriesEvent event,
                                                          Map<String, Object> changes) {
    Map<String, Object> adjusted = new HashMap<>(changes);

    if (changes.containsKey("startDateTime")) {
      LocalDateTime newStart = (LocalDateTime) changes.get("startDateTime");
      LocalTime newTime = newStart.toLocalTime();
      LocalDate eventDate = event.getStartDateTime().toLocalDate();

      adjusted.put("startDateTime", LocalDateTime.of(eventDate, newTime));
    }

    if (changes.containsKey("endDateTime")) {
      LocalDateTime newEnd = (LocalDateTime) changes.get("endDateTime");
      LocalTime newTime = newEnd.toLocalTime();
      LocalDate eventEndDate = event.getEndDateTime() != null
          ? event.getEndDateTime().toLocalDate()
          : event.getStartDateTime().toLocalDate();

      adjusted.put("endDateTime", LocalDateTime.of(eventEndDate, newTime));
    }

    return adjusted;
  }


  @Override
  public boolean isBusy(LocalDateTime dateTime) {

    LocalDate date = dateTime.toLocalDate();
    for (List<SingleEvent> eventList : singleEvents.values()) {
      for (SingleEvent event : eventList) {
        if (isEventOnDate(event, date) && isDateTimeWithinEvent(event, dateTime)) {
          return true;
        }
      }
    }

    for (List<SeriesEvent> series : seriesEvents.values()) {
      for (SeriesEvent event : series) {
        if (event.getStartDateTime().toLocalDate().equals(date)
            && isDateTimeWithinEvent(event, dateTime)) {
          return true;
        }
      }
    }
    return false;
  }


  @Override
  public void exportToCsv(String filename) {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write(CSV_HEADER);

      for (Ievent event : getAllEvents()) {
        String subject = event.getSubject();
        String description = event.getDescription() != null ? event.getDescription() : "";
        String location = event.getLocation() != null ? event.getLocation() : "";

        boolean isAllDay = isAllDayEvent(event);

        String startDate = formatDate(event.getStartDateTime());
        String startTime = isAllDay ? "" : formatTime(event.getStartDateTime());
        String endDate = formatDate(event.getEndDateTime());
        String endTime = isAllDay ? "" : formatTime(event.getEndDateTime());

        writer.write(String.format("\"%s\",%s,%s,%s,%s,%s,\"%s\",\"%s\"\n",
            subject,
            startDate,
            startTime,
            endDate,
            endTime,
            isAllDay ? "True" : "False",
            description,
            location));
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing to CSV", e);
    }
  }

  /**
   * Formats a date/time value for CSV export in Google Calendar format.
   *
   * @param dateTime the date/time to format
   * @return a string in MM/dd/yyyy format
   */
  private String formatDate(LocalDateTime dateTime) {
    return dateTime.format(CSV_DATE_FORMATTER);
  }

  /**
   * Formats a time value for CSV export in Google Calendar format.
   *
   * @param dateTime the date/time from which to extract and format the time
   * @return a string in hh:mm a format (12-hour format with AM/PM)
   */
  private String formatTime(LocalDateTime dateTime) {
    return dateTime.format(CSV_TIME_FORMATTER);
  }

  /**
   * Determines whether an event represents an all-day event based on its
   * start and end times. An event is considered all-day if it either
   * spans from 8:00 AM to 5:00 PM (the default pattern used by this system)
   * or from midnight to 11:59 PM.
   *
   * @param event the event to check
   * @return true if the event is an all-day event, false otherwise
   */
  private boolean isAllDayEvent(Ievent event) {
    LocalDateTime start = event.getStartDateTime();
    LocalDateTime end = event.getEndDateTime();

    return (start.toLocalTime().equals(ALL_DAY_START)
        && end.toLocalTime().equals(ALL_DAY_END))
        || (start.getHour() == 0 && start.getMinute() == 0
        && end.getHour() == 23 && end.getMinute() == 59);
  }

  /**
   * Converts a series event to a single event, removing it from its series.
   *
   * @param seriesEvent the series event to convert
   * @throws IllegalArgumentException if the conversion would create a conflict
   */
  private void convertToSingleEvent(SeriesEvent seriesEvent) {
    SingleEvent newSingle = SingleEvent.getBuilder()
        .subject(seriesEvent.getSubject())
        .startDateTime(seriesEvent.getStartDateTime())
        .endDateTime(seriesEvent.getEndDateTime())
        .description(seriesEvent.getDescription())
        .location(seriesEvent.getLocation())
        .isPublic(seriesEvent.isPublic())
        .build();

    EventSignature oldSignature = new EventSignature(seriesEvent);
    List<SeriesEvent> series = seriesEvents.get(seriesEvent.getSeriesId());
    series.remove(seriesEvent);
    eventSignatures.remove(oldSignature);
    EventSignature newSignature = new EventSignature(newSingle);
    LocalDate date = newSingle.getStartDateTime().toLocalDate();
    singleEvents.computeIfAbsent(date, k -> new ArrayList<>()).add(newSingle);
    eventSignatures.add(newSignature);
    if (series.isEmpty()) {
      seriesEvents.remove(seriesEvent.getSeriesId());
    }
  }

  @Override
  public String showStatus(LocalDateTime dateTime) {
    return isBusy(dateTime) ? "busy" : "available";
  }


  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events
   */
  private List<Ievent> getAllEvents() {
    List<Ievent> all = new ArrayList<>();
    singleEvents.values().forEach(all::addAll);
    seriesEvents.values().forEach(all::addAll);
    return all;
  }

  /**
   * Checks if an event spans or occurs on the given date.
   */
  private boolean isEventOnDate(Ievent event, LocalDate date) {
    LocalDate eventStart = event.getStartDateTime().toLocalDate();
    LocalDate eventEnd = event.getEndDateTime().toLocalDate();
    return !date.isBefore(eventStart) && !date.isAfter(eventEnd);
  }

  /**
   * Checks if a datetime falls within an event's time range.
   */
  private boolean isDateTimeWithinEvent(Ievent event, LocalDateTime dateTime) {
    return !dateTime.isBefore(event.getStartDateTime())
        && !dateTime.isAfter(event.getEndDateTime());
  }

  /**
   * Validates that an event signature doesn't conflict with existing events.
   */
  private void validateNoConflict(EventSignature signature) {
    if (eventSignatures.contains(signature)) {
      throw new IllegalArgumentException("Event conflicts with existing event");
    }
  }

}