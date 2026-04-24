package calendar.command;

import calendar.exception.CalendarException;
import calendar.model.Weekday;
import calendar.service.InEventService;
import calendar.view.InCalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Handles the creation of calendar events - both one-time events and recurring series.
 * This command supports three types of event creation: Single events,
 *    Recurring series (count-based), Recurring series (date-based).
 * */
public class CreateEventCommand implements InCommand {

  private static final String MSG_EVENT_CREATED = "Event created: ";
  private static final String MSG_SERIES_CREATED = "Event series created: ";
  private static final String MSG_OCCURRENCES = " occurrences)";
  private static final String MSG_UNTIL = " (until ";
  private static final String MSG_CLOSING_PAREN = ")";

  private final InEventService eventService;
  private final InCalendarView view;
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final Set<Weekday> weekdays;
  private final Integer occurrenceCount;
  private final LocalDate endDate;
  private final Map<String, String> optionalProps;

  /**
   * Constructs a new command for event creation.
   * The constructor is private and hence we use factory methods to
   *    create instances for better type safety.
   *
   * @param eventService     handles the logic for creating events
   * @param view             displays success or error messages to the user
   * @param subject          what the event is about (e.g., "Team Meeting")
   * @param start            start when the event begins
   * @param end              when the event ends (can be null for all-day events)
   * @param weekdays         weekdays which days of the week to repeat on (null for single events)
   * @param occurrenceCount  how many times to repeat (null if not count based)
   * @param endDate          last date to generate events until (null if not date based)
   * @param optionalProps    additional details like description, location, status
   */
  private CreateEventCommand(InEventService eventService, InCalendarView view,
                             String subject, LocalDateTime start, LocalDateTime end,
                             Set<Weekday> weekdays, Integer occurrenceCount,
                             LocalDate endDate, Map<String, String> optionalProps) {
    validateRequiredFields(eventService, view, subject, start);
    this.eventService = eventService;
    this.view = view;
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.weekdays = weekdays;
    this.occurrenceCount = occurrenceCount;
    this.endDate = endDate;
    this.optionalProps = optionalProps;
  }

  /**
   * Creates a command for a one-time event (non-recurring).
   * For any meetings, appointments or any events that happen only once.
   *
   *
   * @param eventService manages event creation in the system
   * @param view         shows view to the user
   * @param subject      what this event is called
   * @param start        when it begins
   * @param end          when it ends (null for all-day events)
   * @param optionalProps extra details (description, location, status)
   * @return CreateEventCommand for single event
   */
  public static CreateEventCommand forSingleEvent(
      InEventService eventService, InCalendarView view,
      String subject, LocalDateTime start, LocalDateTime end,
      Map<String, String> optionalProps) {
    return new CreateEventCommand(eventService, view, subject, start, end,
        null, null, null, optionalProps);
  }

  /**
   * Creates a command for a recurring event series with a fixed number of occurrences.
   * When we know the amount of times an event should repeat, this type of creation is used.
   *
   * @param eventService manages event creation
   * @param view         displays results to user
   * @param subject      event title
   * @param start        first occurrence start time
   * @param end          first occurrence end time (null for all-day)
   * @param weekdays     weekdays to repeat on
   * @param count        how many total occurrences to create (must be positive)
   * @param optionalProps additional properties
   * @return CreateEventCommand for series with count
   */
  public static CreateEventCommand forSeriesWithCount(
      InEventService eventService, InCalendarView view,
      String subject, LocalDateTime start, LocalDateTime end,
      Set<Weekday> weekdays, int count,
      Map<String, String> optionalProps) {
    return new CreateEventCommand(eventService, view, subject, start, end,
        weekdays, count, null, optionalProps);
  }

  /**
   * Creates a command for a recurring event series that continues until a specific date.
   * Used when we have events to repeat until a particular deadline/end date.
   *
   * @param eventService manages event creation
   * @param view         displays results to user
   * @param subject      event title
   * @param start        start date/time
   * @param end          end date/time (null for all-day)
   * @param weekdays     weekdays to repeat on
   * @param endDate      last date to generate events until
   * @param optionalProps additional properties
   * @return CreateEventCommand for series with end date
   */
  public static CreateEventCommand forSeriesWithEndDate(
      InEventService eventService, InCalendarView view,
      String subject, LocalDateTime start, LocalDateTime end,
      Set<Weekday> weekdays, LocalDate endDate,
      Map<String, String> optionalProps) {
    return new CreateEventCommand(eventService, view, subject, start, end,
        weekdays, null, endDate, optionalProps);
  }

  /**
   * Executes the event creation based on the command type.
   * This method figures out what kind of event we are creating and calls
   *    the appropriate service method. The user will see a success message
   *    after the event(s) are created.
   *
   *  @throws CalendarException if event creation fails
   */
  @Override
  public void execute() throws CalendarException {
    if (isSingleEvent()) {
      executeSingleEvent();
    } else if (isSeriesWithCount()) {
      executeSeriesWithCount();
    } else if (isSeriesWithEndDate()) {
      executeSeriesWithEndDate();
    }
  }

  /**
   * Returns the description of what the command will do.
   *
   * @return event description
   */
  @Override
  public String getDescription() {
    return "Create event: " + subject;
  }

  private boolean isSingleEvent() {
    return weekdays == null;
  }

  private boolean isSeriesWithCount() {
    return occurrenceCount != null;
  }

  private boolean isSeriesWithEndDate() {
    return endDate != null;
  }

  private void executeSingleEvent() throws CalendarException {
    eventService.createSingleEvent(subject, start, end, optionalProps);
    view.displaySuccess(MSG_EVENT_CREATED + subject);
  }

  private void executeSeriesWithCount() throws CalendarException {
    eventService.createEventSeries(subject, start, end, weekdays,
        occurrenceCount, optionalProps);
    view.displaySuccess(MSG_SERIES_CREATED + subject + " ("
        + occurrenceCount + MSG_OCCURRENCES);
  }

  private void executeSeriesWithEndDate() throws CalendarException {
    eventService.createEventSeriesUntil(subject, start, end, weekdays,
        endDate, optionalProps);
    view.displaySuccess(MSG_SERIES_CREATED + subject + MSG_UNTIL
        + endDate + MSG_CLOSING_PAREN);
  }

  /**
   * Validates that all required fields are provided and valid.
   * Throws clear error messages to help developers fix issues quickly.
   */
  private void validateRequiredFields(InEventService eventService, InCalendarView view,
                                      String subject, LocalDateTime start) {
    if (eventService == null || view == null) {
      throw new IllegalArgumentException(
          "EventService and View are required - neither can be null");
    }
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "Event subject is required and cannot be empty");
    }
    if (start == null) {
      throw new IllegalArgumentException(
          "Start date/time is required - cannot be null");
    }
  }
}