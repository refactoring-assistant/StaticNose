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
 * Command for creating single events or event series.
 * Uses specific fields instead of instanceof checks for type safety.
 */
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
   * Constructs a CreateEventCommand.
   * Use factory methods to create instances for better type safety.
   *
   * @param eventService     the event service
   * @param view             the view
   * @param subject          event subject
   * @param start            start date/time
   * @param end              end date/time (null for all-day)
   * @param weekdays         weekdays for recurring (null for single)
   * @param occurrenceCount  number of occurrences (null if not count-based)
   * @param endDate          end date for series (null if not date-based)
   * @param optionalProps    optional properties
   */
  private CreateEventCommand(InEventService eventService, InCalendarView view,
                             String subject, LocalDateTime start, LocalDateTime end,
                             Set<Weekday> weekdays, Integer occurrenceCount,
                             LocalDate endDate, Map<String, String> optionalProps) {
    if (eventService == null || view == null) {
      throw new IllegalArgumentException("EventService and View cannot be null");
    }
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

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
   * Creates a command for a single (non-recurring) event.
   *
   * @param eventService the event service
   * @param view         the view
   * @param subject      event subject
   * @param start        start date/time
   * @param end          end date/time (null for all-day)
   * @param optionalProps optional properties
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
   * Creates a command for an event series with occurrence count.
   *
   * @param eventService the event service
   * @param view         the view
   * @param subject      event subject
   * @param start        start date/time
   * @param end          end date/time (null for all-day)
   * @param weekdays     weekdays to repeat on
   * @param count        number of occurrences
   * @param optionalProps optional properties
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
   * Creates a command for an event series with end date.
   *
   * @param eventService the event service
   * @param view         the view
   * @param subject      event subject
   * @param start        start date/time
   * @param end          end date/time (null for all-day)
   * @param weekdays     weekdays to repeat on
   * @param endDate      last date to repeat until
   * @param optionalProps optional properties
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

  @Override
  public String getDescription() {
    return "Create event: " + subject;
  }
}