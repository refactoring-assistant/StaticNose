package calendar.controller.handlers;

import calendar.controller.commanddata.CreateCommandData;
import calendar.model.Event;
import calendar.model.datatypes.TypeOfEvent;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handler class that performs the logic for creating events.
 * Takes parsed command data and interacts with the model.
 */
public class CreateEventHandler {

  private final CalendarEditable calendarModel;
  private final LocalTime allDayStart;
  private final LocalTime allDayEnd;

  /**
   * Constructor for CreateEventHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public CreateEventHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
    this.allDayStart = LocalTime.of(8, 0);
    this.allDayEnd = LocalTime.of(17, 0);
  }

  /**
   * Executes the create event logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(CreateCommandData data) {
    if (data.isRepeating()) {
      return handleRepetition(data);
    } else {
      return handleSingleEventCreation(data);
    }
  }

  private String handleSingleEventCreation(CreateCommandData data) {
    LocalDateTime startDateTime = data.getStartDateTime();
    LocalDateTime endDateTime = data.getEndDateTime();

    if (endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("Error: End is before start");
    }
    if (isSingleDayEvent(startDateTime, endDateTime)) {
      return createAndAddEvent(data.getSubject(), startDateTime, endDateTime);
    }

    return createMultiDayEvents(data.getSubject(), startDateTime, endDateTime);
  }

  private boolean isSingleDayEvent(LocalDateTime start, LocalDateTime end) {
    return start.toLocalDate().equals(end.toLocalDate());
  }

  private String createAndAddEvent(String subject,
                                   LocalDateTime start, LocalDateTime end) {
    boolean allDay = isAllDay(start, end);
    EventReadOnly event = new Event.EventBuilder(subject, start)
        .setEndDateTime(end)
        .setAllDay(allDay)
        .build();
    return formatEventResult(calendarModel.addEvent(event));
  }

  private String createMultiDayEvents(String subject,
                                      LocalDateTime start, LocalDateTime end) {
    StringBuilder result = new StringBuilder();
    LocalDate currentDate = start.toLocalDate();

    while (!currentDate.isAfter(end.toLocalDate())) {
      LocalDateTime dayStart = getDayStart(currentDate, start);
      LocalDateTime dayEnd = getDayEnd(currentDate, end);

      result.append(createAndAddEvent(subject, dayStart, dayEnd))
          .append(System.lineSeparator());
      currentDate = currentDate.plusDays(1);
    }

    return result.toString();
  }

  private LocalDateTime getDayStart(LocalDate date, LocalDateTime start) {
    if (date.equals(start.toLocalDate())) {
      return start;
    }
    return LocalDateTime.of(date, LocalTime.MIDNIGHT);
  }

  private LocalDateTime getDayEnd(LocalDate date, LocalDateTime end) {
    if (date.equals(end.toLocalDate())) {
      return end;
    }
    return LocalDateTime.of(date, LocalTime.of(23, 59));
  }

  private String formatEventResult(EventReadOnly event) {
    return "Created Event: " + System.lineSeparator() + event.toString();
  }

  private String handleRepetition(CreateCommandData data) {
    String repeatType = data.getRepeatType();
    String repeatValue = data.getRepeatValue();

    switch (repeatType) {
      case "for":
        int n = Integer.parseInt(repeatValue);
        return handleTimesRepetitions(data, n);
      case "until":
        LocalDateTime repeatUntil = LocalDateTime.parse(repeatValue + "T23:59",
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        return handleRepetitionsUntilDate(data, repeatUntil);
      default:
        throw new IllegalArgumentException("Error: Invalid repetition command: " + repeatType);
    }
  }

  private String handleRepetitionsUntilDate(CreateCommandData data,
                                            LocalDateTime repeatUntil) {
    LocalDateTime startDateTime = data.getStartDateTime();
    LocalDateTime endDateTime = data.getEndDateTime();

    if (repeatUntil.isBefore(startDateTime)) {
      throw new IllegalArgumentException("Error: Repeat until date cannot be before start date");
    }

    List<DayOfWeek> repeatDays = populateWeekDays(data.getDaysString());
    UUID uuid = UUID.randomUUID();
    List<EventReadOnly> planned = new ArrayList<>();
    boolean allDay = isAllDay(startDateTime, endDateTime);
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = repeatUntil.toLocalDate();
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(endDate)) {
      DayOfWeek currentDay = currentDate.getDayOfWeek();
      if (repeatDays.contains(currentDay)) {
        LocalDateTime currentStartDateTime = LocalDateTime.of(currentDate,
            startDateTime.toLocalTime());
        LocalDateTime currentEndDateTime = LocalDateTime.of(currentDate,
            endDateTime.toLocalTime());
        EventReadOnly newEvent = buildRepeatingEvent(data.getSubject(), currentStartDateTime,
            currentEndDateTime, uuid, allDay);
        checkConflict(newEvent);
        planned.add(newEvent);
      }
      currentDate = currentDate.plusDays(1);
    }

    return buildSeriesOutput(planned);
  }

  private String handleTimesRepetitions(CreateCommandData data, int n) {
    LocalDateTime startDateTime = data.getStartDateTime();
    LocalDateTime endDateTime = data.getEndDateTime();

    UUID uuid = UUID.randomUUID();
    List<DayOfWeek> repeatDays = populateWeekDays(data.getDaysString());
    List<EventReadOnly> planned = new ArrayList<>();

    LocalDateTime currentStart = startDateTime;
    LocalDateTime currentEnd = endDateTime;
    boolean allDay = isAllDay(startDateTime, endDateTime);

    if (n > 0 && repeatDays.contains(startDateTime.getDayOfWeek())) {
      EventReadOnly startEvent =
          buildRepeatingEvent(data.getSubject(), startDateTime, endDateTime, uuid, allDay);
      checkConflict(startEvent);
      planned.add(startEvent);
      n--;
    }

    while (n > 0) {
      currentStart = currentStart.plusDays(1);
      currentEnd = currentEnd.plusDays(1);

      if (repeatDays.contains(currentStart.getDayOfWeek())) {
        EventReadOnly nextEvent =
            buildRepeatingEvent(data.getSubject(), currentStart, currentEnd, uuid, allDay);
        checkConflict(nextEvent);
        planned.add(nextEvent);
        n--;
      }
    }
    if (planned.isEmpty()) {
      throw new IllegalArgumentException("Error: Invalid command.");
    }
    return buildSeriesOutput(planned);
  }

  private void checkConflict(EventReadOnly newEvent) {
    calendarModel.forEachEvent(event -> {
      if (newEvent.equals(event)) {
        throw new IllegalArgumentException(
            "Error: Cannot create series — conflict with existing event");
      }
    });
  }

  private EventReadOnly buildRepeatingEvent(String subject, LocalDateTime start, LocalDateTime end,
                                            UUID seriesId, boolean isAllDay) {
    return new Event.EventBuilder(subject, start)
        .setEndDateTime(end)
        .setEventType(TypeOfEvent.SERIES)
        .setEventId(seriesId)
        .setAllDay(isAllDay)
        .build();
  }

  private boolean isAllDay(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return startDateTime.toLocalDate().equals(endDateTime.toLocalDate())
        && startDateTime.toLocalTime().equals(allDayStart)
        && endDateTime.toLocalTime().equals(allDayEnd);
  }

  private String buildSeriesOutput(List<EventReadOnly> plannedEvents) {
    StringBuilder output = new StringBuilder("Created a Series Event: " + System.lineSeparator());
    for (EventReadOnly e : plannedEvents) {
      output.append(calendarModel.addEvent(e).toString()).append(System.lineSeparator());
    }

    return output.toString();
  }

  private List<DayOfWeek> populateWeekDays(String daysString) {
    List<DayOfWeek> days = new ArrayList<>();
    for (char c : daysString.toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Error: Invalid day of week: " + c);
      }
    }
    return days;
  }
}

