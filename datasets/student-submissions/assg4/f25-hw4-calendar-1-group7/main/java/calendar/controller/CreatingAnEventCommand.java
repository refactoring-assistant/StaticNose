package calendar.controller;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.MyEventImplement;
import calendar.model.MyEventSeries;
import calendar.view.MyCalendarView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Command to create a single event or event series.
 */
public class CreatingAnEventCommand implements Command {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final boolean isRecurring;
  private final Set<DayOfWeek> repeatDays;
  private final Integer occurrences;
  private final LocalDate repeatEndDate;
  private final boolean isAllDay;

  /**
   * Constructor for single event.
   *
   * @param subject the event subject
   * @param start the start time
   * @param end the end time
   * @param isAllDay whether it's an all-day event
   * @param isRecurring whether it's recurring
   */
  public CreatingAnEventCommand(String subject, LocalDateTime start, LocalDateTime end,
                                boolean isAllDay, boolean isRecurring) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.isAllDay = isAllDay;
    this.isRecurring = isRecurring;
    this.repeatDays = null;
    this.occurrences = null;
    this.repeatEndDate = null;
  }

  /**
   * Constructor for recurring event (with occurrences or end date).
   *
   * @param subject the event subject
   * @param start the start time
   * @param end the end time
   * @param repeatDays days to repeat on
   * @param occurrences number of occurrences (null if using end date)
   * @param repeatEndDate end date for repeating (null if using occurrences)
   * @param isAllDay whether it's an all-day event
   */
  public CreatingAnEventCommand(String subject, LocalDateTime start, LocalDateTime end,
                                Set<DayOfWeek> repeatDays, Integer occurrences,
                                LocalDate repeatEndDate, boolean isAllDay) {
    this.subject = subject;
    this.start = start;
    this.end = end != null ? end : start.plusHours(9);
    this.isAllDay = isAllDay;
    this.isRecurring = true;
    this.repeatDays = repeatDays;
    this.occurrences = occurrences;
    this.repeatEndDate = repeatEndDate;
  }

  @Override
  public void execute(Calendar calendar, MyCalendarView view) {
    try {
      if (isRecurring) {
        createRecurringEvents(calendar, view);
      } else {
        createSingleEvent(calendar, view);
      }
    } catch (IllegalArgumentException e) {
      view.displayError("Error creating event: " + e.getMessage());
    }
  }

  private void createSingleEvent(Calendar calendar, MyCalendarView view) {
    Event event;
    if (isAllDay) {
      event = new MyEventImplement(subject, start);
    } else {
      event = new MyEventImplement(subject, start, end);
    }

    calendar.addEvent(event);
    view.displayMessage("Event created successfully: " + subject);
  }

  private void createRecurringEvents(Calendar calendar, MyCalendarView view) {
    MyEventSeries series;

    if (occurrences != null) {
      series = new MyEventSeries(subject, start, end, repeatDays, occurrences, isAllDay);
    } else {
      series = new MyEventSeries(subject, start, end, repeatDays, repeatEndDate, isAllDay);
    }

    List<Event> events = series.generateEvents();

    for (Event event : events) {
      try {
        calendar.addEvent(event);
      } catch (IllegalArgumentException e) {
        String message = "Warning: Skipped duplicate event on "
            + event.getStart().toLocalDate();
        view.displayError(message);
      }
    }

    view.displayMessage("Created " + events.size() + " recurring event(s): " + subject);
  }

  @Override
  public boolean validate() {
    if (subject == null || subject.trim().isEmpty()) {
      return false;
    }
    if (start == null) {
      return false;
    }
    if (!isAllDay && end == null) {
      return false;
    }
    if (isRecurring && repeatDays == null) {
      return false;
    }
    return true;
  }
}